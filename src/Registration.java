package com.eventmgmt.model;

public class Registration {
    private int id;
    private int userId;
    private int eventId;
    private int ticketId;
    private String status; // REGISTERED, CANCELLED

    public Registration() {}

    public Registration(int id, int userId, int eventId, int ticketId, String status) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.ticketId = ticketId;
        this.status = status;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
