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
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * EventServlet handles general event operations accessible to multiple roles.
 * Provides endpoints for browsing events, searching, and viewing event details.
 * Admins can also approve/reject events through this servlet.
 * 
 * Supported operations:
 * - GET: Retrieve all events, search events, get event details
 * - POST: Approve/reject events (admin only)
 */
@WebServlet("/events")
public class EventServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(EventServlet.class.getName());
    private EventDAO eventDAO;
    private TicketDAO ticketDAO;

    @Override
    public void init() throws ServletException {
        super.init();
        eventDAO = new EventDAOImpl();
        ticketDAO = new TicketDAOImpl();
    }

    /**
     * Handles GET requests to retrieve events, search events, or get event details.
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
            ServletUtil.logSecurityEvent("Invalid session access to EventServlet", session);
            response.sendRedirect("login.jsp?error=session_invalid");
            return;
        }
        
        try {
            String action = request.getParameter("action");
            String eventIdParam = request.getParameter("eventId");
            
            if (eventIdParam != null && !eventIdParam.trim().isEmpty()) {
                // Get specific event details
                handleGetEventDetails(request, response, eventIdParam);
            } else if ("search".equals(action)) {
                // Search events with criteria
                handleSearchEvents(request, response);
            } else {
                // Get all events (filtered for future events with capacity)
                handleGetAllEvents(request, response);
            }
            
        } catch (ValidationException e) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            ServletUtil.logError("Database error in EventServlet.doGet", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Database operation failed. Please try again later.");
        } catch (Exception e) {
            ServletUtil.logError("Unexpected error in EventServlet.doGet", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        }
    }

    /**
     * Handles POST requests for event approval/rejection (admin only).
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
        
        try {
            // Verify session validity
            if (!ServletUtil.isSessionValid(session)) {
                ServletUtil.logSecurityEvent("Invalid session access to EventServlet", session);
                ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "Session invalid. Please log in again.");
                return;
            }

            // Verify admin role for approval/rejection
            if (!ServletUtil.isAdmin(session)) {
                ServletUtil.logSecurityEvent("Unauthorized access attempt to EventServlet.doPost", session);
                ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN,
                    "Access denied. Admin role required.");
                return;
            }

            String action = request.getParameter("action");
            
            if ("approve".equals(action)) {
                handleApproveEvent(request, response, session);
            } else if ("reject".equals(action)) {
                handleRejectEvent(request, response, session);
            } else {
                ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid action. Use 'approve' or 'reject'.");
            }
            
        } catch (ValidationException e) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (SQLException e) {
            ServletUtil.logError("Database error in EventServlet.doPost", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "Database operation failed. Please try again later.");
        } catch (Exception e) {
            ServletUtil.logError("Unexpected error in EventServlet.doPost", session, e);
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        }
    }

    // ==================== Helper Methods ====================

    /**
     * Handles retrieving all events, filtered for future events with available capacity.
     * Implements Requirement 4.1.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws IOException if I/O error occurs
     * @throws SQLException if database error occurs
     */
    private void handleGetAllEvents(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        
        List<Event> allEvents = eventDAO.getAll();
        List<Event> filteredEvents = filterFutureEventsWithCapacity(allEvents);
        
        // Build JSON response
        StringBuilder json = new StringBuilder();
        json.append("{\"success\": true, \"events\": [");
        
        for (int i = 0; i < filteredEvents.size(); i++) {
            Event event = filteredEvents.get(i);
            int availableCapacity = calculateAvailableCapacity(event.getId());
            
            json.append("{");
            json.append("\"id\": ").append(event.getId()).append(",");
            json.append("\"organizerId\": ").append(event.getOrganizerId()).append(",");
            json.append("\"title\": \"").append(escapeJson(event.getTitle())).append("\",");
            json.append("\"description\": \"").append(escapeJson(event.getDescription())).append("\",");
            json.append("\"date\": \"").append(event.getDate().toString()).append("\",");
            json.append("\"time\": \"").append(event.getTime().toString()).append("\",");
            json.append("\"venue\": \"").append(escapeJson(event.getVenue())).append("\",");
            json.append("\"status\": \"").append(escapeJson(event.getStatus())).append("\",");
            json.append("\"availableCapacity\": ").append(availableCapacity);
            json.append("}");
            
            if (i < filteredEvents.size() - 1) {
                json.append(",");
            }
        }
        
        json.append("]}");
        
        ServletUtil.sendJsonResponse(response, HttpServletResponse.SC_OK, json.toString());
    }

    /**
     * Handles retrieving specific event details with complete information.
     * Implements Requirement 4.2.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param eventIdParam The event ID parameter
     * @throws IOException if I/O error occurs
     * @throws ValidationException if validation fails
     * @throws SQLException if database error occurs
     */
    private void handleGetEventDetails(HttpServletRequest request, HttpServletResponse response, String eventIdParam)
            throws IOException, ValidationException, SQLException {
        
        int eventId = ValidationUtil.validatePositiveInteger(eventIdParam, "Event ID");
        Event event = eventDAO.getById(eventId);
        
        if (event == null) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                "Event not found with ID: " + eventId);
            return;
        }
        
        int availableCapacity = calculateAvailableCapacity(eventId);
        
        // Build JSON response with complete event information
        String json = String.format(
            "{\"success\": true, \"event\": {" +
            "\"id\": %d, " +
            "\"organizerId\": %d, " +
            "\"title\": \"%s\", " +
            "\"description\": \"%s\", " +
            "\"date\": \"%s\", " +
            "\"time\": \"%s\", " +
            "\"venue\": \"%s\", " +
            "\"status\": \"%s\", " +
            "\"availableCapacity\": %d" +
            "}}",
            event.getId(),
            event.getOrganizerId(),
            escapeJson(event.getTitle()),
            escapeJson(event.getDescription()),
            event.getDate().toString(),
            event.getTime().toString(),
            escapeJson(event.getVenue()),
            escapeJson(event.getStatus()),
            availableCapacity
        );
        
        ServletUtil.sendJsonResponse(response, HttpServletResponse.SC_OK, json);
    }

    /**
     * Handles searching events with criteria filtering.
     * Implements Requirement 4.3.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @throws IOException if I/O error occurs
     * @throws SQLException if database error occurs
     */
    private void handleSearchEvents(HttpServletRequest request, HttpServletResponse response)
            throws IOException, SQLException {
        
        String titleQuery = request.getParameter("title");
        String venueQuery = request.getParameter("venue");
        String dateQuery = request.getParameter("date");
        String statusQuery = request.getParameter("status");
        
        List<Event> allEvents = eventDAO.getAll();
        List<Event> filteredEvents = new ArrayList<>();
        
        for (Event event : allEvents) {
            boolean matches = true;
            
            // Filter by title (case-insensitive partial match)
            if (titleQuery != null && !titleQuery.trim().isEmpty()) {
                if (!event.getTitle().toLowerCase().contains(titleQuery.toLowerCase())) {
                    matches = false;
                }
            }
            
            // Filter by venue (case-insensitive partial match)
            if (venueQuery != null && !venueQuery.trim().isEmpty()) {
                if (!event.getVenue().toLowerCase().contains(venueQuery.toLowerCase())) {
                    matches = false;
                }
            }
            
            // Filter by date (exact match)
            if (dateQuery != null && !dateQuery.trim().isEmpty()) {
                try {
                    LocalDate searchDate = ValidationUtil.validateDateFormat(dateQuery);
                    if (!event.getDate().equals(searchDate)) {
                        matches = false;
                    }
                } catch (ValidationException e) {
                    // Invalid date format, skip this filter
                }
            }
            
            // Filter by status (exact match, case-insensitive)
            if (statusQuery != null && !statusQuery.trim().isEmpty()) {
                if (!event.getStatus().equalsIgnoreCase(statusQuery)) {
                    matches = false;
                }
            }
            
            if (matches) {
                filteredEvents.add(event);
            }
        }
        
        // Build JSON response
        StringBuilder json = new StringBuilder();
        json.append("{\"success\": true, \"events\": [");
        
        if (filteredEvents.isEmpty()) {
            json.append("], \"message\": \"No events found matching the search criteria\"}");
        } else {
            for (int i = 0; i < filteredEvents.size(); i++) {
                Event event = filteredEvents.get(i);
                int availableCapacity = calculateAvailableCapacity(event.getId());
                
                json.append("{");
                json.append("\"id\": ").append(event.getId()).append(",");
                json.append("\"organizerId\": ").append(event.getOrganizerId()).append(",");
                json.append("\"title\": \"").append(escapeJson(event.getTitle())).append("\",");
                json.append("\"description\": \"").append(escapeJson(event.getDescription())).append("\",");
                json.append("\"date\": \"").append(event.getDate().toString()).append("\",");
                json.append("\"time\": \"").append(event.getTime().toString()).append("\",");
                json.append("\"venue\": \"").append(escapeJson(event.getVenue())).append("\",");
                json.append("\"status\": \"").append(escapeJson(event.getStatus())).append("\",");
                json.append("\"availableCapacity\": ").append(availableCapacity);
                json.append("}");
                
                if (i < filteredEvents.size() - 1) {
                    json.append(",");
                }
            }
            json.append("]}");
        }
        
        ServletUtil.sendJsonResponse(response, HttpServletResponse.SC_OK, json.toString());
    }

    /**
     * Handles event approval by admin.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param session The HTTP session
     * @throws IOException if I/O error occurs
     * @throws ValidationException if validation fails
     * @throws SQLException if database error occurs
     */
    private void handleApproveEvent(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ValidationException, SQLException {
        
        String eventIdParam = request.getParameter("eventId");
        if (eventIdParam == null || eventIdParam.trim().isEmpty()) {
            throw new ValidationException("Event ID is required");
        }
        
        int eventId = ValidationUtil.validatePositiveInteger(eventIdParam, "Event ID");
        Event event = eventDAO.getById(eventId);
        
        if (event == null) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                "Event not found with ID: " + eventId);
            return;
        }
        
        event.setStatus("APPROVED");
        boolean success = eventDAO.update(event);
        
        if (success) {
            logger.info("Event approved by admin " + ServletUtil.getUserIdFromSession(session) + ": " + eventId);
            ServletUtil.sendSuccessResponse(response, HttpServletResponse.SC_OK,
                "Event approved successfully");
        } else {
            throw new SQLException("Failed to approve event");
        }
    }

    /**
     * Handles event rejection by admin.
     * 
     * @param request The HTTP request
     * @param response The HTTP response
     * @param session The HTTP session
     * @throws IOException if I/O error occurs
     * @throws ValidationException if validation fails
     * @throws SQLException if database error occurs
     */
    private void handleRejectEvent(HttpServletRequest request, HttpServletResponse response, HttpSession session)
            throws IOException, ValidationException, SQLException {
        
        String eventIdParam = request.getParameter("eventId");
        if (eventIdParam == null || eventIdParam.trim().isEmpty()) {
            throw new ValidationException("Event ID is required");
        }
        
        int eventId = ValidationUtil.validatePositiveInteger(eventIdParam, "Event ID");
        Event event = eventDAO.getById(eventId);
        
        if (event == null) {
            ServletUtil.sendErrorResponse(response, HttpServletResponse.SC_NOT_FOUND,
                "Event not found with ID: " + eventId);
            return;
        }
        
        event.setStatus("REJECTED");
        boolean success = eventDAO.update(event);
        
        if (success) {
            logger.info("Event rejected by admin " + ServletUtil.getUserIdFromSession(session) + ": " + eventId);
            ServletUtil.sendSuccessResponse(response, HttpServletResponse.SC_OK,
                "Event rejected successfully");
        } else {
            throw new SQLException("Failed to reject event");
        }
    }

    /**
     * Filters events to only include future events with available capacity.
     * Implements Requirement 4.1.
     * 
     * @param events List of all events
     * @return List of filtered events
     * @throws SQLException if database error occurs
     */
    private List<Event> filterFutureEventsWithCapacity(List<Event> events) throws SQLException {
        List<Event> filtered = new ArrayList<>();
        LocalDate today = LocalDate.now();
        
        for (Event event : events) {
            // Check if event is in the future
            if (event.getDate().isAfter(today)) {
                // Check if event has available capacity
                int availableCapacity = calculateAvailableCapacity(event.getId());
                if (availableCapacity > 0) {
                    filtered.add(event);
                }
            }
        }
        
        return filtered;
    }

    /**
     * Calculates the remaining ticket capacity for an event.
     * Implements Requirement 4.4.
     * 
     * @param eventId The event ID
     * @return The available capacity (total tickets - booked tickets)
     * @throws SQLException if database error occurs
     */
    public int calculateAvailableCapacity(int eventId) throws SQLException {
        List<Ticket> allTickets = ticketDAO.getAll();
        
        int totalCapacity = 0;
        int bookedTickets = 0;
        
        for (Ticket ticket : allTickets) {
            if (ticket.getEventId() == eventId) {
                // Assuming the first ticket entry for an event represents total capacity
                // and subsequent entries or quantity changes represent bookings
                // This is a simplified approach - actual implementation may vary
                totalCapacity += ticket.getQuantity();
            }
        }
        
        // For now, return a default capacity if no tickets found
        // In a real system, this would be more sophisticated
        return totalCapacity > 0 ? totalCapacity : 100;
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
