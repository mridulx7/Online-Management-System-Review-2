package com.eventmgmt.dao;

import com.eventmgmt.model.Registration;
import java.util.List;

public interface RegistrationDAO extends GenericDAO<Registration> {
    /**
     * Get all registrations for a specific user.
     * 
     * @param userId The user ID
     * @return List of registrations for the user
     */
    List<Registration> getByUserId(int userId);
    
    /**
     * Get all registrations for a specific event.
     * 
     * @param eventId The event ID
     * @return List of registrations for the event
     */
    List<Registration> getByEventId(int eventId);
    
    /**
     * Get a specific registration by user and event.
     * 
     * @param userId The user ID
     * @param eventId The event ID
     * @return The registration, or null if not found
     */
    Registration getByUserAndEvent(int userId, int eventId);
}
