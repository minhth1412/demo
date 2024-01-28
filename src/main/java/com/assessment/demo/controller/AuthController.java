package com.assessment.demo.controller;

import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.request.ResetPasswordRequest;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.dto.response.others.JwtResponse;

import com.assessment.demo.repository.PostRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.AuthService;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.PostService;
import com.assessment.demo.service.UserService;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/auth")
public class AuthController extends BaseController{
    /**
     * Authentication controller, includes:<p>
     * -> Signup user account       (/api/auth/signup)<p>
     * -> log in                     (/api/auth/login)<p>
     * -> log out                   (/api/auth/logout)<p>
     * -> refresh token             (/api/auth/refresh_token)<p>
     */

    @Autowired
    public AuthController(AuthService authService,JwtService jwtService,PostService postService,UserService userService,UserRepository userRepository,PostRepository postRepository) {
        super(authService,jwtService,postService,userService,userRepository,postRepository);
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        UsualResponse response = authService.signup(signupRequest);
        return responseEntity(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest)  {
        UsualResponse response = authService.login(loginRequest);
        return responseEntity(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request){
        // Explicitly invalidate the current user's authentication token
        UsualResponse response = authService.logout(request);
        return responseEntity(response);
    }

    // API that refresh the present token due to expired
    @GetMapping("/refresh_token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        UsualResponse response = authService.refreshToken(request);
        return responseEntity(response);
    }

    // Forgot password: need 3rd party to handle, this will be deployed later
}
