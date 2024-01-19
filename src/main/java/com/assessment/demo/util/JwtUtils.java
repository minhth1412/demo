package com.assessment.demo.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import com.assessment.demo.entity.User;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;

@Component
@Slf4j
public class JwtUtils {
    @Value("${spring.jwt.secretKey}")
    private String jwtSecret;

    @Value("${spring.jwt.tokenLifespan}")
    private int tokenLifespan;
    @Value("${spring.jwt.refreshTokenLifespan}")
    private int refreshTokenLifespan;
    public String generateJwtToken(User user, Boolean isRefresh) {
        return generateTokenFromUsername(user.getUsername(), isRefresh);
    }

    public String generateTokenFromUsername(String username, Boolean isRefresh) {
        int time = tokenLifespan;
        if (isRefresh)
            time = refreshTokenLifespan;
        SecretKeySpec secret_key = new SecretKeySpec(jwtSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");

//        res.addHeader("token", token);
//        res.addHeader("userid", userdetails.getUserid());
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + time))
                .signWith(secret_key)
                .compact();
    }

    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser().setSigningKey(jwtSecret).parseClaimsJws(token).getBody().getSubject();
    }

    public boolean validateJwtToken(String authToken) {
        try {
            JwtParser parser = Jwts.parser();

            parser.setSigningKey(jwtSecret);
            parser.parseClaimsJws(authToken);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

}
