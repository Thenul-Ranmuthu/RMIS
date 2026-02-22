package com.rmis.rmis.exceptions;

public class RegisterUserAlreadyExistsException extends RuntimeException {
    public RegisterUserAlreadyExistsException(String message) {
        super(message);
    }

    public RegisterUserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}
