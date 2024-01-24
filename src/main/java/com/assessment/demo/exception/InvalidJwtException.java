package com.assessment.demo.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidJwtException extends AuthenticationException {

    public InvalidJwtException(String message) {
        super(message);
    }
}
