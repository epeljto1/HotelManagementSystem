package com.example.hotel_management_system.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConfig {
    private static final String DB_URL = "jdbc:oracle:thin:@ora-02.db.lab.etf.unsa.ba:1521:ETFDB";
    private static final String DB_USERNAME = "NBPT7";
    private static final String DB_PASSWORD = "nbpt7";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
    }
}