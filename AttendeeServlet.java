package com.servlet;

import com.eventmgmt.dao.UserDAO;
import com.eventmgmt.dao.UserDAOImpl;
import com.eventmgmt.dao.RegistrationDAO;
import com.eventmgmt.dao.RegistrationDAOImpl;
import com.eventmgmt.dao.EventDAO;
import com.eventmgmt.dao.EventDAOImpl;
import com.eventmgmt.model.User;
import com.eventmgmt.model.Registration;
import com.eventmgmt.model.Event;
import com.eventmgmt.util.ServletUtil;
import com.eventmgmt.util.ValidationUtil;
import com.eventmgmt.util.ValidationException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * AttendeeServlet handles attendee-specific operations.
 * Provides endpoints for viewing profile and registered events.
 * 
 * Supported operations:
 * - GET: Retrieve attendee profile and registered events
 * - POST: Update attendee profile
 * 
 * All operations require valid session with ATTENDEE role.
 */
@WebServlet("/attendee")
public class AttendeeServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(AttendeeServlet.class.getName());
    private UserDAO userDAO;
    private RegistrationDAO registrationDAO;
    private EventDAO eventDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAOImpl();
        registrationDAO = new RegistrationDAOImpl();
        eventDAO = new EventDAOImpl();
    }

    /**
     * Handles GET requests to retrieve attendee profile or registered events.
     * Implements Requirement 8.1 - session validation.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Verify session validity (Requirement 8.1)
        if (!ServletUtil.isSessionValid(session)) {
            ServletUtil.logSecurityEvent("Invalid session access to AttendeeServlet", session);
            response.sendRedirect("login.jsp?error=session_invalid");
            return;
        }

        // Verify attendee role (Requirement 8.4)
        if (!verifyAttendeeRole(session, response)) {
            return;
        }

        try {
            String action = request.getParameter("action");
            
            if ("profile".equals(action)) {
                handleGetProfile(request, response, session);
            } else if ("events".equals(action)) {
                handleGetRegisteredEvents(request, response, session);
            } else {
                // Default: show profile
                handleGetProfile(request, response, session);
            }
            
        } catch (SQLException e) {
            ServletUtil.logError("Database error in AttendeeServlet.doGet", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Database operation failed. Please try again later.");
        } catch (Exception e) {
            ServletUtil.logError("Unexpected error in AttendeeServlet.doGet", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Handles POST requests to update attendee profile.
     * Implements Requirement 8.1 - session validation.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Verify session validity (Requirement 8.1)
        if (!ServletUtil.isSessionValid(session)) {
            ServletUtil.logSecurityEvent("Invalid session access to AttendeeServlet", session);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                "Session invalid. Please log in again.");
            return;
        }

        // Verify attendee role (Requirement 8.4)
        if (!verifyAttendeeRole(session, response)) {
            return;
        }

        try {
            handleUpdateProfile(request, response, session);
            
        } catch (ValidationException e) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            ServletUtil.logError("Database error in AttendeeServlet.doPost", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Database operation failed. Please try again later.");
        } catch (Exception e) {
            ServletUtil.logError("Unexpected error in AttendeeServlet.doPost", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Verifies that the user has ATTENDEE role.
     * Implements Requirement 8.4 - authorization checks.
     * 
     * @param session The HTTP session
     * @param response The HTTP response
     * @return true if user is attendee, false otherwise
     * @throws IOException if I/O error occurs
     */
    private boolean verifyAttendeeRole(HttpSession session, HttpServletResponse response) throws IOException {
        if (!ServletUtil.isAttendee(session)) {
            ServletUtil.logSecurityEvent("Unauthorized access attempt to AttendeeServlet", session);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                "Access denied. Attendee role required.");
            return false;
        }
        return true;
    }

    /**
     * Handles retrieving attendee profile.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param session The HTTP session
     * @throws IOException if I/O error occurs
     * @throws SQLException if database error occurs
     */
    private void handleGetProfile(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, SQLException {
        
        Integer userId = ServletUtil.getUserIdFromSession(session);
        User user = userDAO.getById(userId);
        
        if (user == null) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                "User not found");
            return;
        }
        
        // Build JSON response
        String json = String.format(
            "{\"success\": true, \"profile\": {\"id\": %d, \"name\": \"%s\", \"email\": \"%s\", \"role\": \"%s\"}}",
            user.getId(),
            escapeJson(user.getName()),
            escapeJson(user.getEmail()),
            escapeJson(user.getRole())
        );
        
        ServletUtil.sendJsonResponse(response, HttpServletResponse.SC_OK, json);
    }

    /**
     * Handles retrieving registered events for the attendee.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param session The HTTP session
     * @throws IOException if I/O error occurs
     * @throws SQLException if database error occurs
     */
    private void handleGetRegisteredEvents(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, SQLException {
        
        Integer userId = ServletUtil.getUserIdFromSession(session);
        List<Registration> registrations = registrationDAO.getByUserId(userId);
        
        // Build JSON response
        StringBuilder json = new StringBuilder();
        json.append("{\"success\": true, \"events\": [");
        
        int count = 0;
        for (Registration reg : registrations) {
            if ("REGISTERED".equals(reg.getStatus())) {
                Event event = eventDAO.getById(reg.getEventId());
                
                if (event != null) {
                    if (count > 0) {
                        json.append(",");
                    }
                    
                    json.append("{");
                    json.append("\"registrationId\": ").append(reg.getId()).append(",");
                    json.append("\"eventId\": ").append(event.getId()).append(",");
                    json.append("\"title\": \"").append(escapeJson(event.getTitle())).append("\",");
                    json.append("\"description\": \"").append(escapeJson(event.getDescription())).append("\",");
                    json.append("\"date\": \"").append(event.getDate().toString()).append("\",");
                    json.append("\"time\": \"").append(event.getTime().toString()).append("\",");
                    json.append("\"venue\": \"").append(escapeJson(event.getVenue())).append("\",");
                    json.append("\"status\": \"").append(escapeJson(event.getStatus())).append("\"");
                    json.append("}");
                    
                    count++;
                }
            }
        }
        
        json.append("]}");
        
        ServletUtil.sendJsonResponse(response, HttpServletResponse.SC_OK, json.toString());
    }

    /**
     * Handles updating attendee profile.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param session The HTTP session
     * @throws IOException if I/O error occurs
     * @throws ValidationException if validation fails
     * @throws SQLException if database error occurs
     */
    private void handleUpdateProfile(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ValidationException, SQLException {
        
        Integer userId = ServletUtil.getUserIdFromSession(session);
        
        // Get existing user
        User user = userDAO.getById(userId);
        if (user == null) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                "User not found");
            return;
        }
        
        // Get and sanitize parameters
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        
        if (name != null && !name.trim().isEmpty()) {
            name = ValidationUtil.sanitizeInput(name);
            user.setName(name);
        }
        
        if (email != null && !email.trim().isEmpty()) {
            email = ValidationUtil.sanitizeInput(email);
            
            // Validate email format
            if (!ValidationUtil.validateEmail(email)) {
                throw new ValidationException("Invalid email format");
            }
            
            // Check if email is already taken by another user
            User existingUser = userDAO.getUserByEmail(email);
            if (existingUser != null && existingUser.getId() != userId) {
                throw new ValidationException("Email already exists");
            }
            
            user.setEmail(email);
        }
        
        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(password);
        }
        
        // Update user
        boolean success = userDAO.update(user);
        
        if (success) {
            logger.info("Profile updated successfully for user " + userId);
            ServletUtil.sendSuccessResponse(response, HttpServletResponse.SC_OK,
                "Profile updated successfully");
        } else {
            throw new SQLException("Failed to update profile");
        }
    }

    /**
     * Escapes special characters in JSON strings.
     * 
     * @param input The input string
     * @return The escaped string
     */
    private String escapeJson(String input) {
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
