package com.eventmgmt.model;

public class Ticket {
    private int id;
    private int eventId;
    private double price;
    private int quantity;

    public Ticket() {}

    public Ticket(int id, int eventId, double price, int quantity) {
        this.id = id;
        this.eventId = eventId;
        this.price = price;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getEventId() { return eventId; }
    public void setEventId(int eventId) { this.eventId = eventId; }

    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
}
