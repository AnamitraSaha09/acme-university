package com.acme.university.exception;

/**
 * Thrown when incorrect data is passed for existing resource.
 * Mapped to HTTP 400 by {@link GlobalExceptionHandler}.
 */
public class DataMismatchException extends RuntimeException {
    public DataMismatchException(String message) {
        super(message);
    }
}
