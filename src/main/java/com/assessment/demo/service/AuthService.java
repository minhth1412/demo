package com.assessment.demo.service;

import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.request.resetPasswordRequest;
import com.assessment.demo.dto.response.others.UsualResponse;
import com.assessment.demo.dto.response.others.JwtResponse;
import com.assessment.demo.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    public UsualResponse signup(SignupRequest signupRequest);

    public UsualResponse login(LoginRequest loginRequest);

    public UsualResponse refreshToken(HttpServletRequest httpServletRequest);

    JwtResponse logout(HttpServletRequest request);

    UsualResponse resetPassword(resetPasswordRequest resetPasswordRequest,HttpServletRequest request);

    public User extractUserFromRequest(HttpServletRequest request);
}
