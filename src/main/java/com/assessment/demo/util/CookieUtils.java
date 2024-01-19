package com.assessment.demo.util;

import com.assessment.demo.entity.User;
import com.assessment.demo.service.JwtService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class CookieUtils {
    @Value("${spring.jwt.tokenLifespan}")
    private static int tokenLifespan;
    @Value("${spring.jwt.refreshTokenLifespan}")
    private static int refreshTokenLifespan;
    public static void addRefreshTokenCookie(HttpServletResponse response, JwtService jwtService, User user) {
        // Create a cookie for the refresh token
        // Create an extra claims for the refresh token, that contain necessary info for later purpose
        // Extra claims now have:
        // - key: "email"
        // - val: email of the user that logging in (after the authentication phase)
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail());
        String token = jwtService.createToken(claims, user, refreshTokenLifespan);

        // NEED TO CHANGE THE NAME OF THE COOKIE LATER, READ MORE DOCS
        Cookie cookie = new Cookie("cookie1",user.getUsername());

        cookie.setAttribute("token", token);
        Date expirationDate = jwtService.extractExpiration(token);
        long timeDifference = (expirationDate.getTime() - (new Date()).getTime()) / 1000;
        // A secure cookie is the one which is only sent to the server over an encrypted HTTPS connection.
        // But for now, using localhost with http, I pass this
        //cookie.setSecure(true);
        cookie.setMaxAge((int) Math.max(0, timeDifference));
        cookie.setHttpOnly(true);   // prevent cross-site scripting (XSS) attacks and are not accessible via JavaScript's Document.cookie API
        cookie.setPath("/user");

        // Add refresh token cookie to the response
        response.addCookie(cookie);
    }

    public static void addTokenCookie(HttpServletResponse response,JwtService jwtService, User user) {
        // Create a cookie for the refresh token.
        String refreshToken = jwtService.generateToken(user);

        // NEED TO CHANGE THE NAME OF THE COOKIE LATER, READ MORE DOCS
        Cookie cookie = new Cookie("cookie2",user.getUsername());

        cookie.setAttribute("refreshToken", refreshToken);
        Date expirationDate = jwtService.extractExpiration(refreshToken);
        long timeDifference = (expirationDate.getTime() - (new Date()).getTime()) / 1000;

        cookie.setMaxAge((int) Math.max(0, timeDifference));
        cookie.setPath("/user");           // Create path that user can access to get the token after logging in
        cookie.setHttpOnly(true);
        // Add refresh token cookie to the response
        response.addCookie(cookie);
    }
}