package com.assessment.demo.exception;

import org.springframework.security.core.AuthenticationException;

public class UserNotExistException extends AuthenticationException {

    public UserNotExistException(String message) {
        super(message);
    }
}
