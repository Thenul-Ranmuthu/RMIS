package com.rmis.rmis.exceptions;

public class QuotaRequestNotFoundException extends RuntimeException {
    public QuotaRequestNotFoundException(String message) {
        super(message);
    }
}
