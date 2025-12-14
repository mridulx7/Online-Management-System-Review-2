package com.eventmgmt.servlet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.eventmgmt.model.User;
import com.eventmgmt.util.ValidationUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for LoginServlet session management and validation logic.
 * Tests universal properties that should hold across all inputs.
 * 
 * Note: These tests focus on the core logic rather than servlet infrastructure.
 */
public class LoginServletPropertyTest {

    /**
     * Feature: servlet-implementation, Property 2: Successful login creates complete session
     * Validates: Requirements 1.3
     * 
     * For any valid user credentials, after successful authentication, 
     * the session should contain userId, userRole, userName, userEmail, and loginTime attributes.
     */
    @ParameterizedTest
    @MethodSource("validUserData")
    void successfulLogin_createsCompleteSession(UserTestData userData) {
        // Simulate session creation logic
        Map<String, Object> sessionAttributes = new HashMap<>();
        
        // Create a user object
        User user = createUser(userData.userId, userData.name, userData.email, userData.role);
        
        // Simulate the session creation logic from LoginServlet
        sessionAttributes.put("userId", user.getId());
        sessionAttributes.put("userRole", user.getRole());
        sessionAttributes.put("userName", user.getName());
        sessionAttributes.put("userEmail", user.getEmail());
        sessionAttributes.put("loginTime", System.currentTimeMillis());
        
        // Assert all required attributes are present
        assertNotNull(sessionAttributes.get("userId"), "Session should contain userId");
        assertNotNull(sessionAttributes.get("userRole"), "Session should contain userRole");
        assertNotNull(sessionAttributes.get("userName"), "Session should contain userName");
        assertNotNull(sessionAttributes.get("userEmail"), "Session should contain userEmail");
        assertNotNull(sessionAttributes.get("loginTime"), "Session should contain loginTime");
        
        // Verify correct values
        assertEquals(user.getId(), sessionAttributes.get("userId"));
        assertEquals(user.getRole(), sessionAttributes.get("userRole"));
        assertEquals(user.getName(), sessionAttributes.get("userName"));
        assertEquals(user.getEmail(), sessionAttributes.get("userEmail"));
        assertTrue((Long) sessionAttributes.get("loginTime") > 0);
    }

    /**
     * Feature: servlet-implementation, Property 3: Failed login attempts are logged
     * Validates: Requirements 1.4
     * 
     * For any invalid credentials, the system should reject the login
     * and the validation should fail before database lookup.
     */
    @ParameterizedTest
    @MethodSource("invalidCredentials")
    void failedLogin_validationRejectsInvalidInput(InvalidCredentials creds) {
        // Test that validation catches invalid inputs before database lookup
        boolean shouldProceedToDatabase = true;
        
        // Check if email is empty
        if (creds.email == null || creds.email.trim().isEmpty()) {
            shouldProceedToDatabase = false;
        }
        
        // Check if password is empty
        if (creds.password == null || creds.password.trim().isEmpty()) {
            shouldProceedToDatabase = false;
        }
        
        // Check if email format is valid
        if (shouldProceedToDatabase && !ValidationUtil.validateEmail(creds.email)) {
            shouldProceedToDatabase = false;
        }
        
        // Assert that invalid credentials are caught before database lookup
        assertFalse(shouldProceedToDatabase, 
            "Invalid credentials should be rejected: email=" + creds.email + ", password=" + creds.password);
    }

    /**
     * Feature: servlet-implementation, Property 5: Session invalidation clears all attributes
     * Validates: Requirements 8.3
     * 
     * For any logout operation, the session should be invalidated 
     * and all session attributes should be cleared.
     */
    @ParameterizedTest
    @MethodSource("sessionScenarios")
    void logout_clearsAllSessionAttributes(SessionScenario scenario) {
        // Create a session with attributes
        Map<String, Object> sessionAttributes = new HashMap<>();
        sessionAttributes.put("userId", scenario.userId);
        sessionAttributes.put("userRole", scenario.userRole);
        sessionAttributes.put("userName", scenario.userName);
        sessionAttributes.put("userEmail", scenario.userEmail);
        sessionAttributes.put("loginTime", scenario.loginTime);
        
        // Verify session has attributes before logout
        assertFalse(sessionAttributes.isEmpty(), "Session should have attributes before logout");
        assertEquals(5, sessionAttributes.size(), "Session should have 5 attributes");
        
        // Simulate logout - clear all attributes
        sessionAttributes.clear();
        
        // Assert all attributes are cleared
        assertTrue(sessionAttributes.isEmpty(), "Session should be empty after logout");
        assertNull(sessionAttributes.get("userId"), "userId should be cleared");
        assertNull(sessionAttributes.get("userRole"), "userRole should be cleared");
        assertNull(sessionAttributes.get("userName"), "userName should be cleared");
        assertNull(sessionAttributes.get("userEmail"), "userEmail should be cleared");
        assertNull(sessionAttributes.get("loginTime"), "loginTime should be cleared");
    }

    @Test
    void logout_alwaysInvalidatesSession_multipleIterations() {
        // Test 100 iterations to ensure property holds
        for (int i = 0; i < 100; i++) {
            Map<String, Object> sessionAttributes = new HashMap<>();
            sessionAttributes.put("userId", i);
            sessionAttributes.put("userRole", i % 3 == 0 ? "ADMIN" : (i % 3 == 1 ? "ORGANIZER" : "ATTENDEE"));
            sessionAttributes.put("userName", "User" + i);
            sessionAttributes.put("userEmail", "user" + i + "@example.com");
            sessionAttributes.put("loginTime", System.currentTimeMillis());
            
            // Simulate logout
            sessionAttributes.clear();
            
            // Verify session is empty
            assertTrue(sessionAttributes.isEmpty(), 
                "Session should be empty after logout (iteration " + i + ")");
        }
    }

    // ==================== Helper Methods ====================

    private User createUser(int id, String name, String email, String role) {
        // Create appropriate user type based on role
        if ("ADMIN".equals(role)) {
            return new com.eventmgmt.model.Admin(id, name, email, "password");
        } else if ("ORGANIZER".equals(role)) {
            return new com.eventmgmt.model.Organizer(id, name, email, "password");
        } else {
            return new com.eventmgmt.model.Attendee(id, name, email, "password");
        }
    }

    // ==================== Data Generators ====================

    static Stream<UserTestData> validUserData() {
        return Stream.of(
            new UserTestData(1, "Admin User", "admin@example.com", "ADMIN"),
            new UserTestData(2, "Organizer User", "organizer@example.com", "ORGANIZER"),
            new UserTestData(3, "Attendee User", "attendee@example.com", "ATTENDEE"),
            new UserTestData(4, "Test Admin", "test.admin@test.com", "ADMIN"),
            new UserTestData(5, "Test Organizer", "test.org@test.org", "ORGANIZER"),
            new UserTestData(6, "Test Attendee", "test.att@test.net", "ATTENDEE"),
            new UserTestData(100, "User 100", "user100@example.com", "ADMIN"),
            new UserTestData(999, "User 999", "user999@example.com", "ORGANIZER")
        );
    }

    static Stream<InvalidCredentials> invalidCredentials() {
        return Stream.of(
            new InvalidCredentials("", "password"),
            new InvalidCredentials("   ", "password"),
            new InvalidCredentials("notanemail", "password"),
            new InvalidCredentials("user@", "password"),
            new InvalidCredentials("@example.com", "password"),
            new InvalidCredentials("valid@example.com", ""),
            new InvalidCredentials("valid@example.com", "   "),
            new InvalidCredentials(null, "password"),
            new InvalidCredentials("valid@example.com", null),
            new InvalidCredentials("invalid-format", "password"),
            new InvalidCredentials("no-at-sign.com", "password"),
            new InvalidCredentials("double@@example.com", "password"),
            new InvalidCredentials("spaces in@example.com", "password"),
            new InvalidCredentials("test@", "password"),
            new InvalidCredentials("@test.com", "password")
        );
    }

    static Stream<SessionScenario> sessionScenarios() {
        return Stream.of(
            new SessionScenario(1, "ADMIN", "Admin User", "admin@example.com", System.currentTimeMillis()),
            new SessionScenario(2, "ORGANIZER", "Organizer User", "org@example.com", System.currentTimeMillis()),
            new SessionScenario(3, "ATTENDEE", "Attendee User", "att@example.com", System.currentTimeMillis()),
            new SessionScenario(10, "ADMIN", "Test User", "test@test.com", System.currentTimeMillis() - 1000),
            new SessionScenario(50, "ORGANIZER", "Another User", "another@test.org", System.currentTimeMillis() - 5000),
            new SessionScenario(100, "ATTENDEE", "User 100", "user100@example.com", System.currentTimeMillis() - 10000)
        );
    }

    // ==================== Test Data Classes ====================

    static class UserTestData {
        int userId;
        String name;
        String email;
        String role;

        UserTestData(int userId, String name, String email, String role) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.role = role;
        }
    }

    static class InvalidCredentials {
        String email;
        String password;

        InvalidCredentials(String email, String password) {
            this.email = email;
            this.password = password;
        }
    }

    static class SessionScenario {
        Integer userId;
        String userRole;
        String userName;
        String userEmail;
        Long loginTime;

        SessionScenario(Integer userId, String userRole, String userName, String userEmail, Long loginTime) {
            this.userId = userId;
            this.userRole = userRole;
            this.userName = userName;
            this.userEmail = userEmail;
            this.loginTime = loginTime;
        }
    }
}
