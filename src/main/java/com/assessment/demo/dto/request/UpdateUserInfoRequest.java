package com.assessment.demo.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserInfoRequest {
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Username must contain only letters and numbers")
    private String username;

    @Pattern(regexp = "^[a-zA-Z]+$", message = "Your first name must contain only letters")
    String firstname;

    @Pattern(regexp = "^[a-zA-Z]+$", message = "Your last name must contain only letters")
    String lastname;

    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[a-zA-Z]).{8,}$", message = "Wrong email type")
    String email;

    private String bio;

    private String image;

    private LocalDate dateOfBirth;
}
