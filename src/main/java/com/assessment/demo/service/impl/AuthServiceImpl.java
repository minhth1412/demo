package com.assessment.demo.service.impl;

import com.assessment.demo.dto.request.LogoutRequest;
import com.assessment.demo.entity.Role;
import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.response.JwtResponse;
import com.assessment.demo.entity.Token;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.TokenRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.AuthService;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.RoleService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;

import java.util.Date;

@Service
@RequiredArgsConstructor
@ComponentScan("com.assessment.demo.config") // For the passwordEncoder Bean
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private Authentication authentication;
    private final JwtService jwtService;
    private final RoleService roleService;

//    private final HttpSession httpSession;

    @Value("${spring.role_user.id}")
    private int roleUserId;

    @Value("${spring.role_user.name}")
    private String roleUserName;

    public JwtResponse signup(SignupRequest signupRequest) {
        try {
            String err = getError(signupRequest, userRepository);

            if (err != null)
                throw new ValidationException(err);
            User user = new User(signupRequest.getUsername(),
                    passwordEncoder.encode(signupRequest.getPassword()),
                    signupRequest.getEmail(),
                    signupRequest.getFirstname(),
                    signupRequest.getLastname(),
                    new Role(roleUserId, roleUserName),
                    signupRequest.getBio(),
                    signupRequest.getImage(),
                    signupRequest.getDateOfBirth());
            // A new user signing up auto receive USER role,
            // it can be edited later by whom has ADMIN role.

            log.info("Your account is created successfully! Return to the login page...");
            userRepository.save(user);

            return JwtResponse.fromUser(user, false);
        } catch (ValidationException e) {
            log.error("Validation error: " + e.getMessage());
            return JwtResponse.msg(e.getMessage());
        } catch (Exception e) {
            log.error("Error while processing user sign up with message: " + e.getMessage());
            return JwtResponse.msg(e.getMessage());
        }
    }

    private static String getError(SignupRequest signupRequest, UserRepository userRepository) {
        if (signupRequest.getPassword() == null || signupRequest.getPassword().trim().isEmpty()) {
            return "Password must not be empty";
        }
        if (!signupRequest.getPassword().equals(signupRequest.getRepassword())) {
            return "Password and repassword do not match";
        }
        if (signupRequest.getPassword().length() < 4) {
            return "Password must be at least 5 characters long";
        }
        if (userRepository.existsByUsername(signupRequest.getUsername()))
            return "Username existed";
        if (userRepository.existsByEmail(signupRequest.getEmail()))
            return "Email already existed";
        // Regex for password requirements
        String passwordRegex = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$";

        if (!signupRequest.getPassword().matches(passwordRegex)) {
            return "Password must have at least one uppercase letter, one lowercase letter, one digit, one special character, and a minimum length of 5 characters";
        }
        return null;
    }

//    private boolean isUserAlreadyLoggedIn() {
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        if (auth != null && auth.getPrincipal() instanceof UserDetails) {
//            UserDetails userDetails = (UserDetails) auth.getPrincipal();
//            return httpSession.getAttribute("alreadyLoggedIn") != null;
//        }
//        return false;
//    }

    public JwtResponse login(LoginRequest loginRequest) {
        try {
            log.info("Second reach of login service!");
            // Get user and its info from the loginRequest
            String reqUsername = loginRequest.getUsername();
            String reqPassword = loginRequest.getPassword();
            log.info("Come here!!!!!!!! 3rd reach!");
            User user = userRepository.findByUsername(reqUsername)
                    .orElseThrow(() -> new RuntimeException("Invalid username or password"));
            //
            if (user.getStatus())
                return JwtResponse.msg("You are already logged in!");
            else
                log.info("The status when created auto set to false");
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                log.info("Auth is null");
                //return JwtResponse.msg("A user is logging in, you need to end his/her session first!");
            }
            else
                log.info("Auth is not null");
            UsernamePasswordAuthenticationToken userAuth = new
                    UsernamePasswordAuthenticationToken(reqUsername, reqPassword);
            //
            Authentication authentication = authenticationManager.authenticate(userAuth);
            // Below holds security-related information for the current thread.
            SecurityContextHolder.getContext().setAuthentication(authentication);

            Token userToken;
            if (user.getToken() == null) {
                // Create tokens for new user's login
                userToken = new Token(jwtService.generateToken(user, false),
                        jwtService.generateToken(user, true));

                userToken.setUser(user);
                user.setToken(userToken);

            } else {
                userToken = user.getToken();
                //log.info("Come here the not null token");
                // Update expiration time for tokens of user logged in
                Date current_time = new Date();
                if (current_time.compareTo(userToken.getTokenExpireAt()) <= 0) {
                    userToken.updateTimeExpired();
                }
            }
            user.setStatus(true);
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

    public JwtResponse refreshToken(LoginRequest loginRequest, HttpServletRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequest.getUsername(), loginRequest.getPassword()));
        String authorizationHeader = request.getHeader("Authorization");
        String token;
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7).trim();
        } else {
            log.info("The token is not valid!\n");
            throw new RuntimeException("The token is not exist!\n");
        }

        // extract username from the claim subject of the refresh token
        String username = jwtService.extractUsername(token);
        User user = userRepository.findByUsername(username).orElseThrow();
        if (jwtService.isTokenValid(token, user)) {
            jwtService.refreshToken(user);
            log.info("Token refreshes successfully!");
            return JwtResponse.fromUser(user);
        }
        log.info("if reaches here, it means that the token not refreshed\n");
        return null;
    }

    @Override
    public JwtResponse logout(LogoutRequest request){//HttpServletRequest request) {
        log.info("Come here??????");
        final String authHeader = request.getToken();//request.getHeader("Authorization");
        // Using the auth Bearer ("Bearer " + <token>), check if it is not the bearer token.
        // If not, it continues with the filter chain without attempting JWT authentication.
        if (org.apache.commons.lang3.StringUtils.isEmpty(authHeader) || !org.apache.commons.lang3.StringUtils.startsWith(authHeader, "Bearer ")) {
            String s = "The token is not the Bearer token format!\n";
            log.error(s);
            return JwtResponse.msg(s);
        }
        final String jwt = authHeader.substring(7);         // We get the token on the header of request after this
        final String username = jwtService.extractUsername(jwt);
        log.info("The token in request is: " + jwt);
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null){
            return JwtResponse.msg("Invalid token");
        }
        if (!user.getStatus()) {
            return JwtResponse.msg("Bad credentials! You need to login first to do this action!");
        }
        else
            log.info("The hell is status equals true!??");
        // set the security-related information for the current thread into null value.
        SecurityContextHolder.getContext().setAuthentication(null);
        return JwtResponse.msg(null);
    }
}

