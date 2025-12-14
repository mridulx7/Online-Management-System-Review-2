package com.eventmgmt.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DBConnection {

    private static final String URL = "jdbc:mysql://localhost:3306/eventdb";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "GreaterNoida@201310";

    private static Connection connection = null;

    private DBConnection() {}

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
                System.out.println("Database connected successfully.");
            } catch (Exception e) {
                System.out.println("Error connecting to database.");
                e.printStackTrace();
            }
        }
        return connection;
    }
}
