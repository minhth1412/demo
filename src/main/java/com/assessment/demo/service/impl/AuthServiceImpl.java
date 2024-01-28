package com.assessment.demo.service.impl;

import com.assessment.demo.dto.request.ResetPasswordRequest;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.entity.Role;
import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.response.others.JwtResponse;
import com.assessment.demo.entity.Token;
import com.assessment.demo.entity.User;
import com.assessment.demo.exception.InvalidJwtException;
import com.assessment.demo.repository.RoleRepository;
import com.assessment.demo.repository.TokenRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.AuthService;
import com.assessment.demo.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountLockedException;
import java.util.Date;
import java.util.Objects;

import static com.assessment.demo.util.EmailUtils.isEmail;

@Service
@RequiredArgsConstructor
@ComponentScan("com.assessment.demo.config") // For the passwordEncoder Bean
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${spring.role_user.id}")
    private int roleUserId;

    @Value("${spring.role_user.name}")
    private String roleUserName;

    // Regex for password requirements
    private static final String passwordRegex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$";

    @Override
    public UsualResponse signup(SignupRequest signupRequest) {
        try {
            String err = getErrorFromSignup(signupRequest);

            if (err != null)
                throw new ValidationException(err);
            User user = new User(signupRequest.getUsername(),
                    passwordEncoder.encode(signupRequest.getPassword()),
                    signupRequest.getEmail(),
                    signupRequest.getFirstname(),
                    signupRequest.getLastname(),
                    // A new user has default USER role, it can be edited in db by ADMIN.
                    new Role(roleUserId, roleUserName),
                    signupRequest.getBio(),
                    signupRequest.getImage(),
                    signupRequest.getDateOfBirth(),
                    false);
            userRepository.save(user);

            String msg = "New account is created successfully! Return to the login page...";
            log.info(msg);
            return UsualResponse.success(JwtResponse.fromUserWithoutToken(user, msg));
        } catch (ValidationException e) {
            log.error("Validation error: " + e.getMessage());
            return UsualResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error while processing user sign up with message: " + e.getMessage());
            return UsualResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public UsualResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElse(null);
        try {
            // Check username and password
            if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword()))
                throw new RuntimeException("Invalid username or password");
            else if (!user.getStatus())
                throw new AccountLockedException("Your account is locked and can not be used right now!");
            else if (user.getIsOnline())
                return UsualResponse.error(HttpStatus.BAD_REQUEST, "You are already logged in!");
            else
                user.setIsOnline(true);

            // After authentication phase, update user token in database
            updateTokenForLoggingInUser(user);

            userRepository.save(user);
            String msg = "Login successfully!";
            log.info(msg);
            // return a response with public information of current user
            return UsualResponse.success(JwtResponse.fromUserWithToken(user, msg));
        } catch (AccountLockedException | RuntimeException e) {
            log.error(e.getMessage());
            return UsualResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            log.error("Error while processing user login with message: " + e.getMessage());
            return UsualResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public UsualResponse refreshToken(HttpServletRequest request) {
        try {
            String msg = "Token refreshes successfully!";
            // Extract username from the input token
            String username = jwtService.userFromJwtInRequest(request);
            // Create user entity from found username
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Invalid token!"));
            String refreshJWT = jwtService.extractJwtFromRequest(request);

            // If the refresh token from the request that valid with current user logging in
            if (jwtService.isTokenInRequestValid(request, user)
                    && Objects.equals(user.getToken().getCompressedRefreshTokenData(), refreshJWT)) {
                jwtService.refreshToken(user, true);
                log.info(msg);
                return UsualResponse.success(JwtResponse.fromUserWithToken(user, msg));
            } else {
                msg = "Invalid token from request!";
                if (!Objects.equals(user.getToken().getCompressedRefreshTokenData(), refreshJWT))
                    msg = "Invalid token from request! @@@@@";
                log.info(user.getToken().getCompressedRefreshTokenData());
                log.info(refreshJWT);
                log.error(msg);
                return UsualResponse.error(HttpStatus.BAD_REQUEST, msg);
            }
        } catch (InvalidJwtException e) {
            log.info("The user token is expired: {}", e.getMessage());
            return UsualResponse.error(HttpStatus.BAD_REQUEST, e.getMessage());

        } catch (Exception e) {
            log.error("Error while processing refresh token with message: " + e.getMessage());
            return UsualResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }

    @Override
    public UsualResponse logout(HttpServletRequest request) {//HttpServletRequest request) {
        User user = extractUserFromRequest(request);
        if (user == null)
            return UsualResponse.error(HttpStatus.BAD_REQUEST, "Invalid token");
        if (!user.getIsOnline())
            return UsualResponse.error(HttpStatus.FORBIDDEN,
                    "Bad credentials! You need to login first to do this action!");
        user.setIsOnline(false);
        userRepository.save(user);
        return UsualResponse.success("Logout successfully! Redirect to the login page....");

    }

    // Apply for all accounts
    @Override
    public UsualResponse resetPassword(ResetPasswordRequest resetPasswordRequest, HttpServletRequest request) {
        String msg = null;
        try {
            if (resetPasswordRequest == null)
                throw new InvalidJwtException("Invalid token");
            String password = resetPasswordRequest.getNewPassword();
            String repassword = resetPasswordRequest.getConfirmNewPassword();
            User user = extractUserFromRequest(request);
            if (user == null)
                msg = "Bad credentials!";
            else if (!Objects.equals(resetPasswordRequest.getOldPassword(), passwordEncoder.encode(user.getPassword())))
                msg = "Wrong old password!";
            else
                msg = getErrorFromPassword(password, repassword);
            if (msg == null)
                return UsualResponse.success("Your password is changed!");
            return UsualResponse.error(HttpStatus.BAD_REQUEST, msg);
        } catch (InvalidJwtException e) {
            log.error(e.getMessage());
            return UsualResponse.error(HttpStatus.UNAUTHORIZED, "Invalid token!");
        } catch (Exception e) {
            return UsualResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred: " + e.getMessage());
        }
    }

    @Override
    public User extractUserFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        // Using the auth Bearer ("Bearer " + <token>), check if it is not the bearer token.
        // If not, it continues with the filter chain without attempting JWT authentication.
        if (org.apache.commons.lang3.StringUtils.isEmpty(authHeader) || !org.apache.commons.lang3.StringUtils.startsWith(authHeader, "Bearer ")) {
            throw new InvalidJwtException("The token is not the Bearer token format!");
        }
        final String jwt = authHeader.substring(7);         // We get the token on the header of request after this
        final String username = jwtService.extractUsername(jwt);
        log.info("The token in request is: " + jwt);
        return userRepository.findByUsername(username).orElse(null);
    }

    public String getErrorFromPassword(String password, String repassword) {
        String msg = null;
        if (password == null || password.trim().isEmpty())
            msg = "Password must not be empty";
        else if (repassword == null || repassword.trim().isEmpty())
            msg = "Repassword must not be empty";
        else if (!password.equals(repassword))
            msg = "Password and repassword do not match";
        else if (password.length() < 4)
            msg = "Password must be at least 5 characters long";
        else if (!password.matches(passwordRegex))
            msg = "Password must have at least one uppercase letter, one lowercase letter, one digit and one special character";
        return msg;
    }

    @Override
    public String getErrorFromSignup(SignupRequest signupRequest) {
        String password = signupRequest.getPassword();
        String repassword = signupRequest.getRepassword();
        String username = signupRequest.getUsername();
        String email = signupRequest.getEmail();
        String msg = getErrorFromPassword(password, repassword);
        String role = signupRequest.getRole();
        if (username == null || email == null)
            msg = "Lack information";
        else if (userRepository.existsByUsername(username))
            msg = "Username existed";
        else if (userRepository.existsByEmail(email))
            msg = "Email already existed";
        else if (!isEmail(email))
            msg = "Email is in wrong type";
        else if (role != null && !roleRepository.existsByRoleName(role))
            msg = "Role " + role + " is not existed!";
        return msg;
    }

    private void updateTokenForLoggingInUser(User user) {
        Token userToken;
        String tk = jwtService.generateToken(user, false);
        String refreshTk = jwtService.generateToken(user, true);
        Date tkTime = jwtService.extractExpiration(tk);
        Date refreshTkTime = jwtService.extractExpiration(refreshTk);
        if (user.getToken() == null)
            // Create tokens for new user's login
            userToken = new Token(tk, refreshTk, tkTime, refreshTkTime);
        else {
            userToken = user.getToken();
            // Update expiration time for tokens of user logged in
            Date current_time = new Date();
            if (current_time.compareTo(userToken.getTokenExpireAt()) <= 0)
                userToken.updateToken(tk, refreshTk, tkTime, refreshTkTime);
        }
        userToken.setUser(user);
        user.setToken(userToken);
        tokenRepository.save(userToken);
        userRepository.save(user);
    }
}

