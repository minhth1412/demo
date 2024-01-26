package com.assessment.demo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetPasswordRequest {

    @NotBlank(message = "This field cannot be blank")
    private String oldPassword;

    @NotBlank(message = "This field cannot be blank")
    @Size(min = 4, message = "Password must be at least 4 characters long")
    private String newPassword;

    @NotBlank(message = "This field cannot be blank")
    private String confirmNewPassword;
}
