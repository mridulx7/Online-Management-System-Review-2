package com.eventmgmt.model;

import java.time.LocalDate;
import java.time.LocalTime;

public class Event {
    private int id;
    private int organizerId;
    private String title;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private String venue;
    private String status; // PENDING, APPROVED, REJECTED

    public Event() {}

    public Event(int id, int organizerId, String title, String description,
                 LocalDate date, LocalTime time, String venue, String status) {
        this.id = id;
        this.organizerId = organizerId;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.venue = venue;
        this.status = status;
    }

    // Getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrganizerId() { return organizerId; }
    public void setOrganizerId(int organizerId) { this.organizerId = organizerId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime() { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
