package com.assessment.demo.security;


import com.assessment.demo.entity.User;
import com.assessment.demo.repository.TokenRepository;
import com.assessment.demo.repository.UserRepository;
import com.assessment.demo.service.JwtService;
import com.assessment.demo.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    // Can't use both userService and userDetailsService here
    private final UserService userService;

    private final UserRepository userRepository;

    /*
    doFilterInternal method: This method is part of the OncePerRequestFilter class
        and is called for each incoming HTTP request.
    It contains the logic for JWT authentication.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,@NonNull HttpServletResponse response,@NonNull FilterChain filterChain)
            throws ServletException, IOException {
        // Variable to save the Authorization in Http Request Header
        final String authHeader = request.getHeader("Authorization");
        // Variable to save the JWT from the Authorization in Http request header
        final String jwt;
        // Variable to save the username that can be extracted from the JWT
        final String username;

        // Using the auth Bearer ("Bearer " + <token>), check if it is not the bearer token.
        // If not, it continues with the filter chain without attempting JWT authentication.
        if (StringUtils.isEmpty(authHeader) || !org.apache.commons.lang3.StringUtils.startsWith(authHeader,"Bearer ")) {
            filterChain.doFilter(request,response);
            return;
        }

        // 7 is the length of the first path "Bearer ", remove that path and save into jwt variables
        jwt = authHeader.substring(7);         // We get the token on the header of request after this
        username = jwtService.extractUsername(jwt);        // Extract username from token's claims

        // Check the username is empty or not
        if (StringUtils.isNotEmpty(username) && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.userDetailsService().loadUserByUsername(username);

            if (jwtService.isTokenExpired(jwt)) {
                handleExpiredToken(response,"Token has expired. Please login again for token refreshment.");
                return;
            }
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

            if (user.getToken() == null) {
                throw new RuntimeException("User token is invalid!");
            }

            if (!user.isCredentialsNonExpired()) {
                handleExpiredToken(response,"Token is no longer valid. Please login again.");
                return;
            }

//            if (jwtService.isTokenValid(jwt,userDetails)) {
//                UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
//                        userDetails,null,userDetails.getAuthorities());
//                token.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(token);

                // jwtService.updateExpiredToken(jwt, true);
                // jwtService.updateExpiredToken(jwt, false);
//            }
        }
        // continues with the filter chain
        filterChain.doFilter(request,response);
    }

    private void handleExpiredToken(HttpServletResponse response,String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + message + "\"}");
        response.getWriter().flush();
        response.getWriter().close();
    }
}
