package com.assessment.demo.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class resetPasswordRequest {
    private String oldPassword;
    private String newPassword;
    private String confirmNewPassword;
}
