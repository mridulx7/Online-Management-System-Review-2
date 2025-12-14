package com.servlet;

import com.eventmgmt.dao.UserDAO;
import com.eventmgmt.dao.UserDAOImpl;
import com.eventmgmt.model.User;
import com.eventmgmt.model.Admin;
import com.eventmgmt.model.Organizer;
import com.eventmgmt.model.Attendee;
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
 * AdminServlet handles administrative operations for user management.
 * Provides CRUD operations for users with proper authorization and validation.
 * 
 * Supported operations:
 * - GET: Retrieve user list or specific user details
 * - POST: Create new users
 * - PUT: Update existing users
 * - DELETE: Delete users
 * 
 * All operations require admin role authorization.
 */
@WebServlet("/admin")
public class AdminServlet extends HttpServlet {
    
    private static final Logger logger = Logger.getLogger(AdminServlet.class.getName());
    private UserDAO userDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        userDAO = new UserDAOImpl();
    }

    /**
     * Handles GET requests to retrieve user list or specific user details.
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
        
        // Verify session validity
        if (!ServletUtil.isSessionValid(session)) {
            ServletUtil.logSecurityEvent("Invalid session access to AdminServlet", session);
            response.sendRedirect("login.jsp?error=session_invalid");
            return;
        }
        
        // Verify admin role
        if (!verifyAdminRole(session, response)) {
            return;
        }
        
        try {
            String userIdParam = request.getParameter("userId");
            
            if (userIdParam != null && !userIdParam.trim().isEmpty()) {
                // Get specific user
                handleGetUserById(request, response, userIdParam);
            } else {
                // Get all users
                handleGetAllUsers(request, response);
            }
            
        } catch (ValidationException e) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            ServletUtil.logError("Database error in AdminServlet.doGet", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Database operation failed");
        } catch (Exception e) {
            ServletUtil.logError("Unexpected error in AdminServlet.doGet", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred");
        }
    }

    /**
     * Handles POST requests to create new users.
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
        
        // Verify session validity
        if (!ServletUtil.isSessionValid(session)) {
            ServletUtil.logSecurityEvent("Invalid session access to AdminServlet", session);
            response.sendRedirect("login.jsp?error=session_invalid");
            return;
        }
        
        // Verify admin role
        if (!verifyAdminRole(session, response)) {
            return;
        }
        
        try {
            handleCreateUser(request, response, session);
            
        } catch (ValidationException e) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            ServletUtil.logError("Database error in AdminServlet.doPost", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Database operation failed");
        } catch (Exception e) {
            ServletUtil.logError("Unexpected error in AdminServlet.doPost", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred");
        }
    }

    /**
     * Handles PUT requests to update existing users.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Verify session validity
        if (!ServletUtil.isSessionValid(session)) {
            ServletUtil.logSecurityEvent("Invalid session access to AdminServlet", session);
            response.sendRedirect("login.jsp?error=session_invalid");
            return;
        }
        
        // Verify admin role
        if (!verifyAdminRole(session, response)) {
            return;
        }
        
        try {
            handleUpdateUser(request, response, session);
            
        } catch (ValidationException e) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            ServletUtil.logError("Database error in AdminServlet.doPut", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Database operation failed");
        } catch (Exception e) {
            ServletUtil.logError("Unexpected error in AdminServlet.doPut", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred");
        }
    }

    /**
     * Handles DELETE requests to delete users.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        // Verify session validity
        if (!ServletUtil.isSessionValid(session)) {
            ServletUtil.logSecurityEvent("Invalid session access to AdminServlet", session);
            response.sendRedirect("login.jsp?error=session_invalid");
            return;
        }
        
        // Verify admin role
        if (!verifyAdminRole(session, response)) {
            return;
        }
        
        try {
            handleDeleteUser(request, response, session);
            
        } catch (ValidationException e) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            ServletUtil.logError("Database error in AdminServlet.doDelete", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "Database operation failed");
        } catch (Exception e) {
            ServletUtil.logError("Unexpected error in AdminServlet.doDelete", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                "An unexpected error occurred");
        }
    }

    /**
     * Verifies that the user has admin role.
     * 
     * @param session The HTTP session
     * @param response The HTTP response
     * @return true if user is admin, false otherwise
     * @throws IOException if I/O error occurs
     */
    private boolean verifyAdminRole(HttpSession session, HttpServletResponse response) throws IOException {
        if (!ServletUtil.isAdmin(session)) {
            ServletUtil.logSecurityEvent("Unauthorized access attempt to AdminServlet", session);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, 
                "Access denied. Admin role required.");
            return false;
        }
        return true;
    }

    /**
     * Handles retrieving all users.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws IOException if I/O error occurs
     * @throws SQLException if database error occurs
     */
    private void handleGetAllUsers(HttpServletRequest request, HttpServletResponse response) 
            throws IOException, SQLException {
        
        List<User> users = userDAO.getAll();
        
        // Build JSON response
        StringBuilder json = new StringBuilder();
        json.append("{\"success\": true, \"users\": [");
        
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            json.append("{");
            json.append("\"id\": ").append(user.getId()).append(",");
            json.append("\"name\": \"").append(escapeJson(user.getName())).append("\",");
            json.append("\"email\": \"").append(escapeJson(user.getEmail())).append("\",");
            json.append("\"role\": \"").append(escapeJson(user.getRole())).append("\"");
            json.append("}");
            
            if (i < users.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]}");
        
        ServletUtil.sendJsonResponse(response, HttpServletResponse.SC_OK, json.toString());
    }

    /**
     * Handles retrieving a specific user by ID.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param userIdParam The user ID parameter
     * @throws IOException if I/O error occurs
     * @throws ValidationException if validation fails
     * @throws SQLException if database error occurs
     */
    private void handleGetUserById(HttpServletRequest request, HttpServletResponse response, String userIdParam) 
            throws IOException, ValidationException, SQLException {
        
        int userId = ValidationUtil.validatePositiveInteger(userIdParam, "User ID");
        User user = userDAO.getById(userId);
        
        if (user == null) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND, 
                "User not found with ID: " + userId);
            return;
        }
        
        // Build JSON response
        String json = String.format(
            "{\"success\": true, \"user\": {\"id\": %d, \"name\": \"%s\", \"email\": \"%s\", \"role\": \"%s\"}}",
            user.getId(),
            escapeJson(user.getName()),
            escapeJson(user.getEmail()),
            escapeJson(user.getRole())
        );
        
        ServletUtil.sendJsonResponse(response, HttpServletResponse.SC_OK, json);
    }

    /**
     * Handles creating a new user.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param session The HTTP session
     * @throws IOException if I/O error occurs
     * @throws ValidationException if validation fails
     * @throws SQLException if database error occurs
     */
    private void handleCreateUser(HttpServletRequest request, HttpServletResponse response, HttpSession session) 
            throws IOException, ValidationException, SQLException {
        
        // Get parameters
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        
        // Sanitize inputs
        name = ValidationUtil.sanitizeInput(name);
        email = ValidationUtil.sanitizeInput(email);
        role = ValidationUtil.sanitizeInput(role);
        
        // Validate user data
        List<String> errors = ValidationUtil.validateUserData(name, email, password, role);
        if (!errors.isEmpty()) {
            String errorMessage = String.join(", ", errors);
            throw new ValidationException(errorMessage);
        }
        
        // Check if email already exists
        User existingUser = userDAO.getUserByEmail(email);
        if (existingUser != null) {
            throw new ValidationException("Email already exists");
        }
        
        // Create appropriate user type based on role
        User newUser = createUserByRole(role);
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);
        newUser.setRole(role.toUpperCase());
        
        // Insert user
        boolean success = userDAO.insert(newUser);
        
        if (success) {
            logger.info("User created successfully by admin: " + ServletUtil.getUserIdFromSession(session));
            ServletUtil.sendSuccessResponse(response, HttpServletResponse.SC_CREATED, 
                "User created successfully");
        } else {
            throw new SQLException("Failed to create user");
        }
    }

    /**
     * Handles updating an existing user.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param session The HTTP session
     * @throws IOException if I/O error occurs
     * @throws ValidationException if validation fails
     * @throws SQLException if database error occurs
     */
    private void handleUpdateUser(HttpServletRequest request, HttpServletResponse response, HttpSession session) 
            throws IOException, ValidationException, SQLException {
        
        // Get parameters
        String userIdParam = request.getParameter("userId");
        String name = request.getParameter("name");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String role = request.getParameter("role");
        
        // Validate user ID
        int userId = ValidationUtil.validatePositiveInteger(userIdParam, "User ID");
        
        // Check if user exists
        User existingUser = userDAO.getById(userId);
        if (existingUser == null) {
            throw new ValidationException("User not found with ID: " + userId);
        }
        
        // Sanitize inputs
        name = ValidationUtil.sanitizeInput(name);
        email = ValidationUtil.sanitizeInput(email);
        role = ValidationUtil.sanitizeInput(role);
        
        // Validate user data
        List<String> errors = ValidationUtil.validateUserData(name, email, password, role);
        if (!errors.isEmpty()) {
            String errorMessage = String.join(", ", errors);
            throw new ValidationException(errorMessage);
        }
        
        // Check if email is being changed and if new email already exists
        if (!email.equals(existingUser.getEmail())) {
            User userWithEmail = userDAO.getUserByEmail(email);
            if (userWithEmail != null && userWithEmail.getId() != userId) {
                throw new ValidationException("Email already exists");
            }
        }
        
        // Update user
        existingUser.setName(name);
        existingUser.setEmail(email);
        existingUser.setPassword(password);
        existingUser.setRole(role.toUpperCase());
        
        boolean success = userDAO.update(existingUser);
        
        if (success) {
            logger.info("User updated successfully by admin: " + ServletUtil.getUserIdFromSession(session));
            ServletUtil.sendSuccessResponse(response, HttpServletResponse.SC_OK, 
                "User updated successfully");
        } else {
            throw new SQLException("Failed to update user");
        }
    }

    /**
     * Handles deleting a user.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param session The HTTP session
     * @throws IOException if I/O error occurs
     * @throws ValidationException if validation fails
     * @throws SQLException if database error occurs
     */
    private void handleDeleteUser(HttpServletRequest request, HttpServletResponse response, HttpSession session) 
            throws IOException, ValidationException, SQLException {
        
        // Get parameters
        String userIdParam = request.getParameter("userId");
        
        // Validate user ID
        int userId = ValidationUtil.validatePositiveInteger(userIdParam, "User ID");
        
        // Check if user exists
        User user = userDAO.getById(userId);
        if (user == null) {
            throw new ValidationException("User not found with ID: " + userId);
        }
        
        // Prevent deletion of last admin
        if (preventLastAdminDeletion(userId)) {
            throw new ValidationException("Cannot delete the last admin user");
        }
        
        // Delete user
        boolean success = userDAO.delete(userId);
        
        if (success) {
            logger.info("User deleted successfully by admin: " + ServletUtil.getUserIdFromSession(session));
            ServletUtil.sendSuccessResponse(response, HttpServletResponse.SC_NO_CONTENT, 
                "User deleted successfully");
        } else {
            throw new SQLException("Failed to delete user");
        }
    }

    /**
     * Prevents deletion of the last admin user.
     * 
     * @param userId The ID of the user to be deleted
     * @return true if this is the last admin and deletion should be prevented, false otherwise
     * @throws SQLException if database error occurs
     */
    private boolean preventLastAdminDeletion(int userId) throws SQLException {
        User user = userDAO.getById(userId);
        
        // Only check if the user being deleted is an admin
        if (user != null && "ADMIN".equalsIgnoreCase(user.getRole())) {
            // Count total admins
            List<User> allUsers = userDAO.getAll();
            long adminCount = allUsers.stream()
                .filter(u -> "ADMIN".equalsIgnoreCase(u.getRole()))
                .count();
            
            // Prevent deletion if this is the last admin
            return adminCount <= 1;
        }
        
        return false;
    }

    /**
     * Creates a user instance based on role.
     * 
     * @param role The user role
     * @return A new user instance of the appropriate type
     * @throws ValidationException if role is invalid
     */
    private User createUserByRole(String role) throws ValidationException {
        if (role == null) {
            throw new ValidationException("Role is required");
        }
        
        switch (role.toUpperCase()) {
            case "ADMIN":
                return new Admin();
            case "ORGANIZER":
                return new Organizer();
            case "ATTENDEE":
                return new Attendee();
            default:
                throw new ValidationException("Invalid role: " + role);
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
