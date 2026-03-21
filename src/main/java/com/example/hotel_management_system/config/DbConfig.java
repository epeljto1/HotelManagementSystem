package com.example.hotel_management_system.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Configuration
public class DbConfig {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUsername;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    private static String URL;
    private static String USER;
    private static String PASS;

    @PostConstruct
    public void init() {
        URL = this.dbUrl;
        USER = this.dbUsername;
        PASS = this.dbPassword;
    }

    public static Connection getConnection() throws SQLException {
        if (URL == null) {
            throw new SQLException("Database configuration not initialized!");
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }
}