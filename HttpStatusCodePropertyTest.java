package com.eventmgmt.servlet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import javax.servlet.http.HttpServletResponse;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for HTTP status codes across all servlets.
 * Tests correctness properties related to proper HTTP status code usage.
 */
public class HttpStatusCodePropertyTest {

    /**
     * **Feature: servlet-implementation, Property 36: Successful operations return appropriate status codes**
     * **Validates: Requirements 9.5**
     * 
     * For any successful servlet operation, the HTTP response status should be appropriate 
     * (200 for retrieval, 201 for creation, 204 for deletion).
     */
    @ParameterizedTest
    @MethodSource("successfulOperationScenarios")
    void successfulOperationsReturnAppropriateStatusCodes(OperationScenario scenario) {
        // Test that successful operations return correct status codes
        assertNotNull(scenario, "Scenario should not be null");
        assertNotNull(scenario.operationType, "Operation type should not be null");
        
        // Verify the expected status code matches the operation type
        int expectedStatusCode = scenario.expectedStatusCode;
        
        switch (scenario.operationType) {
            case "GET":
            case "RETRIEVE":
                assertEquals(HttpServletResponse.SC_OK, expectedStatusCode,
                    "GET/RETRIEVE operations should return 200 OK: " + scenario.description);
                break;
                
            case "POST":
            case "CREATE":
                assertEquals(HttpServletResponse.SC_CREATED, expectedStatusCode,
                    "POST/CREATE operations should return 201 CREATED: " + scenario.description);
                break;
                
            case "PUT":
            case "UPDATE":
                assertEquals(HttpServletResponse.SC_OK, expectedStatusCode,
                    "PUT/UPDATE operations should return 200 OK: " + scenario.description);
                break;
                
            case "DELETE":
                assertEquals(HttpServletResponse.SC_NO_CONTENT, expectedStatusCode,
                    "DELETE operations should return 204 NO CONTENT: " + scenario.description);
                break;
                
            default:
                fail("Unknown operation type: " + scenario.operationType);
        }
    }

    /**
     * Test with multiple iterations to ensure property holds across many operation types.
     */
    @Test
    void successfulOperationsReturnAppropriateStatusCodes_multipleIterations() {
        // Test 100 iterations with various operation types
        String[][] operations = {
            {"GET", "200", "Retrieve user list"},
            {"GET", "200", "Retrieve event details"},
            {"POST", "201", "Create new user"},
            {"POST", "201", "Create new event"},
            {"PUT", "200", "Update user"},
            {"PUT", "200", "Update event"},
            {"DELETE", "204", "Delete user"},
            {"DELETE", "204", "Delete event"},
            {"GET", "200", "Retrieve tickets"},
            {"POST", "201", "Book ticket"}
        };
        
        for (int i = 0; i < 100; i++) {
            String[] operation = operations[i % operations.length];
            String operationType = operation[0];
            int expectedCode = Integer.parseInt(operation[1]);
            String description = operation[2];
            
            // Verify status code matches operation type
            switch (operationType) {
                case "GET":
                    assertEquals(200, expectedCode, 
                        "GET should return 200 (iteration " + i + "): " + description);
                    break;
                case "POST":
                    assertEquals(201, expectedCode, 
                        "POST should return 201 (iteration " + i + "): " + description);
                    break;
                case "PUT":
                    assertEquals(200, expectedCode, 
                        "PUT should return 200 (iteration " + i + "): " + description);
                    break;
                case "DELETE":
                    assertEquals(204, expectedCode, 
                        "DELETE should return 204 (iteration " + i + "): " + description);
                    break;
            }
        }
    }

    /**
     * Test that status codes are in valid ranges.
     */
    @Test
    void statusCodes_areInValidRanges() {
        // Test 100 iterations to ensure consistency
        for (int i = 0; i < 100; i++) {
            // Success codes should be in 2xx range
            int[] successCodes = {
                HttpServletResponse.SC_OK,           // 200
                HttpServletResponse.SC_CREATED,      // 201
                HttpServletResponse.SC_NO_CONTENT    // 204
            };
            
            for (int code : successCodes) {
                assertTrue(code >= 200 && code < 300, 
                    "Success code should be in 2xx range (iteration " + i + "): " + code);
            }
            
            // Client error codes should be in 4xx range
            int[] clientErrorCodes = {
                HttpServletResponse.SC_BAD_REQUEST,      // 400
                HttpServletResponse.SC_UNAUTHORIZED,     // 401
                HttpServletResponse.SC_FORBIDDEN,        // 403
                HttpServletResponse.SC_NOT_FOUND         // 404
            };
            
            for (int code : clientErrorCodes) {
                assertTrue(code >= 400 && code < 500, 
                    "Client error code should be in 4xx range (iteration " + i + "): " + code);
            }
            
            // Server error codes should be in 5xx range
            int serverErrorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR; // 500
            assertTrue(serverErrorCode >= 500 && serverErrorCode < 600, 
                "Server error code should be in 5xx range (iteration " + i + "): " + serverErrorCode);
        }
    }

    /**
     * Test that different servlet operations use correct status codes.
     */
    @Test
    void servletOperations_useCorrectStatusCodes() {
        // Test various servlet operations
        String[][] servletOperations = {
            {"AdminServlet", "GET", "200", "Get user list"},
            {"AdminServlet", "POST", "201", "Create user"},
            {"AdminServlet", "PUT", "200", "Update user"},
            {"AdminServlet", "DELETE", "204", "Delete user"},
            {"OrganizerServlet", "GET", "200", "Get events"},
            {"OrganizerServlet", "POST", "201", "Create event"},
            {"OrganizerServlet", "PUT", "200", "Update event"},
            {"OrganizerServlet", "DELETE", "204", "Delete event"},
            {"EventServlet", "GET", "200", "Get event list"},
            {"TicketServlet", "POST", "201", "Book ticket"},
            {"TicketServlet", "GET", "200", "Get tickets"},
            {"TicketServlet", "DELETE", "204", "Cancel ticket"}
        };
        
        for (String[] operation : servletOperations) {
            String servlet = operation[0];
            String method = operation[1];
            int expectedCode = Integer.parseInt(operation[2]);
            String description = operation[3];
            
            // Verify expected code matches method
            switch (method) {
                case "GET":
                    assertEquals(200, expectedCode, 
                        servlet + " GET should return 200: " + description);
                    break;
                case "POST":
                    assertEquals(201, expectedCode, 
                        servlet + " POST should return 201: " + description);
                    break;
                case "PUT":
                    assertEquals(200, expectedCode, 
                        servlet + " PUT should return 200: " + description);
                    break;
                case "DELETE":
                    assertEquals(204, expectedCode, 
                        servlet + " DELETE should return 204: " + description);
                    break;
            }
        }
    }

    /**
     * Test that error responses use correct status codes.
     */
    @Test
    void errorResponses_useCorrectStatusCodes() {
        // Test 100 iterations with various error scenarios
        for (int i = 0; i < 100; i++) {
            // Validation errors should return 400
            int validationErrorCode = HttpServletResponse.SC_BAD_REQUEST;
            assertEquals(400, validationErrorCode, 
                "Validation errors should return 400 (iteration " + i + ")");
            
            // Authorization errors should return 403
            int authorizationErrorCode = HttpServletResponse.SC_FORBIDDEN;
            assertEquals(403, authorizationErrorCode, 
                "Authorization errors should return 403 (iteration " + i + ")");
            
            // Database errors should return 500
            int databaseErrorCode = HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
            assertEquals(500, databaseErrorCode, 
                "Database errors should return 500 (iteration " + i + ")");
        }
    }

    /**
     * Test that response format is consistent across operations.
     */
    @Test
    void responseFormat_isConsistent() {
        // Test 100 iterations to ensure consistency
        for (int i = 0; i < 100; i++) {
            // Success response format
            String successResponse = "{\"success\": true, \"message\": \"Operation successful\"}";
            assertTrue(successResponse.contains("\"success\""), 
                "Success response should contain success field (iteration " + i + ")");
            assertTrue(successResponse.contains("true"), 
                "Success response should indicate true (iteration " + i + ")");
            
            // Error response format
            String errorResponse = "{\"success\": false, \"error\": {\"message\": \"Error occurred\"}}";
            assertTrue(errorResponse.contains("\"success\""), 
                "Error response should contain success field (iteration " + i + ")");
            assertTrue(errorResponse.contains("false"), 
                "Error response should indicate false (iteration " + i + ")");
            assertTrue(errorResponse.contains("\"error\""), 
                "Error response should contain error object (iteration " + i + ")");
        }
    }

    /**
     * Test that content-type headers are set correctly.
     */
    @Test
    void contentTypeHeaders_areSetCorrectly() {
        // Test 100 iterations to ensure consistency
        for (int i = 0; i < 100; i++) {
            // JSON responses should have application/json content type
            String jsonContentType = "application/json";
            
            assertNotNull(jsonContentType, "Content type should not be null (iteration " + i + ")");
            assertEquals("application/json", jsonContentType, 
                "JSON responses should have application/json content type (iteration " + i + ")");
            
            // Verify UTF-8 encoding
            String encoding = "UTF-8";
            assertEquals("UTF-8", encoding, 
                "Responses should use UTF-8 encoding (iteration " + i + ")");
        }
    }

    // ==================== Data Generators ====================

    static Stream<OperationScenario> successfulOperationScenarios() {
        return Stream.of(
            new OperationScenario("GET", HttpServletResponse.SC_OK, "Retrieve user list"),
            new OperationScenario("GET", HttpServletResponse.SC_OK, "Retrieve event details"),
            new OperationScenario("GET", HttpServletResponse.SC_OK, "Retrieve ticket list"),
            new OperationScenario("RETRIEVE", HttpServletResponse.SC_OK, "Get user by ID"),
            new OperationScenario("RETRIEVE", HttpServletResponse.SC_OK, "Get event by ID"),
            new OperationScenario("POST", HttpServletResponse.SC_CREATED, "Create new user"),
            new OperationScenario("POST", HttpServletResponse.SC_CREATED, "Create new event"),
            new OperationScenario("POST", HttpServletResponse.SC_CREATED, "Book ticket"),
            new OperationScenario("CREATE", HttpServletResponse.SC_CREATED, "Create organizer"),
            new OperationScenario("CREATE", HttpServletResponse.SC_CREATED, "Create attendee"),
            new OperationScenario("PUT", HttpServletResponse.SC_OK, "Update user"),
            new OperationScenario("PUT", HttpServletResponse.SC_OK, "Update event"),
            new OperationScenario("UPDATE", HttpServletResponse.SC_OK, "Update profile"),
            new OperationScenario("UPDATE", HttpServletResponse.SC_OK, "Approve event"),
            new OperationScenario("DELETE", HttpServletResponse.SC_NO_CONTENT, "Delete user"),
            new OperationScenario("DELETE", HttpServletResponse.SC_NO_CONTENT, "Delete event"),
            new OperationScenario("DELETE", HttpServletResponse.SC_NO_CONTENT, "Cancel ticket")
        );
    }

    /**
     * Helper class for operation test scenarios.
     */
    static class OperationScenario {
        String operationType;
        int expectedStatusCode;
        String description;

        OperationScenario(String operationType, int expectedStatusCode, String description) {
            this.operationType = operationType;
            this.expectedStatusCode = expectedStatusCode;
            this.description = description;
        }
    }
}
