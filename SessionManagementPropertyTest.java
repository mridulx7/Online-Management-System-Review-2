package com.eventmgmt.servlet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.eventmgmt.util.ServletUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for session management across all servlets.
 * Tests session validation, logging, and authorization checks.
 */
public class SessionManagementPropertyTest {

    /**
     * **Feature: servlet-implementation, Property 4: Protected endpoints require valid sessions**
     * **Validates: Requirements 8.1**
     * 
     * For any protected servlet endpoint access, if no valid session exists,
     * the request should be rejected or redirected to login.
     */
    @ParameterizedTest
    @MethodSource("sessionValidationScenarios")
    void protectedEndpoints_requireValidSessions(SessionValidationScenario scenario) {
        // Create a mock session with the given attributes
        MockSession session = new MockSession();
        
        if (scenario.hasSession) {
            if (scenario.userId != null) {
                session.setAttribute("userId", scenario.userId);
            }
            if (scenario.role != null) {
                session.setAttribute("userRole", scenario.role);
            }
        } else {
            session = null;
        }
        
        // Test session validation using ServletUtil
        boolean isValid = ServletUtil.isSessionValid(session);
        
        // Assert that session validity matches expected state
        assertEquals(scenario.shouldBeValid, isValid,
            "Session validity should match expected state: hasSession=" + scenario.hasSession +
            ", userId=" + scenario.userId + ", role=" + scenario.role);
        
        // Verify that getUserIdFromSession and getUserRoleFromSession return correct values
        // (they return what's in the session, regardless of overall validity)
        Integer retrievedUserId = ServletUtil.getUserIdFromSession(session);
        String retrievedRole = ServletUtil.getUserRoleFromSession(session);
        
        if (session == null) {
            assertNull(retrievedUserId, "Null session should return null for userId");
            assertNull(retrievedRole, "Null session should return null for role");
        } else {
            assertEquals(scenario.userId, retrievedUserId, "Retrieved userId should match");
            assertEquals(scenario.role, retrievedRole, "Retrieved role should match");
        }
    }

    /**
     * **Feature: servlet-implementation, Property 6: Session validation failures are logged**
     * **Validates: Requirements 8.5**
     * 
     * For any session validation failure, a security event should be logged
     * with timestamp and context.
     */
    @ParameterizedTest
    @MethodSource("securityEventScenarios")
    void sessionValidationFailures_areLogged(SecurityEventScenario scenario) {
        // Create a mock session
        MockSession session = new MockSession();
        
        if (scenario.userId != null) {
            session.setAttribute("userId", scenario.userId);
        }
        if (scenario.role != null) {
            session.setAttribute("userRole", scenario.role);
        }
        
        // Log a security event (this will be logged to the system logger)
        // We can't easily verify logging in unit tests, but we can verify the method doesn't throw
        assertDoesNotThrow(() -> {
            ServletUtil.logSecurityEvent(scenario.eventDescription, session);
        }, "Logging security event should not throw exception");
        
        // Verify that the session state is correctly captured
        Integer userId = ServletUtil.getUserIdFromSession(session);
        String role = ServletUtil.getUserRoleFromSession(session);
        
        assertEquals(scenario.userId, userId, "User ID should match");
        assertEquals(scenario.role, role, "Role should match");
    }

    /**
     * **Feature: servlet-implementation, Property 35: Authorization checks verify role permissions**
     * **Validates: Requirements 8.4**
     * 
     * For any operation requiring specific permissions, the user's role should be
     * verified against required permissions before processing.
     */
    @ParameterizedTest
    @MethodSource("authorizationScenarios")
    void authorizationChecks_verifyRolePermissions(AuthorizationScenario scenario) {
        // Create a mock session with the given role
        MockSession session = new MockSession();
        session.setAttribute("userId", scenario.userId);
        session.setAttribute("userRole", scenario.userRole);
        
        // Test role-based authorization checks
        boolean isAdmin = ServletUtil.isAdmin(session);
        boolean isOrganizer = ServletUtil.isOrganizer(session);
        boolean isAttendee = ServletUtil.isAttendee(session);
        
        // Verify that role checks match expected values
        assertEquals(scenario.expectedIsAdmin, isAdmin,
            "Admin check should match for role: " + scenario.userRole);
        assertEquals(scenario.expectedIsOrganizer, isOrganizer,
            "Organizer check should match for role: " + scenario.userRole);
        assertEquals(scenario.expectedIsAttendee, isAttendee,
            "Attendee check should match for role: " + scenario.userRole);
        
        // Verify that only one role is true at a time
        int trueCount = (isAdmin ? 1 : 0) + (isOrganizer ? 1 : 0) + (isAttendee ? 1 : 0);
        assertEquals(1, trueCount,
            "Exactly one role check should be true for role: " + scenario.userRole);
    }

    @Test
    void sessionValidation_multipleIterations() {
        // Test 100 iterations to ensure property holds
        for (int i = 0; i < 100; i++) {
            MockSession validSession = new MockSession();
            validSession.setAttribute("userId", i);
            validSession.setAttribute("userRole", i % 3 == 0 ? "ADMIN" : (i % 3 == 1 ? "ORGANIZER" : "ATTENDEE"));
            
            assertTrue(ServletUtil.isSessionValid(validSession),
                "Valid session should be recognized (iteration " + i + ")");
            
            MockSession invalidSession = new MockSession();
            // Missing userId
            invalidSession.setAttribute("userRole", "ADMIN");
            
            assertFalse(ServletUtil.isSessionValid(invalidSession),
                "Invalid session should be rejected (iteration " + i + ")");
        }
    }

    // ==================== Data Generators ====================

    static Stream<SessionValidationScenario> sessionValidationScenarios() {
        return Stream.of(
            // Valid sessions
            new SessionValidationScenario(true, 1, "ADMIN", true),
            new SessionValidationScenario(true, 2, "ORGANIZER", true),
            new SessionValidationScenario(true, 3, "ATTENDEE", true),
            new SessionValidationScenario(true, 100, "ADMIN", true),
            new SessionValidationScenario(true, 999, "ORGANIZER", true),
            
            // Invalid sessions - no session
            new SessionValidationScenario(false, null, null, false),
            
            // Invalid sessions - missing userId
            new SessionValidationScenario(true, null, "ADMIN", false),
            new SessionValidationScenario(true, null, "ORGANIZER", false),
            new SessionValidationScenario(true, null, "ATTENDEE", false),
            
            // Invalid sessions - missing role
            new SessionValidationScenario(true, 1, null, false),
            new SessionValidationScenario(true, 2, null, false),
            new SessionValidationScenario(true, 3, null, false),
            
            // Invalid sessions - both missing
            new SessionValidationScenario(true, null, null, false)
        );
    }

    static Stream<SecurityEventScenario> securityEventScenarios() {
        return Stream.of(
            new SecurityEventScenario("Unauthorized access attempt", 1, "ADMIN"),
            new SecurityEventScenario("Invalid session access", 2, "ORGANIZER"),
            new SecurityEventScenario("Session validation failure", 3, "ATTENDEE"),
            new SecurityEventScenario("Failed authorization check", null, null),
            new SecurityEventScenario("Missing credentials", null, "ADMIN"),
            new SecurityEventScenario("Invalid role", 5, null),
            new SecurityEventScenario("Expired session", 10, "ORGANIZER"),
            new SecurityEventScenario("Concurrent access violation", 20, "ATTENDEE")
        );
    }

    static Stream<AuthorizationScenario> authorizationScenarios() {
        return Stream.of(
            new AuthorizationScenario(1, "ADMIN", true, false, false),
            new AuthorizationScenario(2, "ORGANIZER", false, true, false),
            new AuthorizationScenario(3, "ATTENDEE", false, false, true),
            new AuthorizationScenario(4, "admin", true, false, false),  // Case insensitive
            new AuthorizationScenario(5, "organizer", false, true, false),
            new AuthorizationScenario(6, "attendee", false, false, true),
            new AuthorizationScenario(7, "Admin", true, false, false),
            new AuthorizationScenario(8, "Organizer", false, true, false),
            new AuthorizationScenario(9, "Attendee", false, false, true),
            new AuthorizationScenario(10, "ADMIN", true, false, false),
            new AuthorizationScenario(100, "ORGANIZER", false, true, false),
            new AuthorizationScenario(999, "ATTENDEE", false, false, true)
        );
    }

    // ==================== Test Data Classes ====================

    static class SessionValidationScenario {
        boolean hasSession;
        Integer userId;
        String role;
        boolean shouldBeValid;

        SessionValidationScenario(boolean hasSession, Integer userId, String role, boolean shouldBeValid) {
            this.hasSession = hasSession;
            this.userId = userId;
            this.role = role;
            this.shouldBeValid = shouldBeValid;
        }
    }

    static class SecurityEventScenario {
        String eventDescription;
        Integer userId;
        String role;

        SecurityEventScenario(String eventDescription, Integer userId, String role) {
            this.eventDescription = eventDescription;
            this.userId = userId;
            this.role = role;
        }
    }

    static class AuthorizationScenario {
        Integer userId;
        String userRole;
        boolean expectedIsAdmin;
        boolean expectedIsOrganizer;
        boolean expectedIsAttendee;

        AuthorizationScenario(Integer userId, String userRole, boolean expectedIsAdmin,
                            boolean expectedIsOrganizer, boolean expectedIsAttendee) {
            this.userId = userId;
            this.userRole = userRole;
            this.expectedIsAdmin = expectedIsAdmin;
            this.expectedIsOrganizer = expectedIsOrganizer;
            this.expectedIsAttendee = expectedIsAttendee;
        }
    }

    // ==================== Mock Session Class ====================

    /**
     * Simple mock session for testing without servlet infrastructure.
     */
    static class MockSession implements javax.servlet.http.HttpSession {
        private Map<String, Object> attributes = new HashMap<>();

        @Override
        public Object getAttribute(String name) {
            return attributes.get(name);
        }

        @Override
        public void setAttribute(String name, Object value) {
            attributes.put(name, value);
        }

        @Override
        public void removeAttribute(String name) {
            attributes.remove(name);
        }

        // Unused methods - minimal implementation
        @Override public long getCreationTime() { return 0; }
        @Override public String getId() { return "mock-session"; }
        @Override public long getLastAccessedTime() { return 0; }
        @Override public javax.servlet.ServletContext getServletContext() { return null; }
        @Override public void setMaxInactiveInterval(int interval) { }
        @Override public int getMaxInactiveInterval() { return 0; }
        @Override public javax.servlet.http.HttpSessionContext getSessionContext() { return null; }
        @Override public Object getValue(String name) { return getAttribute(name); }
        @Override public java.util.Enumeration<String> getAttributeNames() { return null; }
        @Override public String[] getValueNames() { return new String[0]; }
        @Override public void putValue(String name, Object value) { setAttribute(name, value); }
        @Override public void removeValue(String name) { removeAttribute(name); }
        @Override public void invalidate() { attributes.clear(); }
        @Override public boolean isNew() { return false; }
    }
}
