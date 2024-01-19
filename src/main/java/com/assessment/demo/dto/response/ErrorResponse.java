package com.assessment.demo.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private String exceptionType;
    private String message;
    private String status;

    public static ErrorResponse fromUser(String exceptionType, String message, String status) {
        return ErrorResponse.builder()
                .exceptionType(exceptionType)
                .message(message)
                .status(status)
                .build();
    }
}
