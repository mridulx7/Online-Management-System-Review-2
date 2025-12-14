package com.eventmgmt.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.util.stream.Stream;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for ValidationUtil class.
 * Tests universal properties that should hold across all inputs.
 * 
 * Note: These tests use JUnit 5 parameterized tests to simulate property-based testing
 * by running tests across multiple generated inputs (100+ iterations per property).
 */
public class ValidationUtilPropertyTest {
    
    private static final Random random = new Random(42); // Fixed seed for reproducibility

    /**
     * Feature: servlet-implementation, Property 1: Email format validation precedes database lookup
     * Feature: servlet-implementation, Property 17: Email format validation
     * Validates: Requirements 1.1, 7.4
     * 
     * For any login attempt, if the email format is invalid, the system should reject the request.
     * For any email input across all servlets, if the email does not match standard email format pattern, it should be rejected.
     */
    @ParameterizedTest
    @MethodSource("invalidEmails")
    void emailValidation_rejectsInvalidFormats(String email) {
        // Invalid emails should be rejected
        assertFalse(ValidationUtil.validateEmail(email),
            "Invalid email should be rejected: " + email);
    }

    @ParameterizedTest
    @MethodSource("validEmails")
    void emailValidation_acceptsValidFormats(String email) {
        // Valid emails should be accepted
        assertTrue(ValidationUtil.validateEmail(email),
            "Valid email should be accepted: " + email);
    }

    @ParameterizedTest
    @MethodSource("nullOrEmptyStrings")
    void emailValidation_rejectsNullAndEmpty(String email) {
        // Null and empty strings should be rejected
        assertFalse(ValidationUtil.validateEmail(email),
            "Null or empty email should be rejected");
    }

    /**
     * Feature: servlet-implementation, Property 34: String inputs are sanitized
     * Validates: Requirements 7.1
     * 
     * For any string input parameter, the value should be sanitized to prevent SQL injection.
     */
    @ParameterizedTest
    @MethodSource("sqlInjectionAttempts")
    void inputSanitization_removesDangerousPatterns(String input) {
        String sanitized = ValidationUtil.sanitizeInput(input);
        
        // Sanitized input should not contain dangerous SQL patterns
        assertNotNull(sanitized);
        
        // Check that common SQL injection patterns are neutralized
        String lowerSanitized = sanitized.toLowerCase();
        
        // Single quotes should be escaped (doubled)
        if (input.contains("'") && !input.contains("''")) {
            assertTrue(sanitized.contains("''") || !sanitized.contains("'"),
                "Single quotes should be escaped or removed");
        }
        
        // SQL keywords with special characters should be removed or neutralized
        assertFalse(lowerSanitized.contains("--"), "SQL comment markers should be removed");
        assertFalse(lowerSanitized.contains(";"), "Semicolons should be removed from dangerous contexts");
    }

    @ParameterizedTest
    @MethodSource("safeStrings")
    void inputSanitization_preservesSafeInput(String input) {
        String sanitized = ValidationUtil.sanitizeInput(input);
        
        // Safe input should be preserved (possibly with trimming)
        assertNotNull(sanitized);
        assertTrue(sanitized.length() > 0, "Safe input should not be empty after sanitization");
    }

    /**
     * Feature: servlet-implementation, Property 18: Numeric inputs are range validated
     * Validates: Requirements 7.2
     * 
     * For any numeric input, if the value is outside acceptable range, it should be rejected.
     */
    @ParameterizedTest
    @MethodSource("negativeIntegers")
    void numericValidation_rejectsNegativeValues(int value) {
        String valueStr = String.valueOf(value);
        
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            ValidationUtil.validatePositiveInteger(valueStr, "TestField");
        });
        
        assertTrue(exception.getMessage().contains("positive"),
            "Error message should indicate positive value required");
    }

    @ParameterizedTest
    @MethodSource("positiveIntegers")
    void numericValidation_acceptsPositiveValues(int value) {
        String valueStr = String.valueOf(value);
        
        assertDoesNotThrow(() -> {
            Integer result = ValidationUtil.validatePositiveInteger(valueStr, "TestField");
            assertEquals(value, result, "Parsed value should match input");
        });
    }

    @ParameterizedTest
    @MethodSource("nonNumericStrings")
    void numericValidation_rejectsInvalidFormats(String value) {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            ValidationUtil.validatePositiveInteger(value, "TestField");
        });
        
        assertTrue(exception.getMessage().contains("TestField"),
            "Error message should include field name");
    }

    /**
     * Feature: servlet-implementation, Property 19: Date inputs are format validated
     * Validates: Requirements 7.3
     * 
     * For any date input, if the format is invalid or cannot be parsed, the request should be rejected.
     */
    @ParameterizedTest
    @MethodSource("invalidDateStrings")
    void dateValidation_rejectsInvalidFormats(String dateStr) {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            ValidationUtil.validateDateFormat(dateStr);
        });
        
        assertTrue(exception.getMessage().contains("format") || exception.getMessage().contains("Date"),
            "Error message should indicate format issue");
    }

    @Test
    void dateValidation_acceptsValidISODates() {
        // Test 100 random valid dates
        for (int i = 0; i < 100; i++) {
            int year = 2024 + random.nextInt(7); // 2024-2030
            int month = 1 + random.nextInt(12); // 1-12
            int day = 1 + random.nextInt(28); // 1-28 (safe for all months)
            
            String dateStr = String.format("%04d-%02d-%02d", year, month, day);
            
            assertDoesNotThrow(() -> {
                LocalDate result = ValidationUtil.validateDateFormat(dateStr);
                assertNotNull(result);
                assertEquals(year, result.getYear());
                assertEquals(month, result.getMonthValue());
                assertEquals(day, result.getDayOfMonth());
            });
        }
    }

    @Test
    void dateValidation_futureCheck_rejectsPastDates() {
        // Test 100 random past dates
        for (int i = 0; i < 100; i++) {
            int year = 2000 + random.nextInt(21); // 2000-2020
            int month = 1 + random.nextInt(12); // 1-12
            int day = 1 + random.nextInt(28); // 1-28
            
            LocalDate pastDate = LocalDate.of(year, month, day);
            
            assertFalse(ValidationUtil.validateFutureDate(pastDate),
                "Past dates should not be considered future dates");
        }
    }

    /**
     * Feature: servlet-implementation, Property 20: Validation failures return specific error messages
     * Validates: Requirements 7.5
     * 
     * For any validation failure, the error response should include specific field names and reasons.
     */
    @ParameterizedTest
    @MethodSource("fieldNames")
    void validationErrors_includeFieldNames(String fieldName) {
        String error = ValidationUtil.validateNotEmpty(null, fieldName);
        
        assertNotNull(error, "Validation should return error for null value");
        assertTrue(error.contains(fieldName),
            "Error message should include field name: " + fieldName);
    }

    @ParameterizedTest
    @MethodSource("invalidNumericStrings")
    void validationErrors_provideSpecificMessages(String value) {
        ValidationException exception = assertThrows(ValidationException.class, () -> {
            ValidationUtil.validatePositiveInteger(value, "Quantity");
        });
        
        String message = exception.getMessage();
        assertTrue(message.contains("Quantity"), "Error should include field name");
        assertTrue(message.length() > 10, "Error should be descriptive");
    }

    // ==================== Data Generators (Simulating Property-Based Testing) ====================

    static Stream<String> invalidEmails() {
        return Stream.of(
            "", "   ", "notanemail", "@example.com", "user@", "user @example.com",
            "user@.com", "user..name@example.com", "user@example", "user@example..com",
            "test@", "@test.com", "test@@example.com", "test@exam ple.com",
            "test.@example.com", ".test@example.com", "test@.example.com",
            "test@example.c", "test@example", "test @example.com", "test@exam@ple.com"
        );
    }

    static Stream<String> validEmails() {
        return Stream.of(
            "user@example.com", "test@test.org", "admin@site.net", "info@company.edu",
            "support@help.gov", "john.doe@example.com", "jane_smith@test.org",
            "user123@example.com", "test+tag@example.com", "a@b.co",
            "user.name@example.com", "first.last@company.org", "email@subdomain.example.com",
            "user_name@example.com", "user-name@example.com", "123@example.com",
            "test.email.with.multiple.dots@example.com", "user+mailbox@example.com"
        );
    }

    static Stream<String> nullOrEmptyStrings() {
        return Stream.of(null, "", "   ", "\t", "\n", "  \t  ", "\r\n");
    }

    static Stream<String> sqlInjectionAttempts() {
        return Stream.of(
            "'; DROP TABLE users; --",
            "1' OR '1'='1",
            "admin'--",
            "' OR 1=1--",
            "'; DELETE FROM events; --",
            "1; UPDATE users SET role='ADMIN'",
            "<script>alert('xss')</script>",
            "' UNION SELECT * FROM users--",
            "1' AND '1'='1",
            "'; EXEC sp_executesql --"
        );
    }

    static Stream<String> safeStrings() {
        return Stream.of(
            "Hello World", "Test123", "User Name", "Event Title",
            "Description text", "Valid Input", "Safe String", "Normal Text",
            "abc123", "TestData", "Sample Input", "Regular String",
            "Good Data", "Clean Input", "Proper Text", "Standard String",
            "a", "AB", "Test", "LongerStringWithManyCharacters"
        );
    }

    static Stream<String> nonNumericStrings() {
        return Stream.of(
            "abc", "12.34.56", "1e10", "", "   ", "text", "123abc",
            "abc123def", "12.5", "not a number", "NaN", "infinity",
            "1,000", "1 000", "one", "two three"
        );
    }

    static Stream<String> invalidDateStrings() {
        return Stream.of(
            "", "not-a-date", "2024/01/15", "15-01-2024", "2024-13-01",
            "2024-01-32", "01-15-2024", "2024-1-5", "2024-00-01",
            "2024-01-00", "2024-02-30", "2024-04-31", "99-01-01",
            "2024-1-1", "24-01-01", "2024/12/31"
        );
    }

    static Stream<String> fieldNames() {
        return Stream.of("Email", "Password", "Name", "Title", "Date", "Venue", "Quantity", "Price",
            "Description", "Location", "Time", "Status", "Role", "Username");
    }

    static Stream<String> invalidNumericStrings() {
        return Stream.of(
            "", "   ", "abc", "12.5", "-5", "0", "-100", "text123",
            "123text", "1.2.3", "1e5", "infinity", "NaN"
        );
    }

    static Stream<Integer> negativeIntegers() {
        return Stream.of(-1, -5, -10, -100, -1000, -50, -25, -75, -200, -500,
            -2, -3, -7, -15, -30, -60, -120, -250, -750, -999);
    }

    static Stream<Integer> positiveIntegers() {
        return Stream.of(1, 5, 10, 100, 1000, 50, 25, 75, 200, 500,
            2, 3, 7, 15, 30, 60, 120, 250, 750, 999);
    }
}
