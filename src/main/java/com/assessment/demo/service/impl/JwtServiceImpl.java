package com.assessment.demo.service.impl;

import com.assessment.demo.entity.Token;
import com.assessment.demo.entity.User;
import com.assessment.demo.repository.TokenRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.GZIPInputStream;

import java.security.Key;
import java.util.function.Function;
import java.util.zip.GZIPOutputStream;

@Service
@Slf4j
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {
    @Value("${spring.jwt.secretKey}")
    private String secretKey;
    @Value("${spring.jwt.tokenLifespan}")
    private int tokenLifespan;
    @Value("${spring.jwt.refreshTokenLifespan}")
    private int refreshTokenLifespan;

    private TokenRepository tokenRepository;

    private UserRepository userRepository;

    public String generateToken(UserDetails userDetails,boolean isRefresh) {
        Map<String, Object> Claims = new HashMap<>();
        return createToken(Claims,userDetails,isRefresh);
    }

    @Override
    public String createToken(Map<String, Object> Claims,UserDetails userDetails,boolean isRefresh) {
        int lifespan = tokenLifespan;
        if (isRefresh)
            lifespan = refreshTokenLifespan;

        // Build a JWT with custom claims and subject set to the username
        return Jwts.builder()
                .setClaims(Claims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date()) // Token creation time
                // Token expiration time: 7 days from the current time
                // Date(new Date()) will return amount of ms that represent for that day
                .setExpiration(new Date((new Date()).getTime() + lifespan))
                .signWith(getSignKey(),SignatureAlgorithm.HS256) // Sign the token with a secret key
                .compact(); // Compact the JWT into its final form
    }

    // Retrieve the secret key for signing the JWT
    private Key getSignKey() {
        // Note: In a production scenario, consider storing the key securely, e.g., in application properties or environment variables
        byte[] key = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(key);
    }

    // Extract a specific claim from the JWT using a provided resolver function
    private <T> T extractClaim(String token,Function<Claims, T> claimsResolvers) {
        // Extract claims from the JWT
        final Claims claims = extractAllClaims(token);
        // Apply the resolver function to retrieve the desired claim
        return claimsResolvers.apply(claims);
    }

    // Extract all claims from the JWT
    private Claims extractAllClaims(String token) {
        // Parse and verify the JWT, then retrieve its body (claims)
        return Jwts
                .parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Extract the username claim from the JWT, it is placed at the subject of the token
    public String extractUsername(String token) {
        return extractClaim(token,Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token,Claims::getExpiration);
    }

    // Check if the token is valid by comparing the username and verifying expiration
    public boolean isTokenValid(String token,UserDetails userDetails) {
        final String username = extractUsername(token);
        // Check if the extracted username matches the username from UserDetails and the token is not expired
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    @Override
    public void refreshToken(User user) {
        String token = generateToken(user,false);
        String refreshToken = generateToken(user,true);
        Token oldToken = tokenRepository.findByTokenId(user.getToken().getTokenId()).orElse(null);
        if (oldToken != null) {
            oldToken.updateToken(token, refreshToken);
            user.setToken(null);
            tokenRepository.delete(oldToken);
        }
        else{
            throw new RuntimeException("There is no token to refresh!");
        }
        // Save changes
        tokenRepository.save(oldToken);
        userRepository.save(user);
    }

    @Override
    public void updateExpiredToken(String token,boolean isRefresh) {
        int time = (isRefresh) ? refreshTokenLifespan : tokenLifespan;

        Claims claims = this.extractAllClaims(token);
        claims.setIssuedAt(new Date());
        claims.setExpiration(new Date((new Date()).getTime() + time));

        // Build a new token with the updated claims
        Jwts.builder()
                .setClaims(claims)
                .signWith(getSignKey(),SignatureAlgorithm.HS256)
                .compact();
    }

    // Check if the token is expired by comparing the expiration time with the current time
    private boolean isTokenExpired(String token) {
        return extractClaim(token,Claims::getExpiration).before(new Date());
    }
}
