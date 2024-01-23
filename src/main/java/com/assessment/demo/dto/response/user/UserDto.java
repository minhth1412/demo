package com.assessment.demo.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserDto {
    private String username;
    private UUID userId;
}
