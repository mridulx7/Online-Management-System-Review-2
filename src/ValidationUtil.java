package com.eventmgmt.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility class for input validation across all servlets.
 * Provides centralized validation logic for email, dates, numeric values, and strings.
 */
public class ValidationUtil {

    // Email validation pattern (RFC 5322 simplified)
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );

    // SQL injection patterns to detect
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(--|;|/\\*|\\*/|xp_|sp_|exec|execute|select|insert|update|delete|drop|create|alter|union|script|javascript|<script|'.*')",
        Pattern.CASE_INSENSITIVE
    );

    private ValidationUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Validates email format against standard email pattern.
     * 
     * @param email The email address to validate
     * @return true if email format is valid, false otherwise
     */
    public static boolean validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Validates that a string value is not null or empty.
     * 
     * @param value The string value to validate
     * @param fieldName The name of the field being validated
     * @return Error message if validation fails, null if valid
     */
    public static String validateNotEmpty(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            return fieldName + " is required and cannot be empty";
        }
        return null;
    }

    /**
     * Validates and parses a positive integer from a string.
     * 
     * @param value The string value to parse
     * @param fieldName The name of the field being validated
     * @return The parsed integer value
     * @throws ValidationException if the value is not a valid positive integer
     */
    public static Integer validatePositiveInteger(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
        
        try {
            int intValue = Integer.parseInt(value.trim());
            if (intValue <= 0) {
                throw new ValidationException(fieldName + " must be a positive integer");
            }
            return intValue;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a valid integer");
        }
    }

    /**
     * Validates and parses a non-negative integer from a string.
     * 
     * @param value The string value to parse
     * @param fieldName The name of the field being validated
     * @return The parsed integer value
     * @throws ValidationException if the value is not a valid non-negative integer
     */
    public static Integer validateNonNegativeInteger(String value, String fieldName) throws ValidationException {
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(fieldName + " is required");
        }
        
        try {
            int intValue = Integer.parseInt(value.trim());
            if (intValue < 0) {
                throw new ValidationException(fieldName + " must be a non-negative integer");
            }
            return intValue;
        } catch (NumberFormatException e) {
            throw new ValidationException(fieldName + " must be a valid integer");
        }
    }

    /**
     * Validates and parses a date string in ISO format (yyyy-MM-dd).
     * 
     * @param dateString The date string to parse
     * @return The parsed LocalDate
     * @throws ValidationException if the date format is invalid
     */
    public static LocalDate validateDateFormat(String dateString) throws ValidationException {
        if (dateString == null || dateString.trim().isEmpty()) {
            throw new ValidationException("Date is required");
        }
        
        try {
            return LocalDate.parse(dateString.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
        } catch (DateTimeParseException e) {
            throw new ValidationException("Date must be in format yyyy-MM-dd");
        }
    }

    /**
     * Validates that a date is in the future.
     * 
     * @param date The date to validate
     * @return true if the date is in the future, false otherwise
     */
    public static boolean validateFutureDate(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.isAfter(LocalDate.now());
    }

    /**
     * Sanitizes input string to prevent SQL injection attacks.
     * Removes or escapes potentially dangerous characters and patterns.
     * 
     * @param input The input string to sanitize
     * @return The sanitized string
     */
    public static String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }
        
        // Trim whitespace
        String sanitized = input.trim();
        
        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(sanitized).find()) {
            // Remove dangerous characters including semicolons
            sanitized = sanitized.replaceAll("[';\"\\-\\-]", "");
            // Also remove standalone semicolons
            sanitized = sanitized.replace(";", "");
        }
        
        // Escape single quotes for SQL safety (double them)
        sanitized = sanitized.replace("'", "''");
        
        return sanitized;
    }

    /**
     * Validates password strength (basic implementation).
     * 
     * @param password The password to validate
     * @return true if password meets minimum requirements, false otherwise
     */
    public static boolean validatePasswordStrength(String password) {
        if (password == null || password.length() < 6) {
            return false;
        }
        return true;
    }

    /**
     * Validates all required fields for user creation/update.
     * 
     * @param name User name
     * @param email User email
     * @param password User password
     * @param role User role
     * @return List of validation error messages (empty if all valid)
     */
    public static List<String> validateUserData(String name, String email, String password, String role) {
        List<String> errors = new ArrayList<>();
        
        String nameError = validateNotEmpty(name, "Name");
        if (nameError != null) {
            errors.add(nameError);
        }
        
        String emailError = validateNotEmpty(email, "Email");
        if (emailError != null) {
            errors.add(emailError);
        } else if (!validateEmail(email)) {
            errors.add("Invalid email format");
        }
        
        String passwordError = validateNotEmpty(password, "Password");
        if (passwordError != null) {
            errors.add(passwordError);
        } else if (!validatePasswordStrength(password)) {
            errors.add("Password must be at least 6 characters long");
        }
        
        String roleError = validateNotEmpty(role, "Role");
        if (roleError != null) {
            errors.add(roleError);
        }
        
        return errors;
    }

    /**
     * Validates all required fields for event creation/update.
     * 
     * @param title Event title
     * @param date Event date
     * @param venue Event venue
     * @return List of validation error messages (empty if all valid)
     */
    public static List<String> validateEventData(String title, String date, String venue) {
        List<String> errors = new ArrayList<>();
        
        String titleError = validateNotEmpty(title, "Title");
        if (titleError != null) {
            errors.add(titleError);
        }
        
        String dateError = validateNotEmpty(date, "Date");
        if (dateError != null) {
            errors.add(dateError);
        } else {
            try {
                LocalDate eventDate = validateDateFormat(date);
                if (!validateFutureDate(eventDate)) {
                    errors.add("Event date must be in the future");
                }
            } catch (ValidationException e) {
                errors.add(e.getMessage());
            }
        }
        
        String venueError = validateNotEmpty(venue, "Venue");
        if (venueError != null) {
            errors.add(venueError);
        }
        
        return errors;
    }
}
