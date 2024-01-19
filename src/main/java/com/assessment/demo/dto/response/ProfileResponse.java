package com.assessment.demo.dto.response;

import com.assessment.demo.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ProfileResponse {
    // Basic information
    String bio;
    LocalDate dateOfBirth;
    String image;
    String username;

    // Add-up later information
    // Call get all posts of current user.

    public static ProfileResponse fromUser(User user) {
        return ProfileResponse.builder()
                .bio(user.getBio())
                .dateOfBirth(user.getDateOfBirth())
                .username(user.getUsername())
                .image(user.getImage())
                .build();
    }
}
