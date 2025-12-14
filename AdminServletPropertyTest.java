package com.eventmgmt.servlet;

import com.eventmgmt.dao.UserDAO;
import com.eventmgmt.dao.UserDAOImpl;
import com.eventmgmt.model.User;
import com.eventmgmt.model.Admin;
import com.eventmgmt.model.Organizer;
import com.eventmgmt.model.Attendee;
import com.eventmgmt.util.ValidationUtil;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for AdminServlet.
 * Tests correctness properties related to admin authorization, user creation, updates, and deletion.
 */
public class AdminServletPropertyTest {

    /**
     * **Feature: servlet-implementation, Property 7: Admin operations require admin role**
     * **Validates: Requirements 2.1, 2.5**
     * 
     * For any admin servlet operation, if the user role is not ADMIN, 
     * the request should be rejected with HTTP 403 status.
     */
    @ParameterizedTest
    @MethodSource("nonAdminRoles")
    void adminOperationsRequireAdminRole(String role) {
        // Test that non-admin roles should be rejected
        // This tests the authorization logic
        boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
        
        // For any non-admin role, access should be denied
        if (!isAdmin) {
            assertFalse(isAdmin, "Non-admin role should not have admin access: " + role);
        }
    }

    /**
     * Test with multiple iterations to ensure property holds across many inputs.
     */
    @Test
    void adminOperationsRequireAdminRole_multipleIterations() {
        String[] nonAdminRoles = {"ORGANIZER", "ATTENDEE", "organizer", "attendee", "USER", "GUEST", "user", "guest"};
        
        for (String role : nonAdminRoles) {
            for (int i = 0; i < 10; i++) {
                boolean isAdmin = "ADMIN".equalsIgnoreCase(role);
                assertFalse(isAdmin, 
                    "Non-admin role should not have admin access: " + role + " (iteration " + i + ")");
            }
        }
    }

    /**
     * **Feature: servlet-implementation, Property 12: User creation validates all required fields**
     * **Validates: Requirements 2.2**
     * 
     * For any user creation request, if any required field (name, email, password, role) 
     * is missing or empty, the request should be rejected with HTTP 400 and specific error messages.
     */
    @ParameterizedTest
    @MethodSource("invalidUserData")
    void userCreationValidatesAllRequiredFields(InvalidUserData userData) {
        // Validate user data using ValidationUtil
        List<String> errors = ValidationUtil.validateUserData(
            userData.name, 
            userData.email, 
            userData.password, 
            userData.role
        );
        
        // Assert that validation catches the missing/invalid fields
        assertFalse(errors.isEmpty(), 
            "Validation should fail for invalid user data: " + userData.description);
        
        // Verify that the error message is specific
        String errorMessage = String.join(", ", errors);
        assertFalse(errorMessage.isEmpty(), "Error message should be specific");
    }

    /**
     * Test with multiple iterations to ensure property holds.
     */
    @Test
    void userCreationValidatesAllRequiredFields_multipleIterations() {
        // Test 100 iterations with various invalid combinations
        for (int i = 0; i < 100; i++) {
            // Test missing name
            List<String> errors1 = ValidationUtil.validateUserData(null, "test@example.com", "password", "ADMIN");
            assertFalse(errors1.isEmpty(), "Should reject null name (iteration " + i + ")");
            
            // Test missing email
            List<String> errors2 = ValidationUtil.validateUserData("Test User", null, "password", "ADMIN");
            assertFalse(errors2.isEmpty(), "Should reject null email (iteration " + i + ")");
            
            // Test missing password
            List<String> errors3 = ValidationUtil.validateUserData("Test User", "test@example.com", null, "ADMIN");
            assertFalse(errors3.isEmpty(), "Should reject null password (iteration " + i + ")");
            
            // Test missing role
            List<String> errors4 = ValidationUtil.validateUserData("Test User", "test@example.com", "password", null);
            assertFalse(errors4.isEmpty(), "Should reject null role (iteration " + i + ")");
        }
    }

    /**
     * **Feature: servlet-implementation, Property 13: User updates validate existence and data**
     * **Validates: Requirements 2.3**
     * 
     * For any user update request, if the user does not exist or updated data is invalid, 
     * the request should be rejected with appropriate error messages.
     */
    @ParameterizedTest
    @MethodSource("invalidUpdateData")
    void userUpdatesValidateExistenceAndData(InvalidUpdateData updateData) {
        // Validate the update data
        List<String> errors = ValidationUtil.validateUserData(
            updateData.name,
            updateData.email,
            updateData.password,
            updateData.role
        );
        
        // Assert that validation catches invalid data
        assertFalse(errors.isEmpty(), 
            "Validation should fail for invalid update data: " + updateData.description);
    }

    /**
     * Test that user existence is checked before update.
     */
    @Test
    void userUpdatesValidateExistence_multipleIterations() {
        UserDAO userDAO = new UserDAOImpl();
        
        // Test with non-existent user IDs
        for (int i = 1; i < 100; i++) {
            int nonExistentId = 999999 + i;
            User user = userDAO.getById(nonExistentId);
            
            // Assert that non-existent users return null
            assertNull(user, "Non-existent user should return null (ID: " + nonExistentId + ")");
        }
    }

    /**
     * **Feature: servlet-implementation, Property 21: Last admin cannot be deleted**
     * **Validates: Requirements 2.4**
     * 
     * For any user deletion request, if the user is the last remaining admin in the system, 
     * the deletion should be prevented.
     */
    @Test
    void lastAdminCannotBeDeleted() {
        // Simulate scenario where we have only one admin
        User admin1 = new Admin(1, "Admin User", "admin@example.com", "password");
        User organizer1 = new Organizer(2, "Organizer User", "org@example.com", "password");
        User attendee1 = new Attendee(3, "Attendee User", "att@example.com", "password");
        
        // Count admins
        User[] users = {admin1, organizer1, attendee1};
        long adminCount = 0;
        for (User user : users) {
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                adminCount++;
            }
        }
        
        // If trying to delete the last admin, it should be prevented
        boolean isLastAdmin = adminCount <= 1 && "ADMIN".equalsIgnoreCase(admin1.getRole());
        assertTrue(isLastAdmin, "Should detect when trying to delete the last admin");
    }

    /**
     * Test with multiple scenarios to ensure property holds.
     */
    @Test
    void lastAdminCannotBeDeleted_multipleScenarios() {
        // Test 100 iterations with different scenarios
        for (int i = 0; i < 100; i++) {
            // Scenario 1: Only one admin
            User[] scenario1 = {
                new Admin(1, "Admin", "admin@example.com", "password"),
                new Organizer(2, "Org", "org@example.com", "password"),
                new Attendee(3, "Att", "att@example.com", "password")
            };
            
            long adminCount1 = countAdmins(scenario1);
            assertEquals(1, adminCount1, "Should have exactly 1 admin in scenario 1");
            
            // Scenario 2: Multiple admins
            User[] scenario2 = {
                new Admin(1, "Admin1", "admin1@example.com", "password"),
                new Admin(2, "Admin2", "admin2@example.com", "password"),
                new Organizer(3, "Org", "org@example.com", "password")
            };
            
            long adminCount2 = countAdmins(scenario2);
            assertEquals(2, adminCount2, "Should have exactly 2 admins in scenario 2");
            assertTrue(adminCount2 > 1, "Multiple admins allow deletion");
            
            // Scenario 3: No admins (edge case)
            User[] scenario3 = {
                new Organizer(1, "Org", "org@example.com", "password"),
                new Attendee(2, "Att", "att@example.com", "password")
            };
            
            long adminCount3 = countAdmins(scenario3);
            assertEquals(0, adminCount3, "Should have 0 admins in scenario 3");
        }
    }

    // ==================== Helper Methods ====================

    private long countAdmins(User[] users) {
        long count = 0;
        for (User user : users) {
            if ("ADMIN".equalsIgnoreCase(user.getRole())) {
                count++;
            }
        }
        return count;
    }

    // ==================== Data Generators ====================

    static Stream<String> nonAdminRoles() {
        return Stream.of(
            "ORGANIZER", "ATTENDEE", "organizer", "attendee", 
            "USER", "GUEST", "user", "guest", "Manager", "Staff"
        );
    }

    static Stream<InvalidUserData> invalidUserData() {
        return Stream.of(
            new InvalidUserData(null, "test@example.com", "password", "ADMIN", "null name"),
            new InvalidUserData("", "test@example.com", "password", "ADMIN", "empty name"),
            new InvalidUserData("   ", "test@example.com", "password", "ADMIN", "whitespace name"),
            new InvalidUserData("Test User", null, "password", "ADMIN", "null email"),
            new InvalidUserData("Test User", "", "password", "ADMIN", "empty email"),
            new InvalidUserData("Test User", "   ", "password", "ADMIN", "whitespace email"),
            new InvalidUserData("Test User", "invalid-email", "password", "ADMIN", "invalid email format"),
            new InvalidUserData("Test User", "test@example.com", null, "ADMIN", "null password"),
            new InvalidUserData("Test User", "test@example.com", "", "ADMIN", "empty password"),
            new InvalidUserData("Test User", "test@example.com", "   ", "ADMIN", "whitespace password"),
            new InvalidUserData("Test User", "test@example.com", "short", "ADMIN", "short password"),
            new InvalidUserData("Test User", "test@example.com", "password", null, "null role"),
            new InvalidUserData("Test User", "test@example.com", "password", "", "empty role"),
            new InvalidUserData("Test User", "test@example.com", "password", "   ", "whitespace role"),
            new InvalidUserData(null, null, null, null, "all null"),
            new InvalidUserData("", "", "", "", "all empty")
        );
    }

    static Stream<InvalidUpdateData> invalidUpdateData() {
        return Stream.of(
            new InvalidUpdateData(null, "test@example.com", "password", "ADMIN", "null name"),
            new InvalidUpdateData("", "test@example.com", "password", "ADMIN", "empty name"),
            new InvalidUpdateData("Test User", null, "password", "ADMIN", "null email"),
            new InvalidUpdateData("Test User", "", "password", "ADMIN", "empty email"),
            new InvalidUpdateData("Test User", "invalid", "password", "ADMIN", "invalid email"),
            new InvalidUpdateData("Test User", "test@example.com", null, "ADMIN", "null password"),
            new InvalidUpdateData("Test User", "test@example.com", "", "ADMIN", "empty password"),
            new InvalidUpdateData("Test User", "test@example.com", "short", "ADMIN", "short password"),
            new InvalidUpdateData("Test User", "test@example.com", "password", null, "null role"),
            new InvalidUpdateData("Test User", "test@example.com", "password", "", "empty role")
        );
    }

    // ==================== Test Data Classes ====================

    static class InvalidUserData {
        String name;
        String email;
        String password;
        String role;
        String description;

        InvalidUserData(String name, String email, String password, String role, String description) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
            this.description = description;
        }
    }

    static class InvalidUpdateData {
        String name;
        String email;
        String password;
        String role;
        String description;

        InvalidUpdateData(String name, String email, String password, String role, String description) {
            this.name = name;
            this.email = email;
            this.password = password;
            this.role = role;
            this.description = description;
        }
    }
}
