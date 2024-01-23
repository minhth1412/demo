package com.assessment.demo.dto.response.admin;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
public class UserDto {
    private UUID userId;
    private String username;
    private String role;
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Boolean status;
    private Boolean isDeleted;
    private Boolean isOnline;
    private String bio;
    private String image;
    private Date dateOfBirth;
}
