package com.assessment.demo.controller;

import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.response.JwtResponse;
import com.assessment.demo.dto.response.ErrorResponse;
import com.assessment.demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        if (response.getMsg() != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMsg());
        }
        response.setMsg("Login successfully!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        // Explicitly invalidate the current user's authentication token
        JwtResponse response = authService.logout(request);
        if (response.getMsg() != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMsg());
        }
        return ResponseEntity.ok("Logout successfully! Redirect to the login page....");
    }

    // API that refresh the present token
//    @PostMapping("refresh_token")
//    public ResponseEntity<?> refresh(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
//        return ResponseEntity.ok(authService.refreshToken(loginRequest, request, response));
//    }

    // CREATE CHANGE PASSWORD VS FORGET PASSWORD HERE
}
