package com.assessment.demo.dto.response;

import com.assessment.demo.entity.User;
import com.assessment.demo.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
public class JwtResponse {
    String firstname;
    String lastname;
    String username;
    String email;
    String role;
    //String token;

    public static JwtResponse fromUser(User user) {
        return JwtResponse.builder()
                .firstname(user.getFirst_name())
                .lastname(user.getLast_name())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().getRoleName())
                //.token(user.getToken())
                .build();
    }
}
