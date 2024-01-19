package com.assessment.demo.service.impl;

import com.assessment.demo.entity.Role;
import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.response.JwtResponse;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.AuthService;
import com.assessment.demo.service.JwtService;
// import com.assessment.demo.utils.CookieUtils;
import com.assessment.demo.util.CookieUtils;
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
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Value("${spring.role_user.id}")
    private int roleUserId;

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
                    new Role(roleUserId));
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
        var user = userRepository.findUserByUsername(loginRequest.getUsername()).orElseThrow(() -> new RuntimeException("Invalid username or password"));
        JwtResponse jwtUserResponse = JwtResponse.fromUser(user);

        // Create 2 token
        CookieUtils.addTokenCookie(response, jwtService, user);
        CookieUtils.addRefreshTokenCookie(response, jwtService, user);
        CookieUtils.addRefreshTokenCookie(response, jwtService, user);

        log.debug("Login successfully!");
        return jwtUserResponse;
    }

    public JwtResponse refreshToken(LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        String authorizationHeader = request.getHeader("Authorization");
        String token;
        if (StringUtils.hasText(authorizationHeader) && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7).trim();
        } else {
            log.debug("The token is not valid!\n");
            throw new RuntimeException("The token is not exist!\n");
        }

        // extract username from the claim subject of the refresh token
        String username = jwtService.extractUsername(token);
        User user = userRepository.findUserByUsername(username).orElseThrow();
        if (jwtService.isTokenValid(token,user)) {
            // Return a msg to user through response
            JwtResponse messageResponse = JwtResponse.builder()
                    //.msg("Token refreshes successfully!")
                    .build();
            // Save both whole new tokens into cookies
            CookieUtils.addTokenCookie(response,jwtService,user);
            CookieUtils.addRefreshTokenCookie(response,jwtService,user);

            log.debug("Token refreshes successfully!");
            return messageResponse;
        }
        log.debug("if reaches here, it means that the token not refreshed\n");
        return null;
    }
}

