package com.acme.university.exception;

/**
 * Thrown when attempting to create a resource (lecturer or student) that
 * already exists. Mapped to HTTP 409 by {@link GlobalExceptionHandler}.
 */
public class ResourceAlreadyExistsException extends RuntimeException {

    public ResourceAlreadyExistsException(String message) {
        super(message);
    }
}
