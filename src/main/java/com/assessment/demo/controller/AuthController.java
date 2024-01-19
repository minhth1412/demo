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
        if (!isEmail(signupRequest.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email is invalid");
        }
        return (response == null) ?
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error during sign up")
                : ResponseEntity.ok(response);

    }

    @PostMapping("login")
    public ResponseEntity<?> login(
            @RequestBody LoginRequest loginRequest,
            HttpServletResponse response,
            BindingResult bindingResult)
    {
        // Check for validation errors
        if (bindingResult.hasErrors()) {
            // There are validation errors, handle them
            List<ObjectError> errors = bindingResult.getAllErrors();
            StringBuilder errorMessage = new StringBuilder("Validation error(s): ");
            for (ObjectError error : errors) {
                errorMessage.append(error.getDefaultMessage()).append("; ");
            }
            return ResponseEntity.badRequest().body(errorMessage.toString());
        }
        return ResponseEntity.ok(authService.login(loginRequest, response));
    }

    // API that refresh the present token
//    @PostMapping("refresh_token")
//    public ResponseEntity<MessageResponse> refresh(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
//        return ResponseEntity.ok(authService.refreshToken(loginRequest, request, response));
//    }

    // CREATE CHANGE PASSWORD VS FORGET PASSWORD HERE
}
