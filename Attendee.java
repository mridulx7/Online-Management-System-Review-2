package com.eventmgmt.model;

public class Attendee extends User {

    public Attendee() {}

    public Attendee(int id, String name, String email, String password) {
        super(id, name, email, password, "ATTENDEE");
    }

    @Override
    public void showDashboard() {
        System.out.println("Attendee Dashboard: View events, tickets, updates.");
    }
}
