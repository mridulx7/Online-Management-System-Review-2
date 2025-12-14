package com.eventmgmt.util;

/**
 * Custom exception for validation errors.
 * Thrown when input validation fails.
 */
public class ValidationException extends Exception {
    
    public ValidationException(String message) {
        super(message);
    }
    
    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}
