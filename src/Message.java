package com.eventmgmt.model;

import java.time.LocalDateTime;

public class Message {
    private int id;
    private int organizerId;
    private int eventId;
    private String content;
    private LocalDateTime sentAt;

    public Message() {}

    public Message(int id, int organizerId, int eventId, String content, LocalDateTime sentAt) {
        this.id = id;
        this.organizerId = organizerId;
        this.eventId = eventId;
        this.content = content;
        this.sentAt = sentAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOrganizerId() { return organizerId; }
    public void setOrganizerId(int organizerId) { this.organizerId = organizerId; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getSentAt() { return sentAt; }
    public void setSentAt(LocalDateTime sentAt) { this.sentAt = sentAt; }
}
