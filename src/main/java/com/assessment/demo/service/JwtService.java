package com.assessment.demo.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public interface JwtService {
    public String generateToken(UserDetails userDetails);

    String createToken(Map<String, Object> Claims,UserDetails userDetails, boolean isRefresh);

    public String extractUsername(String token);
    public Date extractExpiration(String token);
    public boolean isTokenValid(String token, UserDetails userDetails);

    public void saveCompressedToken(String tokenData, String refreshTokenData);

    public String getDecompressedToken(UUID tokenId);
}
