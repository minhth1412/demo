package com.assessment.demo.dto.response;

import com.assessment.demo.entity.User;
import com.assessment.demo.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    String msg;

    public static JwtResponse fromUser(User user) {
        return JwtResponse.builder()
                .firstname(user.getFirst_name())
                .lastname(user.getLast_name())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().getRoleName())
                .userId(user.getUserId())
                .token(user.getToken().getCompressedTokenData())
                .refreshToken(user.getToken().getCompressedRefreshTokenData())
                .msg(null)
                .build();
    }

    public static JwtResponse msg(String msg) {
        return JwtResponse.builder().msg(msg).build();
    }
}
