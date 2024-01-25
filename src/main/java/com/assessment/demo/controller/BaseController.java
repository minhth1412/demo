package com.assessment.demo.controller;

import com.assessment.demo.dto.response.others.UsualResponse;
import org.springframework.http.ResponseEntity;

public abstract class BaseController {
    public static ResponseEntity<?> responseEntity(UsualResponse response) {
        if (response.getData() == null)
            return ResponseEntity.status(response.getStatus()).body(response.getMessage());
        else
            return ResponseEntity.status(response.getStatus()).body(response.getData());
    }
}
