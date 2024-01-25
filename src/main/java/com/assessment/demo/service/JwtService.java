package com.assessment.demo.service;

import com.assessment.demo.entity.Token;
import com.assessment.demo.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Map;

public interface JwtService {

    public String extractJwtFromRequest(HttpServletRequest request);

    public String generateToken(UserDetails userDetails, boolean isRefresh);

    String createToken(Map<String, Object> Claims, UserDetails userDetails, boolean isRefresh);

    public String extractUsername(String token);

    public Date extractExpiration(String token);

    public boolean isTokenValid(String token, UserDetails userDetails);

    public boolean isTokenExpired(String token);

    public void refreshToken(User user);

    public void updateExpiredToken(String token, boolean isRefresh);

//    public void saveCompressedToken(String tokenData, String refreshTokenData);
//
//    public String getDecompressedToken(UUID tokenId);
}
