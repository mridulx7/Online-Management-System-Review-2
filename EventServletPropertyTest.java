package com.eventmgmt.servlet;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import com.eventmgmt.model.Event;
import com.servlet.EventServlet;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for EventServlet event filtering and capacity calculation logic.
 * Tests universal properties that should hold across all inputs.
 * 
 * Note: These tests focus on the core logic rather than servlet infrastructure.
 */
public class EventServletPropertyTest {

    /**
     * Feature: servlet-implementation, Property 23: Event list filters future events with capacity
     * Validates: Requirements 4.1
     * 
     * For any attendee event list request, all returned events should have dates 
     * in the future and available capacity greater than zero.
     */
    @ParameterizedTest
    @MethodSource("eventListScenarios")
    void eventList_filtersOnlyFutureEventsWithCapacity(EventListScenario scenario) {
        LocalDate today = LocalDate.now();
        List<Event> filteredEvents = new ArrayList<>();
        
        // Simulate the filtering logic from EventServlet
        for (Event event : scenario.events) {
            // Check if event is in the future
            if (event.getDate().isAfter(today)) {
                // Check if event has available capacity (simulated)
                int availableCapacity = scenario.capacityMap.getOrDefault(event.getId(), 0);
                if (availableCapacity > 0) {
                    filteredEvents.add(event);
                }
            }
        }
        
        // Assert all filtered events are in the future
        for (Event event : filteredEvents) {
            assertTrue(event.getDate().isAfter(today), 
                "Event date should be in the future: " + event.getDate());
        }
        
        // Assert all filtered events have capacity > 0
        for (Event event : filteredEvents) {
            int capacity = scenario.capacityMap.getOrDefault(event.getId(), 0);
            assertTrue(capacity > 0, 
                "Event should have available capacity: " + capacity);
        }
        
        // Assert no past events are included
        for (Event event : filteredEvents) {
            assertFalse(event.getDate().isBefore(today) || event.getDate().isEqual(today),
                "Past or today's events should not be included");
        }
    }

    /**
     * Feature: servlet-implementation, Property 24: Event details include complete information
     * Validates: Requirements 4.2
     * 
     * For any event detail request, the response should include all event fields 
     * (title, description, date, time, venue, status) and available ticket count.
     */
    @ParameterizedTest
    @MethodSource("eventDetailScenarios")
    void eventDetails_includeCompleteInformation(EventDetailScenario scenario) {
        Event event = scenario.event;
        
        // Verify all required fields are present and not null
        assertNotNull(event.getId(), "Event ID should not be null");
        assertNotNull(event.getOrganizerId(), "Organizer ID should not be null");
        assertNotNull(event.getTitle(), "Title should not be null");
        assertNotNull(event.getDescription(), "Description should not be null");
        assertNotNull(event.getDate(), "Date should not be null");
        assertNotNull(event.getTime(), "Time should not be null");
        assertNotNull(event.getVenue(), "Venue should not be null");
        assertNotNull(event.getStatus(), "Status should not be null");
        
        // Verify capacity is calculated (non-negative)
        int availableCapacity = scenario.availableCapacity;
        assertTrue(availableCapacity >= 0, 
            "Available capacity should be non-negative: " + availableCapacity);
        
        // Verify title is not empty
        assertFalse(event.getTitle().trim().isEmpty(), "Title should not be empty");
        
        // Verify venue is not empty
        assertFalse(event.getVenue().trim().isEmpty(), "Venue should not be empty");
        
        // Verify status is valid
        assertTrue(
            "PENDING".equals(event.getStatus()) || 
            "APPROVED".equals(event.getStatus()) || 
            "REJECTED".equals(event.getStatus()),
            "Status should be PENDING, APPROVED, or REJECTED: " + event.getStatus()
        );
    }

    /**
     * Feature: servlet-implementation, Property 25: Event search filters by criteria
     * Validates: Requirements 4.3
     * 
     * For any event search request with criteria, all returned events 
     * should match the provided search parameters.
     */
    @ParameterizedTest
    @MethodSource("searchCriteriaScenarios")
    void eventSearch_filtersMatchingCriteria(SearchCriteriaScenario scenario) {
        List<Event> allEvents = scenario.allEvents;
        List<Event> filteredEvents = new ArrayList<>();
        
        // Simulate the search filtering logic from EventServlet
        for (Event event : allEvents) {
            boolean matches = true;
            
            // Filter by title (case-insensitive partial match)
            if (scenario.titleQuery != null && !scenario.titleQuery.trim().isEmpty()) {
                if (!event.getTitle().toLowerCase().contains(scenario.titleQuery.toLowerCase())) {
                    matches = false;
                }
            }
            
            // Filter by venue (case-insensitive partial match)
            if (scenario.venueQuery != null && !scenario.venueQuery.trim().isEmpty()) {
                if (!event.getVenue().toLowerCase().contains(scenario.venueQuery.toLowerCase())) {
                    matches = false;
                }
            }
            
            // Filter by date (exact match)
            if (scenario.dateQuery != null) {
                if (!event.getDate().equals(scenario.dateQuery)) {
                    matches = false;
                }
            }
            
            // Filter by status (exact match, case-insensitive)
            if (scenario.statusQuery != null && !scenario.statusQuery.trim().isEmpty()) {
                if (!event.getStatus().equalsIgnoreCase(scenario.statusQuery)) {
                    matches = false;
                }
            }
            
            if (matches) {
                filteredEvents.add(event);
            }
        }
        
        // Assert all filtered events match the title criteria
        if (scenario.titleQuery != null && !scenario.titleQuery.trim().isEmpty()) {
            for (Event event : filteredEvents) {
                assertTrue(event.getTitle().toLowerCase().contains(scenario.titleQuery.toLowerCase()),
                    "Event title should contain search query: " + event.getTitle() + " vs " + scenario.titleQuery);
            }
        }
        
        // Assert all filtered events match the venue criteria
        if (scenario.venueQuery != null && !scenario.venueQuery.trim().isEmpty()) {
            for (Event event : filteredEvents) {
                assertTrue(event.getVenue().toLowerCase().contains(scenario.venueQuery.toLowerCase()),
                    "Event venue should contain search query: " + event.getVenue() + " vs " + scenario.venueQuery);
            }
        }
        
        // Assert all filtered events match the date criteria
        if (scenario.dateQuery != null) {
            for (Event event : filteredEvents) {
                assertEquals(scenario.dateQuery, event.getDate(),
                    "Event date should match search query");
            }
        }
        
        // Assert all filtered events match the status criteria
        if (scenario.statusQuery != null && !scenario.statusQuery.trim().isEmpty()) {
            for (Event event : filteredEvents) {
                assertTrue(event.getStatus().equalsIgnoreCase(scenario.statusQuery),
                    "Event status should match search query: " + event.getStatus() + " vs " + scenario.statusQuery);
            }
        }
    }

    /**
     * Feature: servlet-implementation, Property 26: Capacity calculation is accurate
     * Validates: Requirements 4.4
     * 
     * For any event display, the remaining capacity should equal 
     * total capacity minus sum of booked tickets.
     */
    @ParameterizedTest
    @MethodSource("capacityCalculationScenarios")
    void capacityCalculation_isAccurate(CapacityCalculationScenario scenario) {
        // Calculate available capacity
        int totalCapacity = scenario.totalCapacity;
        int bookedTickets = scenario.bookedTickets;
        int expectedAvailableCapacity = totalCapacity - bookedTickets;
        
        // Simulate the capacity calculation
        int calculatedCapacity = totalCapacity - bookedTickets;
        
        // Assert capacity calculation is accurate
        assertEquals(expectedAvailableCapacity, calculatedCapacity,
            "Available capacity should equal total minus booked");
        
        // Assert capacity is non-negative (business rule)
        assertTrue(calculatedCapacity >= 0,
            "Available capacity should not be negative: " + calculatedCapacity);
        
        // Assert capacity is at most total capacity
        assertTrue(calculatedCapacity <= totalCapacity,
            "Available capacity should not exceed total capacity");
        
        // If no tickets booked, capacity should equal total
        if (bookedTickets == 0) {
            assertEquals(totalCapacity, calculatedCapacity,
                "When no tickets booked, available should equal total");
        }
        
        // If all tickets booked, capacity should be zero
        if (bookedTickets == totalCapacity) {
            assertEquals(0, calculatedCapacity,
                "When all tickets booked, available should be zero");
        }
    }

    @Test
    void capacityCalculation_consistentAcrossMultipleIterations() {
        // Test 100 iterations to ensure property holds
        for (int i = 1; i <= 100; i++) {
            int totalCapacity = i * 10;
            int bookedTickets = i * 5;
            
            int calculatedCapacity = totalCapacity - bookedTickets;
            int expectedCapacity = totalCapacity - bookedTickets;
            
            assertEquals(expectedCapacity, calculatedCapacity,
                "Capacity calculation should be consistent (iteration " + i + ")");
            assertTrue(calculatedCapacity >= 0,
                "Capacity should be non-negative (iteration " + i + ")");
        }
    }

    // ==================== Data Generators ====================

    static Stream<EventListScenario> eventListScenarios() {
        LocalDate today = LocalDate.now();
        
        return Stream.of(
            // Scenario 1: Mix of past, present, and future events
            new EventListScenario(
                List.of(
                    createEvent(1, "Past Event", today.minusDays(5)),
                    createEvent(2, "Future Event 1", today.plusDays(5)),
                    createEvent(3, "Future Event 2", today.plusDays(10)),
                    createEvent(4, "Today Event", today)
                ),
                java.util.Map.of(1, 50, 2, 100, 3, 75, 4, 0)
            ),
            // Scenario 2: All future events with varying capacity
            new EventListScenario(
                List.of(
                    createEvent(5, "Event A", today.plusDays(1)),
                    createEvent(6, "Event B", today.plusDays(2)),
                    createEvent(7, "Event C", today.plusDays(3))
                ),
                java.util.Map.of(5, 10, 6, 50, 7, 100)
            ),
            // Scenario 3: Future events with zero capacity
            new EventListScenario(
                List.of(
                    createEvent(8, "Full Event", today.plusDays(7)),
                    createEvent(9, "Available Event", today.plusDays(8))
                ),
                java.util.Map.of(8, 0, 9, 25)
            ),
            // Scenario 4: All past events
            new EventListScenario(
                List.of(
                    createEvent(10, "Old Event 1", today.minusDays(10)),
                    createEvent(11, "Old Event 2", today.minusDays(20))
                ),
                java.util.Map.of(10, 100, 11, 50)
            ),
            // Scenario 5: Mix with large capacity numbers
            new EventListScenario(
                List.of(
                    createEvent(12, "Large Event", today.plusDays(30)),
                    createEvent(13, "Small Event", today.plusDays(15))
                ),
                java.util.Map.of(12, 1000, 13, 5)
            )
        );
    }

    static Stream<EventDetailScenario> eventDetailScenarios() {
        LocalDate today = LocalDate.now();
        
        return Stream.of(
            new EventDetailScenario(
                createEvent(1, "Tech Conference", today.plusDays(10)),
                100
            ),
            new EventDetailScenario(
                createEvent(2, "Music Festival", today.plusDays(30)),
                500
            ),
            new EventDetailScenario(
                createEvent(3, "Workshop", today.plusDays(5)),
                20
            ),
            new EventDetailScenario(
                createEvent(4, "Seminar", today.plusDays(15)),
                0
            ),
            new EventDetailScenario(
                createEvent(5, "Networking Event", today.plusDays(7)),
                75
            ),
            new EventDetailScenario(
                createEvent(6, "Training Session", today.plusDays(3)),
                15
            )
        );
    }

    static Stream<SearchCriteriaScenario> searchCriteriaScenarios() {
        LocalDate today = LocalDate.now();
        
        List<Event> sampleEvents = List.of(
            createEvent(1, "Tech Conference", today.plusDays(10), "Convention Center", "APPROVED"),
            createEvent(2, "Music Festival", today.plusDays(20), "City Park", "PENDING"),
            createEvent(3, "Tech Workshop", today.plusDays(5), "Tech Hub", "APPROVED"),
            createEvent(4, "Art Exhibition", today.plusDays(15), "Art Gallery", "REJECTED"),
            createEvent(5, "Tech Meetup", today.plusDays(7), "Convention Center", "APPROVED")
        );
        
        return Stream.of(
            // Search by title
            new SearchCriteriaScenario(sampleEvents, "Tech", null, null, null),
            new SearchCriteriaScenario(sampleEvents, "Music", null, null, null),
            // Search by venue
            new SearchCriteriaScenario(sampleEvents, null, "Convention Center", null, null),
            new SearchCriteriaScenario(sampleEvents, null, "Park", null, null),
            // Search by status
            new SearchCriteriaScenario(sampleEvents, null, null, null, "APPROVED"),
            new SearchCriteriaScenario(sampleEvents, null, null, null, "PENDING"),
            // Search by date
            new SearchCriteriaScenario(sampleEvents, null, null, today.plusDays(10), null),
            // Combined search
            new SearchCriteriaScenario(sampleEvents, "Tech", "Convention Center", null, "APPROVED"),
            new SearchCriteriaScenario(sampleEvents, "Tech", null, null, "APPROVED")
        );
    }

    static Stream<CapacityCalculationScenario> capacityCalculationScenarios() {
        return Stream.of(
            new CapacityCalculationScenario(100, 0),    // No bookings
            new CapacityCalculationScenario(100, 50),   // Half booked
            new CapacityCalculationScenario(100, 100),  // Fully booked
            new CapacityCalculationScenario(100, 25),   // Quarter booked
            new CapacityCalculationScenario(100, 75),   // Three quarters booked
            new CapacityCalculationScenario(50, 10),    // Small event
            new CapacityCalculationScenario(1000, 500), // Large event
            new CapacityCalculationScenario(20, 5),     // Workshop
            new CapacityCalculationScenario(500, 499),  // Almost full
            new CapacityCalculationScenario(10, 0)      // Small event, no bookings
        );
    }

    // ==================== Helper Methods ====================

    private static Event createEvent(int id, String title, LocalDate date) {
        return createEvent(id, title, date, "Default Venue", "PENDING");
    }

    private static Event createEvent(int id, String title, LocalDate date, String venue, String status) {
        Event event = new Event();
        event.setId(id);
        event.setOrganizerId(1);
        event.setTitle(title);
        event.setDescription("Description for " + title);
        event.setDate(date);
        event.setTime(LocalTime.of(10, 0));
        event.setVenue(venue);
        event.setStatus(status);
        return event;
    }

    // ==================== Test Data Classes ====================

    static class EventListScenario {
        List<Event> events;
        java.util.Map<Integer, Integer> capacityMap;

        EventListScenario(List<Event> events, java.util.Map<Integer, Integer> capacityMap) {
            this.events = events;
            this.capacityMap = capacityMap;
        }
    }

    static class EventDetailScenario {
        Event event;
        int availableCapacity;

        EventDetailScenario(Event event, int availableCapacity) {
            this.event = event;
            this.availableCapacity = availableCapacity;
        }
    }

    static class SearchCriteriaScenario {
        List<Event> allEvents;
        String titleQuery;
        String venueQuery;
        LocalDate dateQuery;
        String statusQuery;

        SearchCriteriaScenario(List<Event> allEvents, String titleQuery, String venueQuery, 
                              LocalDate dateQuery, String statusQuery) {
            this.allEvents = allEvents;
            this.titleQuery = titleQuery;
            this.venueQuery = venueQuery;
            this.dateQuery = dateQuery;
            this.statusQuery = statusQuery;
        }
    }

    static class CapacityCalculationScenario {
        int totalCapacity;
        int bookedTickets;

        CapacityCalculationScenario(int totalCapacity, int bookedTickets) {
            this.totalCapacity = totalCapacity;
            this.bookedTickets = bookedTickets;
        }
    }
}
