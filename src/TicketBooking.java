package com.eventmgmt.thread;

public class TicketBooking {

    private int availableTickets = 50;

    public synchronized void book(int qty) {
        if (availableTickets >= qty) {
            System.out.println(Thread.currentThread().getName() + " booked " + qty + " tickets.");
            availableTickets -= qty;
        } else {
            System.out.println(Thread.currentThread().getName() + " failed to book, not enough tickets.");
        }

        System.out.println("Tickets left: " + availableTickets);
    }
}
