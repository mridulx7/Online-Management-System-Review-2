package com.servlet;

import com.eventmgmt.dao.EventDAO;
import com.eventmgmt.dao.EventDAOImpl;
import com.eventmgmt.dao.TicketDAO;
import com.eventmgmt.dao.TicketDAOImpl;
import com.eventmgmt.model.Event;
import com.eventmgmt.model.Ticket;
import com.eventmgmt.util.ServletUtil;
import com.eventmgmt.util.ValidationException;
import com.eventmgmt.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Servlet for handling organizer-specific event management operations.
 * Provides endpoints for creating, updating, deleting, and retrieving events.
 * 
 * Requires ORGANIZER or ADMIN role for all operations.
 */
@WebServlet("/organizer/*")
public class OrganizerServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(OrganizerServlet.class.getName());
    private EventDAO eventDAO;
    private TicketDAO ticketDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        eventDAO = new EventDAOImpl();
        ticketDAO = new TicketDAOImpl();
    }

    /**
     * Handles GET requests to retrieve organizer's events.
     * 
     * @param request  The HTTP request
     * @param response The HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        try {
            // Verify session validity
            if (!ServletUtil.isSessionValid(session)) {
                ServletUtil.logSecurityEvent("Invalid session access to OrganizerServlet", session);
                response.sendRedirect("login.jsp?error=session_invalid");
                return;
            }

            // Verify organizer or admin role
            if (!verifyOrganizerOrAdminRole(session, response)) {
                return;
            }

            Integer userId = ServletUtil.getUserIdFromSession(session);
            String userRole = ServletUtil.getUserRoleFromSession(session);

            // Get organizer's events
            List<Event> events = handleGetOrganizerEvents(userId, userRole);

            // Set events as request attribute and forward to JSP
            request.setAttribute("events", events);
            request.getRequestDispatcher("/views/organizerDashboard.jsp").forward(request, response);

        } catch (SQLException e) {
            ServletUtil.logError("Database error in OrganizerServlet.doGet", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Database operation failed. Please try again later.");
        } catch (Exception e) {
            ServletUtil.logError("Unexpected error in OrganizerServlet.doGet", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Handles POST requests to create new events.
     * 
     * @param request  The HTTP request
     * @param response The HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        try {
            // Verify session validity
            if (!ServletUtil.isSessionValid(session)) {
                ServletUtil.logSecurityEvent("Invalid session access to OrganizerServlet", session);
                ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Session invalid. Please log in again.");
                return;
            }

            // Verify organizer or admin role
            if (!verifyOrganizerOrAdminRole(session, response)) {
                return;
            }

            Integer userId = ServletUtil.getUserIdFromSession(session);

            // Handle event creation
            handleCreateEvent(request, response, userId, session);

        } catch (ValidationException e) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            ServletUtil.logError("Database error in OrganizerServlet.doPost", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Database operation failed. Please try again later.");
        } catch (Exception e) {
            ServletUtil.logError("Unexpected error in OrganizerServlet.doPost", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Handles PUT requests to update existing events.
     * 
     * @param request  The HTTP request
     * @param response The HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        try {
            // Verify session validity
            if (!ServletUtil.isSessionValid(session)) {
                ServletUtil.logSecurityEvent("Invalid session access to OrganizerServlet", session);
                ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Session invalid. Please log in again.");
                return;
            }

            // Verify organizer or admin role
            if (!verifyOrganizerOrAdminRole(session, response)) {
                return;
            }

            Integer userId = ServletUtil.getUserIdFromSession(session);
            String userRole = ServletUtil.getUserRoleFromSession(session);

            // Handle event update
            handleUpdateEvent(request, response, userId, userRole, session);

        } catch (ValidationException e) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            ServletUtil.logError("Database error in OrganizerServlet.doPut", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Database operation failed. Please try again later.");
        } catch (Exception e) {
            ServletUtil.logError("Unexpected error in OrganizerServlet.doPut", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Handles DELETE requests to delete events.
     * 
     * @param request  The HTTP request
     * @param response The HTTP response
     * @throws ServletException if a servlet error occurs
     * @throws IOException      if an I/O error occurs
     */
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        
        try {
            // Verify session validity
            if (!ServletUtil.isSessionValid(session)) {
                ServletUtil.logSecurityEvent("Invalid session access to OrganizerServlet", session);
                ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Session invalid. Please log in again.");
                return;
            }

            // Verify organizer or admin role
            if (!verifyOrganizerOrAdminRole(session, response)) {
                return;
            }

            Integer userId = ServletUtil.getUserIdFromSession(session);
            String userRole = ServletUtil.getUserRoleFromSession(session);

            // Handle event deletion
            handleDeleteEvent(request, response, userId, userRole, session);

        } catch (ValidationException e) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            ServletUtil.logError("Database error in OrganizerServlet.doDelete", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Database operation failed. Please try again later.");
        } catch (Exception e) {
            ServletUtil.logError("Unexpected error in OrganizerServlet.doDelete", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Verifies that the user has ORGANIZER or ADMIN role.
     * 
     * @param session  The HTTP session
     * @param response The HTTP response
     * @return true if user has required role, false otherwise
     * @throws IOException if an I/O error occurs
     */
    private boolean verifyOrganizerOrAdminRole(HttpSession session, HttpServletResponse response)
            throws IOException {
        
        String userRole = ServletUtil.getUserRoleFromSession(session);
        
        if (!"ORGANIZER".equalsIgnoreCase(userRole) && !"ADMIN".equalsIgnoreCase(userRole)) {
            ServletUtil.logSecurityEvent("Unauthorized access attempt to OrganizerServlet by role: " + userRole, session);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                "Access denied. Organizer or Admin role required.");
            return false;
        }
        
        return true;
    }

    /**
     * Retrieves events for the organizer.
     * Admins can see all events, organizers can only see their own events.
     * 
     * @param userId   The user ID
     * @param userRole The user role
     * @return List of events
     * @throws SQLException if a database error occurs
     */
    private List<Event> handleGetOrganizerEvents(Integer userId, String userRole) throws SQLException {
        List<Event> allEvents = eventDAO.getAll();
        
        // If admin, return all events
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            return allEvents;
        }
        
        // If organizer, filter to only their events
        List<Event> organizerEvents = new ArrayList<>();
        for (Event event : allEvents) {
            if (event.getOrganizerId() == userId) {
                organizerEvents.add(event);
            }
        }
        
        return organizerEvents;
    }

    /**
     * Handles event creation.
     * 
     * @param request  The HTTP request
     * @param response The HTTP response
     * @param userId   The user ID (organizer)
     * @param session  The HTTP session
     * @throws ValidationException if validation fails
     * @throws SQLException        if a database error occurs
     * @throws IOException         if an I/O error occurs
     */
    private void handleCreateEvent(HttpServletRequest request, HttpServletResponse response,
                                   Integer userId, HttpSession session)
            throws ValidationException, SQLException, IOException {
        
        // Get and sanitize parameters
        String title = ValidationUtil.sanitizeInput(request.getParameter("title"));
        String description = ValidationUtil.sanitizeInput(request.getParameter("description"));
        String dateStr = request.getParameter("date");
        String timeStr = request.getParameter("time");
        String venue = ValidationUtil.sanitizeInput(request.getParameter("venue"));

        // Validate required fields
        List<String> errors = ValidationUtil.validateEventData(title, dateStr, venue);
        
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join(", ", errors));
        }

        // Parse and validate date
        LocalDate eventDate = ValidationUtil.validateDateFormat(dateStr);
        if (!ValidationUtil.validateFutureDate(eventDate)) {
            throw new ValidationException("Event date must be in the future");
        }

        // Parse time (default to 00:00 if not provided)
        LocalTime eventTime = LocalTime.of(0, 0);
        if (timeStr != null && !timeStr.trim().isEmpty()) {
            try {
                eventTime = LocalTime.parse(timeStr);
            } catch (Exception e) {
                throw new ValidationException("Invalid time format. Use HH:mm format.");
            }
        }

        // Create event object
        Event event = new Event();
        event.setOrganizerId(userId);
        event.setTitle(title);
        event.setDescription(description != null ? description : "");
        event.setDate(eventDate);
        event.setTime(eventTime);
        event.setVenue(venue);
        event.setStatus("PENDING"); // New events start as PENDING

        // Insert event
        boolean success = eventDAO.insert(event);
        
        if (success) {
            logger.info("Event created successfully by user " + userId + ": " + title);
            ServletUtil.sendSuccessResponse(response, HttpServletResponse.SC_CREATED,
                "Event created successfully");
        } else {
            throw new SQLException("Failed to create event");
        }
    }

    /**
     * Handles event update.
     * 
     * @param request  The HTTP request
     * @param response The HTTP response
     * @param userId   The user ID
     * @param userRole The user role
     * @param session  The HTTP session
     * @throws ValidationException if validation fails
     * @throws SQLException        if a database error occurs
     * @throws IOException         if an I/O error occurs
     */
    private void handleUpdateEvent(HttpServletRequest request, HttpServletResponse response,
                                   Integer userId, String userRole, HttpSession session)
            throws ValidationException, SQLException, IOException {
        
        // Get event ID
        String eventIdStr = request.getParameter("eventId");
        if (eventIdStr == null || eventIdStr.trim().isEmpty()) {
            throw new ValidationException("Event ID is required");
        }

        int eventId = ValidationUtil.validatePositiveInteger(eventIdStr, "Event ID");

        // Get existing event
        Event existingEvent = eventDAO.getById(eventId);
        if (existingEvent == null) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                "Event not found");
            return;
        }

        // Verify ownership (organizers can only update their own events, admins can update any)
        if (!verifyEventOwnership(existingEvent, userId, userRole, response)) {
            return;
        }

        // Get and sanitize parameters
        String title = ValidationUtil.sanitizeInput(request.getParameter("title"));
        String description = ValidationUtil.sanitizeInput(request.getParameter("description"));
        String dateStr = request.getParameter("date");
        String timeStr = request.getParameter("time");
        String venue = ValidationUtil.sanitizeInput(request.getParameter("venue"));
        String status = request.getParameter("status");

        // Validate required fields
        List<String> errors = ValidationUtil.validateEventData(title, dateStr, venue);
        
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join(", ", errors));
        }

        // Parse and validate date
        LocalDate eventDate = ValidationUtil.validateDateFormat(dateStr);
        if (!ValidationUtil.validateFutureDate(eventDate)) {
            throw new ValidationException("Event date must be in the future");
        }

        // Parse time
        LocalTime eventTime = LocalTime.of(0, 0);
        if (timeStr != null && !timeStr.trim().isEmpty()) {
            try {
                eventTime = LocalTime.parse(timeStr);
            } catch (Exception e) {
                throw new ValidationException("Invalid time format. Use HH:mm format.");
            }
        }

        // Update event object
        existingEvent.setTitle(title);
        existingEvent.setDescription(description != null ? description : "");
        existingEvent.setDate(eventDate);
        existingEvent.setTime(eventTime);
        existingEvent.setVenue(venue);
        
        // Only admins can change status
        if (status != null && "ADMIN".equalsIgnoreCase(userRole)) {
            existingEvent.setStatus(status);
        }

        // Update event
        boolean success = eventDAO.update(existingEvent);
        
        if (success) {
            logger.info("Event updated successfully by user " + userId + ": " + eventId);
            ServletUtil.sendSuccessResponse(response, HttpServletResponse.SC_OK,
                "Event updated successfully");
        } else {
            throw new SQLException("Failed to update event");
        }
    }

    /**
     * Handles event deletion.
     * 
     * @param request  The HTTP request
     * @param response The HTTP response
     * @param userId   The user ID
     * @param userRole The user role
     * @param session  The HTTP session
     * @throws ValidationException if validation fails
     * @throws SQLException        if a database error occurs
     * @throws IOException         if an I/O error occurs
     */
    private void handleDeleteEvent(HttpServletRequest request, HttpServletResponse response,
                                   Integer userId, String userRole, HttpSession session)
            throws ValidationException, SQLException, IOException {
        
        // Get event ID
        String eventIdStr = request.getParameter("eventId");
        if (eventIdStr == null || eventIdStr.trim().isEmpty()) {
            throw new ValidationException("Event ID is required");
        }

        int eventId = ValidationUtil.validatePositiveInteger(eventIdStr, "Event ID");

        // Get existing event
        Event existingEvent = eventDAO.getById(eventId);
        if (existingEvent == null) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                "Event not found");
            return;
        }

        // Verify ownership (organizers can only delete their own events, admins can delete any)
        if (!verifyEventOwnership(existingEvent, userId, userRole, response)) {
            return;
        }

        // Check for existing bookings
        if (checkExistingBookings(eventId)) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_CONFLICT,
                "Cannot delete event with existing ticket bookings. Please cancel all bookings first.");
            return;
        }

        // Delete event
        boolean success = eventDAO.delete(eventId);
        
        if (success) {
            logger.info("Event deleted successfully by user " + userId + ": " + eventId);
            ServletUtil.sendSuccessResponse(response, HttpServletResponse.SC_NO_CONTENT,
                "Event deleted successfully");
        } else {
            throw new SQLException("Failed to delete event");
        }
    }

    /**
     * Verifies event ownership for update/delete operations.
     * 
     * @param event    The event to verify
     * @param userId   The user ID
     * @param userRole The user role
     * @param response The HTTP response
     * @return true if user owns the event or is admin, false otherwise
     * @throws IOException if an I/O error occurs
     */
    private boolean verifyEventOwnership(Event event, Integer userId, String userRole,
                                        HttpServletResponse response) throws IOException {
        
        // Admins can modify any event
        if ("ADMIN".equalsIgnoreCase(userRole)) {
            return true;
        }

        // Organizers can only modify their own events
        if (event.getOrganizerId() != userId) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                "Access denied. You can only modify your own events.");
            return false;
        }

        return true;
    }

    /**
     * Checks if an event has existing ticket bookings.
     * 
     * @param eventId The event ID
     * @return true if bookings exist, false otherwise
     * @throws SQLException if a database error occurs
     */
    private boolean checkExistingBookings(int eventId) throws SQLException {
        List<Ticket> allTickets = ticketDAO.getAll();
        
        for (Ticket ticket : allTickets) {
            if (ticket.getEventId() == eventId && ticket.getQuantity() > 0) {
                return true;
            }
        }
        
        return false;
    }
}
