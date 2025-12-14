package com.eventmgmt.model;

public interface EventActions {
    void createEvent(Event event);
    void updateEvent(Event event);
    void cancelEvent(int eventId);
}
