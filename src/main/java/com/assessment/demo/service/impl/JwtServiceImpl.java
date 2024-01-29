package com.assessment.demo.service.impl;

import com.assessment.demo.entity.Token;
import com.assessment.demo.entity.User;
import com.assessment.demo.exception.InvalidJwtException;
import com.assessment.demo.repository.TokenRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.JwtService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import io.jsonwebtoken.security.SignatureException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.security.Key;
import java.util.function.Function;

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

    private final TokenRepository tokenRepository;
    private final UserRepository userRepository;

    public String extractJwtFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (org.apache.commons.lang3.StringUtils.isEmpty(authHeader) ||
                !org.apache.commons.lang3.StringUtils.startsWith(authHeader, "Bearer ")) {
            String errorMessage = "The token is not in the Bearer token format!";
            log.info(errorMessage);
            return null;
        }
        // Extract and return the token from the Authorization header
        return authHeader.substring(7);
    }

    public String generateToken(UserDetails userDetails, boolean isRefresh) {
        int lifespan = isRefresh ? refreshTokenLifespan : tokenLifespan;
        return tokenBuilder(userDetails.getUsername(), lifespan);
    }

    // Retrieve the secret key for signing the JWT
    private Key getSignKey() {
        // Note: In a production scenario, consider storing the key securely, e.g., in application properties or environment variables
        byte[] key = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(key);
    }

    // Extract a specific claim from the JWT using a provided resolver function
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolvers) {
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

    @Override
    // Extract the username claim from the JWT, it is placed at the subject of the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Check if the token is valid by comparing the username and verifying expiration
    @Override
    public boolean isTokenValid(String token, UserDetails userDetails) {
        if (token == null)
            return false;
        try {
            Claims claims = extractAllClaims(token);
            String username = extractUsername(token);
            if (!username.equals(userDetails.getUsername())) {
                log.info("This is the place that username {} and user from token {} did not match", username, userDetails.getUsername());
                throw new RuntimeException("Invalid token!");
            }
            // Check if the extracted username matches the username from UserDetails and the token is not expired
            else if (isTokenExpired(token))
                throw new ExpiredJwtException(null, claims, "JWT token is expired! Please login again.");
            else
                return true;

        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error(e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("An unexpected error occurred during JWT validation: {}", e.getMessage());
        }
        return false;
    }

    // when change the username, the token times is also refresh
//    private String changeUsernameInToken(String token, String newUsername, int lifespan) {
//        // Build a new token with the claims from the original token and the new username
//        return tokenBuilder(newUsername, lifespan);
//    }

    public void changeUsernameInToken(String existingToken, String newUsername, User user, Boolean isRefresh) {
        try {
            Claims existingClaims = extractAllClaims(existingToken);
            Date expirationDate = existingClaims.getExpiration();
            Date issuedAt = existingClaims.getIssuedAt();

            String token = Jwts.builder()
                    .setSubject(newUsername)
                    .setIssuedAt(issuedAt)
                    .signWith(getSignKey(), SignatureAlgorithm.HS256)
                    .setExpiration(expirationDate)
                    .compact();

            Token tokenEntity = user.getToken();
            if (isRefresh)
                tokenEntity.updateRefreshTokenOnly(token);
            else
                tokenEntity.updateTokenOnly(token);
            user.setToken(tokenEntity);
            tokenRepository.save(tokenEntity);
            userRepository.save(user);

        } catch (Exception e) {
            log.error("Error changing username in token: {}", e.getMessage());
            throw new RuntimeException("Error changing username in token.");
        }
    }

    @Override
    public void refreshToken(User user, Boolean isResetTime) {
        userRepository.save(user);
        String token = generateToken(user, false);
        String refreshToken = generateToken(user, true);
        // Extract the time expired of tokens
        Date tkTime = extractExpiration(token);
        Date refreshTkTime = extractExpiration(refreshToken);
        Token oldToken = user.getToken();
        try {
            if (!isResetTime && user.getToken() != null) {
                oldToken.updateToken(token, refreshToken, tkTime, refreshTkTime);
            }
        } catch (Exception e) {
            log.info("The token of user is not initialized yet till now!");
            token = tokenBuilder(token, tokenLifespan);
            refreshToken = tokenBuilder(refreshToken, refreshTokenLifespan);
            oldToken = new Token(token, refreshToken, tkTime, refreshTkTime);
        }
        // if the oldToken and the new token generated above have the same name
        // -> The refresh token is not from update username, so refresh all
        // else if the oldToken and the new token have different name, it proves that the user has adjusted the username.
        // -> Not reset time expiration, but save new tokens generated from new username
        finally {
            if (oldToken != null) {
                oldToken.setUser(user);
                user.setToken(oldToken);
                tokenRepository.save(oldToken);
                userRepository.save(user);
                log.info("The token is refreshed!");
            } else
                throw new RuntimeException("Can not refresh the token because it is not found!");
        }
    }

    private String tokenBuilder(String username, int lifespan) {
        Date currentDate = new Date();
        Date expireDate = new Date(currentDate.getTime() + lifespan);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(currentDate)
                .setExpiration(expireDate)
                .signWith(getSignKey(), SignatureAlgorithm.HS256) // Sign the token with a secret key
                .compact(); // Compact the JWT into its final form
    }

    @Override
    // Check if the token is expired by comparing the expiration time with the current time
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    // Check if token is valid, include:
    //  + user extract from the jwt exists
    //  + is token expires
    //  + is the user extract from the jwt equals with the user get from userId
    @Override
    public boolean isTokenInRequestValid(HttpServletRequest request, User user) {
        return isTokenValid(extractJwtFromRequest(request), user);
    }

    @Override
    public String userFromJwtInRequest(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        return extractUsername(jwt);
    }
}
