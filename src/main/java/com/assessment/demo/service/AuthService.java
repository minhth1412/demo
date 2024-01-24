package com.assessment.demo.service;

import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.request.resetPasswordRequest;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.dto.response.others.JwtResponse;
import com.assessment.demo.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    public JwtResponse signup(SignupRequest signupRequest);

    public JwtResponse login(LoginRequest loginRequest);

    public JwtResponse refreshToken(LoginRequest request, HttpServletRequest httpServletRequest);

    JwtResponse logout(HttpServletRequest request);

    UsualResponse resetPassword(resetPasswordRequest resetPasswordRequest,HttpServletRequest request);

    public User extractUserFromRequest(HttpServletRequest request);
}
