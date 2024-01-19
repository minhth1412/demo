package com.assessment.demo.service;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;
import java.util.Map;

public interface JwtService {
    public String generateToken(UserDetails userDetails);

    String createToken(Map<String, Object> Claims,UserDetails userDetails,int lifespan);

    public String extractUsername(String token);
    public Date extractExpiration(String token);
    public boolean isTokenValid(String token, UserDetails userDetails);
}
