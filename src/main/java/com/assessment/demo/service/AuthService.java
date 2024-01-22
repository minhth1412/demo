package com.assessment.demo.service;

import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.response.JwtResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    public JwtResponse signup(SignupRequest signupRequest);

    public JwtResponse login(LoginRequest loginRequest);

    public JwtResponse refreshToken(LoginRequest loginRequest, HttpServletRequest httpServletRequest, HttpServletResponse response);

    JwtResponse logout(HttpServletRequest request);

//    Object logout(HttpServletResponse response);
}
