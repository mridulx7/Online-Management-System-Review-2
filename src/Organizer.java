package com.eventmgmt.model;

public class Organizer extends User {

    public Organizer() {}

    public Organizer(int id, String name, String email, String password) {
        super(id, name, email, password, "ORGANIZER");
    }

    @Override
    public void showDashboard() {
        System.out.println("Organizer Dashboard: Manage events, tickets, attendees.");
    }
}
