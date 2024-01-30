package com.assessment.demo.service;

import com.assessment.demo.dto.request.LoginRequest;
import com.assessment.demo.dto.request.SignupRequest;
import com.assessment.demo.dto.request.ResetPasswordRequest;
import com.assessment.demo.dto.response.general.UsualResponse;
import com.assessment.demo.entity.User;
import jakarta.servlet.http.HttpServletRequest;

public interface AuthService {
    public UsualResponse signup(SignupRequest signupRequest);

    public UsualResponse login(LoginRequest loginRequest);

    public UsualResponse refreshToken(HttpServletRequest httpServletRequest);

    UsualResponse logout(HttpServletRequest request);

    UsualResponse resetPassword(ResetPasswordRequest resetPasswordRequest,HttpServletRequest request);

    public User extractUserFromRequest(HttpServletRequest request);

    public String getErrorFromSignup(SignupRequest signupRequest);

    public String getErrorFromPassword(String password,String repassword);
}
