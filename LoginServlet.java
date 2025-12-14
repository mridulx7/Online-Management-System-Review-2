package com.eventmgmt.servlet;

import javax.servlet.*;
import javax.servlet.http.*;
import javax.servlet.annotation.WebServlet;
import java.io.IOException;
import com.eventmgmt.dao.*;
import com.eventmgmt.model.*;
import com.eventmgmt.util.ValidationUtil;
import com.eventmgmt.util.ServletUtil;

/**
 * LoginServlet handles user authentication, session creation, and logout functionality.
 * Implements proper validation, error handling, and security logging.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    /**
     * Handles GET requests for displaying login form or processing logout.
     * 
     * @param req The HTTP request
     * @param resp The HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        
        String action = req.getParameter("action");
        
        // Handle logout
        if ("logout".equals(action)) {
            handleLogout(req, resp);
        } else {
            // Display login form
            req.getRequestDispatcher("views/login.jsp").forward(req, resp);
        }
    }

    /**
     * Handles POST requests for user login authentication.
     * Validates credentials, creates session, and redirects based on role.
     * 
     * @param req The HTTP request
     * @param resp The HTTP response
     * @throws ServletException if servlet error occurs
     * @throws IOException if I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        try {
            // Validate email and password are not empty
            if (email == null || email.trim().isEmpty()) {
                ServletUtil.logFailedLogin(email, "Empty email");
                resp.sendRedirect("views/login.jsp?error=empty_email");
                return;
            }

            if (password == null || password.trim().isEmpty()) {
                ServletUtil.logFailedLogin(email, "Empty password");
                resp.sendRedirect("views/login.jsp?error=empty_password");
                return;
            }

            // Validate email format before database lookup (Requirement 1.1)
            if (!ValidationUtil.validateEmail(email)) {
                ServletUtil.logFailedLogin(email, "Invalid email format");
                resp.sendRedirect("views/login.jsp?error=invalid_email");
                return;
            }

            // Attempt to authenticate user
            UserDAO userDAO = new UserDAOImpl();
            User user = userDAO.getUserByEmail(email);

            if (user != null && user.getPassword().equals(password)) {
                // Successful login - create session with all required attributes (Requirement 1.3)
                createUserSession(req.getSession(), user);
                
                // Redirect based on role
                if (user.getRole().equals("ADMIN")) {
                    resp.sendRedirect("views/adminDashboard.jsp");
                } else if (user.getRole().equals("ORGANIZER")) {
                    resp.sendRedirect("views/organizerDashboard.jsp");
                } else {
                    resp.sendRedirect("views/attendeeDashboard.jsp");
                }
            } else {
                // Failed login - log attempt and redirect with error (Requirement 1.4)
                ServletUtil.logFailedLogin(email, "Invalid credentials");
                resp.sendRedirect("views/login.jsp?error=invalid_credentials");
            }

        } catch (Exception e) {
            // Database and unexpected error handling (Requirements 1.5)
            ServletUtil.logError("Error during login: " + e.getMessage(), req.getSession(false), e);
            resp.sendRedirect("views/login.jsp?error=system_error");
        }
    }

    /**
     * Creates a user session with all required attributes.
     * 
     * @param session The HTTP session
     * @param user The authenticated user
     */
    private void createUserSession(HttpSession session, User user) {
        session.setAttribute("userId", user.getId());
        session.setAttribute("userRole", user.getRole());
        session.setAttribute("userName", user.getName());
        session.setAttribute("userEmail", user.getEmail());
        session.setAttribute("loginTime", System.currentTimeMillis());
    }

    /**
     * Handles user logout by invalidating session and clearing all attributes.
     * Implements Requirement 8.3.
     * 
     * @param req The HTTP request
     * @param resp The HTTP response
     * @throws IOException if I/O error occurs
     */
    private void handleLogout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        
        if (session != null) {
            // Log the logout event
            ServletUtil.logSecurityEvent("User logout", session);
            
            // Invalidate session and clear all attributes
            session.invalidate();
        }
        
        // Redirect to login page
        resp.sendRedirect("views/login.jsp?message=logged_out");
    }
}