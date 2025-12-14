package com.eventmgmt.main;

import com.eventmgmt.dao.*;
import com.eventmgmt.model.*;
import com.eventmgmt.thread.NotificationThread;
import com.eventmgmt.thread.TicketBooking;
import com.eventmgmt.util.DBConnection;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


public class MainApp {
    public static void main(String[] args) {

        // Test DB connection
        Connection conn = DBConnection.getConnection();
        System.out.println("----- DAO Testing Started -----");

        // USER DAO
        UserDAO userDAO = new UserDAOImpl();

        User admin = new Admin(0, "Admin One", "admin@test.com", "12345");
        userDAO.insert(admin);

        List<User> users = userDAO.getAll();
        System.out.println("Users List:");
        for (User u : users) {
            System.out.println(u.getId() + " | " + u.getName() + " | " + u.getRole());
        }

        // EVENT DAO
        EventDAO eventDAO = new EventDAOImpl();

        Event event = new Event(
                0,
                1,
                "Tech Conference",
                "Technology and Innovation",
                LocalDate.now(),
                LocalTime.now(),
                "Hall A",
                "PENDING"
        );

        eventDAO.insert(event);

        List<Event> events = eventDAO.getAll();
        System.out.println("\nEvents List:");
        for (Event e : events) {
            System.out.println(e.getId() + " | " + e.getTitle() + " | " + e.getStatus());
        }

        // TICKET DAO
        TicketDAO ticketDAO = new TicketDAOImpl();

        Ticket ticket = new Ticket(0, 1, 499.0, 100);
        ticketDAO.insert(ticket);

        List<Ticket> tickets = ticketDAO.getAll();
        System.out.println("\nTickets List:");
        for (Ticket t : tickets) {
            System.out.println(t.getId() + " | " + t.getPrice() + " | " + t.getQuantity());
        }

        System.out.println("----- DAO Testing Completed -----");
        //----------------------------------------------------------------------------
        System.out.println("\n----- Multithreading Testing -----");

        // Thread Test 1: Notification Thread
        NotificationThread nt = new NotificationThread("Your event is starting soon!");
        nt.start();

        // Thread Test 2: Synchronized Ticket Booking
        TicketBooking booking = new TicketBooking();

        Thread t1 = new Thread(() -> booking.book(30), "User-1");
        Thread t2 = new Thread(() -> booking.book(25), "User-2");

        t1.start();
        t2.start();

    }
}

