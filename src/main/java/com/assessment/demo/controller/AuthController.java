package com.assessment.demo.controller;

import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.request.resetPasswordRequest;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.dto.response.others.JwtResponse;

import com.assessment.demo.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/auth")
public class AuthController extends BaseController{
    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest signupRequest) {
        UsualResponse response = authService.signup(signupRequest);
        return responseEntity(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest loginRequest)
    {
        // If no errors, return 200 with login method from authService
        UsualResponse response = authService.login(loginRequest);
        return responseEntity(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request){
        // Explicitly invalidate the current user's authentication token
        JwtResponse response = authService.logout(request);
        if (response.getMsg() != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response.getMsg());
        }
        return ResponseEntity.ok("Logout successfully! Redirect to the login page....");
    }

    // API that refresh the present token due to expired
    @GetMapping("/refresh_token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request) {
        UsualResponse response = authService.refreshToken(request);
        return responseEntity(response);
    }

    @PostMapping("/reset_password")
    public ResponseEntity<?> resetPassword(@RequestBody resetPasswordRequest resetPasswordRequest,HttpServletRequest request) {
        UsualResponse response = authService.resetPassword(resetPasswordRequest,request);
        return responseEntity(response);
    }

    // ~Forgot password method: need 3rd party to handle, so this can be deployed later
}
