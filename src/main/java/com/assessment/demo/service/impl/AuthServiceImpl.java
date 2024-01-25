package com.assessment.demo.service.impl;

import com.assessment.demo.dto.request.resetPasswordRequest;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.entity.Role;
import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.response.others.JwtResponse;
import com.assessment.demo.entity.Token;
import com.assessment.demo.entity.User;
import com.assessment.demo.exception.InvalidJwtException;
import com.assessment.demo.repository.TokenRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.AuthService;
import com.assessment.demo.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@ComponentScan("com.assessment.demo.config") // For the passwordEncoder Bean
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${spring.role_user.id}")
    private int roleUserId;

    @Value("${spring.role_user.name}")
    private String roleUserName;

    // Regex for password requirements
    private static final String passwordRegex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$";

    public JwtResponse signup(SignupRequest signupRequest) {
        try {
            String err = getErrorFromSignup(signupRequest,userRepository);

            if (err != null)
                throw new ValidationException(err);
            User user = new User(signupRequest.getUsername(),
                    passwordEncoder.encode(signupRequest.getPassword()),
                    signupRequest.getEmail(),
                    signupRequest.getFirstname(),
                    signupRequest.getLastname(),
                    new Role(roleUserId,roleUserName),
                    signupRequest.getBio(),
                    signupRequest.getImage(),
                    signupRequest.getDateOfBirth(),
                    false);
            // A new user signing up has default USER role,
            // it can be edited later by whom has ADMIN role.

            log.info("Your account is created successfully! Return to the login page...");
            userRepository.save(user);

            return JwtResponse.fromUser(user,false);
        } catch (ValidationException e) {
            log.error("Validation error: " + e.getMessage());
            return JwtResponse.msg(e.getMessage());
        } catch (Exception e) {
            log.error("Error while processing user sign up with message: " + e.getMessage());
            return JwtResponse.msg(e.getMessage());
        }
    }

    private static String getErrorFromSignup(SignupRequest signupRequest,UserRepository userRepository) {
        String password = signupRequest.getPassword();
        String repassword = signupRequest.getRepassword();
        String msg = getErrorFromPassword(password, repassword);
        if (userRepository.existsByUsername(signupRequest.getUsername()))
            msg = "Username existed";
        else if (userRepository.existsByEmail(signupRequest.getEmail()))
            msg = "Email already existed";
        return msg;
    }

    private static String getErrorFromPassword(String password, String repassword) {
        String msg = null;
        if (password == null || password.trim().isEmpty())
            msg = "Password must not be empty";
        else if (!password.equals(repassword))
            msg = "Password and repassword do not match";
        else if (password.length() < 4)
            msg = "Password must be at least 5 characters long";
        else if (!password.matches(passwordRegex))
            msg = "Password must have at least one uppercase letter, one lowercase letter, one digit and one special character";
        return msg;
    }

    public JwtResponse login(LoginRequest loginRequest) {
        try {
            // Check username and password
            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new RuntimeException("Invalid username or password"));

            if (!passwordEncoder.matches(loginRequest.getPassword(),user.getPassword())) {
                throw new RuntimeException("Invalid username or password");
            }
            // Check online status
            if (user.getIsOnline())
                return JwtResponse.msg("You are already logged in!");
            else
                user.setIsOnline(true);

            Token userToken;
            String tk = jwtService.generateToken(user,false);
            String refreshTk = jwtService.generateToken(user,true);
            Date tkTime = jwtService.extractExpiration(tk);
            Date refreshTkTime = jwtService.extractExpiration(refreshTk);
            if (user.getToken() == null) {
                // Create tokens for new user's login
                userToken = new Token(tk,refreshTk,tkTime,refreshTkTime);
            } else {
                userToken = user.getToken();
                // Update expiration time for tokens of user logged in
                Date current_time = new Date();
                if (current_time.compareTo(userToken.getTokenExpireAt()) <= 0) {
                    userToken.updateToken(tk,refreshTk,tkTime,refreshTkTime);
                }
            }
            userToken.setUser(user);
            user.setToken(userToken);
            tokenRepository.save(userToken);
            userRepository.save(user);
            log.info("Login successfully!");
            // return a response with public information of current user
            return JwtResponse.fromUser(user);
        } catch (Exception e) {
            log.error("Error while processing user login with message: " + e.getMessage());
            return JwtResponse.msg(e.getMessage());
        }
    }

    public JwtResponse refreshToken(LoginRequest loginRequest,HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");

        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            String errorMessage = "The token is not the Bearer token format!";
            log.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
        // extract username from the claim subject of the refresh token
        String jwt = authorizationHeader.substring(7).trim();
        String username = jwtService.extractUsername(jwt);
        User user = userRepository.findByUsername(username).orElseThrow();  //~ Throw what exception?
        if (jwtService.isTokenValid(jwt,user)) {
            jwtService.refreshToken(user);
            log.info("Token refreshes successfully!");
            return JwtResponse.fromUser(user);
        }
        log.error("The token is not refreshed!");
        return null;
    }

    @Override
    public JwtResponse logout(HttpServletRequest request) {//HttpServletRequest request) {
        User user = extractUserFromRequest(request);
        if (user == null) {
            return JwtResponse.msg("Invalid token");
        }
        if (!user.getIsOnline()) {
            return JwtResponse.msg("Bad credentials! You need to login first to do this action!");
        } else {
            user.setIsOnline(false);
            userRepository.save(user);
            return JwtResponse.msg(null);
        }
    }

    // Apply for all accounts
    @Override
    public UsualResponse resetPassword(resetPasswordRequest resetPasswordRequest,HttpServletRequest request) {
        String msg = null;
        try {
            String password = resetPasswordRequest.getNewPassword();
            String repassword = resetPasswordRequest.getConfirmNewPassword();
            User user = extractUserFromRequest(request);
            if (user == null)
                msg = "Bad credentials!";
            else if (!Objects.equals(resetPasswordRequest.getOldPassword(),user.getPassword()))
                msg = "Wrong old password!";
            else if (resetPasswordRequest.getNewPassword().length() < 4) {
                msg = "";
            } else if (!resetPasswordRequest.getNewPassword().matches(passwordRegex))
                msg = "Password must have at least one uppercase letter, one lowercase letter and one digit, one special character, and a minimum length of 5 characters";
            else if (!Objects.equals(resetPasswordRequest.getNewPassword(), resetPasswordRequest.getConfirmNewPassword()))
                msg = "Confirm password does not match with the new one!";
            HttpStatus status = HttpStatus.BAD_REQUEST;
            if (msg == null) {
                status = HttpStatus.OK;
                msg = "Your password is changed!";
            }
            return UsualResponse.builder().exceptionType(status).message(msg).build();
        } catch (InvalidJwtException e) {
            log.error(e.getMessage());
            return UsualResponse.builder().exceptionType(HttpStatus.UNAUTHORIZED).message("Invalid token!").build();
        } catch (Exception e) {
            return UsualResponse.init(HttpStatus.INTERNAL_SERVER_ERROR,"An unexpected error occurred: " + e.getMessage());
        }
    }

    @Override
    public User extractUserFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        // Using the auth Bearer ("Bearer " + <token>), check if it is not the bearer token.
        // If not, it continues with the filter chain without attempting JWT authentication.
        if (org.apache.commons.lang3.StringUtils.isEmpty(authHeader) || !org.apache.commons.lang3.StringUtils.startsWith(authHeader,"Bearer ")) {
            throw new InvalidJwtException("The token is not the Bearer token format!");
        }
        final String jwt = authHeader.substring(7);         // We get the token on the header of request after this
        final String username = jwtService.extractUsername(jwt);
        log.info("The token in request is: " + jwt);
        return userRepository.findByUsername(username).orElse(null);
    }


}

