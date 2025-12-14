package com.eventmgmt.util;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

/**
 * Utility class for common servlet operations.
 * Provides helper methods for session management, response handling, and logging.
 */
public class ServletUtil {

    private static final Logger logger = Logger.getLogger(ServletUtil.class.getName());

    private ServletUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Retrieves the user ID from the session.
     * 
     * @param session The HTTP session
     * @return The user ID, or null if not found
     */
    public static Integer getUserIdFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object userId = session.getAttribute("userId");
        if (userId instanceof Integer) {
            return (Integer) userId;
        }
        return null;
    }

    /**
     * Retrieves the user role from the session.
     * 
     * @param session The HTTP session
     * @return The user role, or null if not found
     */
    public static String getUserRoleFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object role = session.getAttribute("userRole");
        if (role instanceof String) {
            return (String) role;
        }
        return null;
    }

    /**
     * Retrieves the user email from the session.
     * 
     * @param session The HTTP session
     * @return The user email, or null if not found
     */
    public static String getUserEmailFromSession(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object email = session.getAttribute("userEmail");
        if (email instanceof String) {
            return (String) email;
        }
        return null;
    }

    /**
     * Checks if a session is valid (exists and has required attributes).
     * 
     * @param session The HTTP session
     * @return true if session is valid, false otherwise
     */
    public static boolean isSessionValid(HttpSession session) {
        if (session == null) {
            return false;
        }
        
        Integer userId = getUserIdFromSession(session);
        String userRole = getUserRoleFromSession(session);
        
        return userId != null && userRole != null;
    }

    /**
     * Sends a JSON response to the client.
     * 
     * @param response The HTTP response object
     * @param statusCode The HTTP status code
     * @param jsonData The JSON data to send
     * @throws IOException if writing to response fails
     */
    public static void sendJsonResponse(HttpServletResponse response, int statusCode, String jsonData) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        PrintWriter out = response.getWriter();
        out.print(jsonData);
        out.flush();
    }

    /**
     * Sends an error response to the client.
     * 
     * @param response The HTTP response object
     * @param statusCode The HTTP status code
     * @param errorMessage The error message
     * @throws IOException if writing to response fails
     */
    public static void sendErrorResponse(HttpServletResponse response, int statusCode, String errorMessage) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonError = String.format(
            "{\"success\": false, \"error\": {\"message\": \"%s\"}}",
            escapeJson(errorMessage)
        );
        
        PrintWriter out = response.getWriter();
        out.print(jsonError);
        out.flush();
    }

    /**
     * Sends a success response to the client.
     * 
     * @param response The HTTP response object
     * @param statusCode The HTTP status code
     * @param message The success message
     * @throws IOException if writing to response fails
     */
    public static void sendSuccessResponse(HttpServletResponse response, int statusCode, String message) throws IOException {
        response.setStatus(statusCode);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        String jsonSuccess = String.format(
            "{\"success\": true, \"message\": \"%s\"}",
            escapeJson(message)
        );
        
        PrintWriter out = response.getWriter();
        out.print(jsonSuccess);
        out.flush();
    }

    /**
     * Logs a security event with context information.
     * 
     * @param event The security event description
     * @param session The HTTP session (may be null)
     */
    public static void logSecurityEvent(String event, HttpSession session) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Integer userId = getUserIdFromSession(session);
        String userRole = getUserRoleFromSession(session);
        
        String logMessage = String.format(
            "[SECURITY] %s - Event: %s, UserId: %s, Role: %s",
            timestamp,
            event,
            userId != null ? userId : "N/A",
            userRole != null ? userRole : "N/A"
        );
        
        logger.warning(logMessage);
    }

    /**
     * Logs an error with full context information.
     * 
     * @param errorMessage The error message
     * @param session The HTTP session (may be null)
     * @param exception The exception that occurred (may be null)
     */
    public static void logError(String errorMessage, HttpSession session, Throwable exception) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        Integer userId = getUserIdFromSession(session);
        String userRole = getUserRoleFromSession(session);
        
        String logMessage = String.format(
            "[ERROR] %s - Message: %s, UserId: %s, Role: %s",
            timestamp,
            errorMessage,
            userId != null ? userId : "N/A",
            userRole != null ? userRole : "N/A"
        );
        
        if (exception != null) {
            logger.severe(logMessage + " - Exception: " + exception.getMessage());
            exception.printStackTrace();
        } else {
            logger.severe(logMessage);
        }
    }

    /**
     * Logs a failed login attempt.
     * 
     * @param email The email used in the login attempt
     * @param reason The reason for failure
     */
    public static void logFailedLogin(String email, String reason) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        String logMessage = String.format(
            "[LOGIN_FAILURE] %s - Email: %s, Reason: %s",
            timestamp,
            email != null ? email : "N/A",
            reason
        );
        logger.warning(logMessage);
    }

    /**
     * Checks if a user has admin role.
     * 
     * @param session The HTTP session
     * @return true if user is admin, false otherwise
     */
    public static boolean isAdmin(HttpSession session) {
        String role = getUserRoleFromSession(session);
        return "ADMIN".equalsIgnoreCase(role);
    }

    /**
     * Checks if a user has organizer role.
     * 
     * @param session The HTTP session
     * @return true if user is organizer, false otherwise
     */
    public static boolean isOrganizer(HttpSession session) {
        String role = getUserRoleFromSession(session);
        return "ORGANIZER".equalsIgnoreCase(role);
    }

    /**
     * Checks if a user has attendee role.
     * 
     * @param session The HTTP session
     * @return true if user is attendee, false otherwise
     */
    public static boolean isAttendee(HttpSession session) {
        String role = getUserRoleFromSession(session);
        return "ATTENDEE".equalsIgnoreCase(role);
    }

    /**
     * Escapes special characters in JSON strings.
     * 
     * @param input The input string
     * @return The escaped string
     */
    private static String escapeJson(String input) {
        if (input == null) {
            return "";
        }
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
    }
}
