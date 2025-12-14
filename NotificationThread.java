package com.eventmgmt.thread;

public class NotificationThread extends Thread {

    private String message;

    public NotificationThread(String message) {
        this.message = message;
    }

    @Override
    public void run() {
        // Simulate sending message to attendees
        for (int i = 1; i <= 5; i++) {
            System.out.println("Sending notification: " + message + " | Batch " + i);

            try {
                Thread.sleep(500); // pause for realism
            } catch (Exception e) {
                System.out.println("Thread error: " + e.getMessage());
            }
        }

        System.out.println("Notification sending completed.");
    }
}
