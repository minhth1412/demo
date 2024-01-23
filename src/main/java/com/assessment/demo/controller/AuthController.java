package com.assessment.demo.controller;

import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.response.JwtResponse;

import com.assessment.demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import static com.assessment.demo.util.EmailUtils.isEmail;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth/")
public class AuthController {
    private final AuthService authService;

    @PostMapping("signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        JwtResponse response = authService.signup(signupRequest);
        if (response.getMsg() != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMsg());
        }
        if (!isEmail(signupRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is invalid");
        }
        response.setMsg("Sign up successfully! Please return to the login page and sign in!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest loginRequest)
    {
        // If no errors, return 200 with login method from authService
        JwtResponse response = authService.login(loginRequest);
        if (response.getMsg() != null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMsg());
        }
        response.setMsg("Login successfully!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("logout")
    public ResponseEntity<?> logout(HttpServletRequest request){ // HttpServletRequest request) {
        // Explicitly invalidate the current user's authentication token
        JwtResponse response = authService.logout(request);
        log.info("Here comes.....");
        if (response.getMsg() != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMsg());
        }
        return ResponseEntity.ok("Logout successfully! Redirect to the login page....");
    }

    // API that refresh the present token due to expired
    @PostMapping("refresh_token")
    public ResponseEntity<?> refresh(@RequestBody LoginRequest req, HttpServletRequest request) {
        String x = request.getQueryString();
        log.info(x);
        return ResponseEntity.ok(authService.refreshToken(req, request));
    }

    // CREATE CHANGE PASSWORD VS FORGET PASSWORD HERE
}
