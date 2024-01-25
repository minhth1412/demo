package com.assessment.demo.service.impl;

import com.assessment.demo.entity.Token;
import com.assessment.demo.entity.User;
import com.assessment.demo.exception.InvalidJwtException;
import com.assessment.demo.repository.TokenRepository;
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

    public String extractJwtFromRequest(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        if (org.apache.commons.lang3.StringUtils.isEmpty(authHeader) ||
                !org.apache.commons.lang3.StringUtils.startsWith(authHeader,"Bearer ")) {
            String errorMessage = "The token is not in the Bearer token format!";
            log.info(errorMessage);
            throw new InvalidJwtException(errorMessage);
        }
        // Extract and return the token from the Authorization header
        return authHeader.substring(7);
    }

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

    @Override
    // Extract the username claim from the JWT, it is placed at the subject of the token
    public String extractUsername(String token) {
        return extractClaim(token,Claims::getSubject);
    }

    @Override
    public Date extractExpiration(String token) {
        return extractClaim(token,Claims::getExpiration);
    }

    // Check if the token is valid by comparing the username and verifying expiration
    @Override
    public boolean isTokenValid(String token,UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(token);
            String username = extractUsername(token);
            if (!username.equals(userDetails.getUsername())){
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
        }catch (Exception e) {
            log.error("An unexpected error occurred during JWT validation: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public void refreshToken(User user, Boolean isResetTime) {
        String token = generateToken(user,false);
        String refreshToken = generateToken(user,true);
        // Extract the time expired of tokens
        Date tkTime = extractExpiration(token);
        Date refreshTkTime = extractExpiration(refreshToken);

        Token oldToken = tokenRepository.findByTokenId(user.getToken().getTokenId()).orElse(null);
        if (oldToken != null) {
            oldToken.updateToken(token,refreshToken,tkTime,refreshTkTime);
        } else {
            throw new RuntimeException("There is no token to refresh!");
        }
        if (isResetTime) {
            updateExpiredToken(oldToken.getCompressedTokenData(),false);
            updateExpiredToken(oldToken.getCompressedRefreshTokenData(),true);
        }
        // Save changes
        tokenRepository.save(oldToken);
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

    @Override
    // Check if the token is expired by comparing the expiration time with the current time
    public boolean isTokenExpired(String token) {
        return extractClaim(token,Claims::getExpiration).before(new Date());
    }

    // Check if token is valid, include:
    //  + user extract from the jwt exists
    //  + is token expires
    //  + is the user extract from the jwt equals with the user get from userId
    @Override
    public boolean isTokenInRequestValid(HttpServletRequest request,User user) {
        return isTokenValid(extractJwtFromRequest(request),user);
    }

    @Override
    public String userFromJwtInRequest(HttpServletRequest request) {
        String jwt = extractJwtFromRequest(request);
        return extractUsername(jwt);
    }
}
