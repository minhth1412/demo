package com.assessment.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
public class SignupRequest {

    @NotBlank(message = "First name cannot be blank")
    private String firstname;

    @NotBlank(message = "Last name cannot be blank")
    private String lastname;

    @NotBlank(message = "Email cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Invalid email format")
    @Size(max = 50, message = "Email must be less than 50 characters")
    //@UniqueEmail // Custom annotation for uniqueness validation
    private String email;

    @NotBlank(message = "Username cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Username must contain only letters and numbers")
    @Size(min = 3, message = "Username must have at least 3 characters")
    @Size(max = 20, message = "Username must have less than 20 characters")
    //@UniqueUsername //Custom annotation for uniqueness validation
    private String username;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 4, message = "Password must be at least 4 characters long")
    private String password;

    @NotBlank(message = "Repassword cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "Repassword must contain only letters and numbers")
    private String repassword;

    //..................

    private String role_id;

    // Nullable
    private LocalDate dateOfBirth;

    private String bio;

    private String image;
}
