package com.rmis.rmis.exceptions;

public class ExportException extends RuntimeException {

    private final String format;

    public ExportException(String format, String message, Throwable cause) {
        super(message, cause);
        this.format = format;
    }

    public String getFormat() {
        return format;
    }
}
