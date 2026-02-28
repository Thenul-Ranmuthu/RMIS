package com.rmis.rmis.exceptions;

public class UnregisteredUserException extends RuntimeException {
    public UnregisteredUserException(String message) {
        super(message);
    }
}
