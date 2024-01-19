package com.assessment.demo.service.impl;

import com.assessment.demo.entity.Role;
import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.response.JwtResponse;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.TokenRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.AuthService;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.RoleService;
import io.jsonwebtoken.Jwt;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@ComponentScan("com.assessment.demo.config") // For the passwordEncoder Bean
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final RoleService roleService;

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
                    new Role(roleUserId, roleUserName), null, null, null);
            // A new user signing up auto receive USER role,
            // it can be edited later by whom has ADMIN role.

            log.info("Your account is created successfully!");
            userRepository.save(user);

            return JwtResponse.fromUser(user);
        } catch (ValidationException e){
            log.error("Validation error: " + e.getMessage());
            return null;
        }
        catch (Exception e) {
            log.error("Error while processing user sign up with message: " + e.getMessage());
            return null;
        }
    }

    private static String getError(SignupRequest signupRequest, UserRepository userRepository) {
        if (signupRequest.getPassword() == null || signupRequest.getPassword().trim().isEmpty()) {
            return "Password must not be empty";
        }
        if (!signupRequest.getPassword() .equals(signupRequest.getRepassword())) {
            return "Password and repassword do not match";
        }
        if (signupRequest.getPassword().length() < 4) {
            return "Password must be at least 4 characters long";
        }
        if (userRepository.existsByUsername(signupRequest.getUsername()))
            return "Username existed";
        if (userRepository.existsByEmail(signupRequest.getEmail()))
            return "Email already existed";
        return null;
    }

    public JwtResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        var user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow(() -> new RuntimeException("Invalid username or password"));
        JwtResponse jwtResponse = JwtResponse.fromUser(user);

//        var token = tokenRepository.findByUser(user).orElse(null);
//        if (token == null) {
//        // Create 2 token
//        addToken(response, jwtService, user);
//        addRefreshToken(response, jwtService, user);
//        }
        log.debug("Login successfully!");
        return jwtResponse;
    }

//    public JwtResponse refreshToken(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
//        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
//        String authorizationHeader = request.getHeader("Authorization");
//        String token;
//        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
//            token = authorizationHeader.substring(7).trim();
//        } else {
//            log.debug("The token is not valid!\n");
//            throw new RuntimeException("The token is not exist!\n");
//        }
//
//        // extract username from the claim subject of the refresh token
//        String username = jwtService.extractUsername(token);
//        User user = userRepository.findByUsername(username).orElseThrow();
//        if (jwtService.isTokenValid(token,user)) {
//            // Return a msg to user through response
//            // Save both whole new tokens into cookies
////            addToken(response,jwtService,user);
////            addRefreshToken(response,jwtService,user);
//
////            log.debug("Token refreshes successfully!");
//            return JwtResponse.fromUser(user);
//        }
//        log.debug("if reaches here, it means that the token not refreshed\n");
//        return null;
//    }
}

