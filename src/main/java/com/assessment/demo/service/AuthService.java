package com.assessment.demo.service;

import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.response.JwtResponse;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    public JwtResponse signup(SignupRequest signupRequest);

    public JwtResponse login(LoginRequest loginRequest);

    public JwtResponse refreshToken(LoginRequest request, HttpServletRequest httpServletRequest);

    JwtResponse logout(HttpServletRequest request);//HttpServletRequest request);

//    Object logout(HttpServletResponse response);
}
