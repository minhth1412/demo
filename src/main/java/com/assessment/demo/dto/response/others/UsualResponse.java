package com.assessment.demo.dto.response.others;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class UsualResponse {
    private HttpStatus status;
    private String message;

    // Additional field for more information
    private Object data;

    // Private constructor to enforce the use of the builder
    private UsualResponse(HttpStatus status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Public static methods for creating instances
    public static UsualResponse success(String message) {
        return UsualResponse.builder()
                .status(HttpStatus.OK)
                .message(message)
                .data(null)
                .build();
    }

    public static UsualResponse success(JwtResponse data) {
        return UsualResponse.builder()
                .status(HttpStatus.OK)
                .data(data)
                .message(null)
                .build();
    }

    public static UsualResponse success(String message, Object data) {
        return UsualResponse.builder()
                .status(HttpStatus.OK)
                .message(message)
                .data(data)
                .build();
    }

    public static UsualResponse error(HttpStatus status, String message) {
        return UsualResponse.builder()
                .status(status)
                .message(message)
                .data(null)
                .build();
    }

    public static UsualResponse error(HttpStatus status, String message, Object data) {
        return UsualResponse.builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
    }
}
