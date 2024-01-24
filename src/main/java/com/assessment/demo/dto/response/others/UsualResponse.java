package com.assessment.demo.dto.response.others;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

@Data
@Builder
public class UsualResponse {
    private HttpStatus exceptionType;
    private String message;

    public static UsualResponse init(HttpStatus exceptionType,String message) {
        return UsualResponse.builder()
                .exceptionType(exceptionType)
                .message(message)
                .build();
    }
}
