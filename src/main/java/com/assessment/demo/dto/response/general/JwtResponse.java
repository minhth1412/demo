package com.assessment.demo.dto.response.general;

import com.assessment.demo.entity.User;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class JwtResponse {
    String firstname;
    String lastname;
    String username;
    String email;
    String role;

    //------ testing
    UUID userId;
    String token;
    String refreshToken;

    public static JwtResponse fromUserWithToken(User user) {
        return JwtResponse.builder()
                .firstname(user.getFirst_name())
                .lastname(user.getLast_name())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().getRoleName())
                .userId(user.getUserId())
                .token(user.getToken().getTokenData())
                .refreshToken(user.getToken().getRefreshTokenData())
                .build();
    }

    public static JwtResponse fromUserWithoutToken(User user) {
        return JwtResponse.builder()
                .firstname(user.getFirst_name())
                .lastname(user.getLast_name())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().getRoleName())
                .userId(user.getUserId())
                .build();
    }

    public static JwtResponse msg(String msg) {
        return JwtResponse.builder().build();
    }
}
