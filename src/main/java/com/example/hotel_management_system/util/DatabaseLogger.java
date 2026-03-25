package com.example.hotel_management_system.util;

import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
public class DatabaseLogger {
    private static final String LOG_QUERY =
            "INSERT INTO NBP.NBP_LOG (ACTION_NAME, TABLE_NAME, DATE_TIME, DB_USER) VALUES (?, ?, CURRENT_TIMESTAMP, USER)";

    public static void log(Connection conn, String action, String tableName) {
        if (conn == null) return;

        try (PreparedStatement ps = conn.prepareStatement(LOG_QUERY)) {
            ps.setString(1, action);
            ps.setString(2, tableName);
            ps.executeUpdate();
        } catch (SQLException e) {
            // Ispis greske, bez prekida rada aplikacije
            System.err.println("JDBC LOG ERROR: " + e.getMessage());
        }
    }

    public static void logWithJdbcTemplate(JdbcTemplate jdbcTemplate, String action, String tableName) {
        String sql = "INSERT INTO NBP.NBP_LOG (ACTION_NAME, TABLE_NAME, DATE_TIME, DB_USER) VALUES (?, ?, CURRENT_TIMESTAMP, USER)";
        try {
            jdbcTemplate.update(sql, action, tableName);
        } catch (Exception e) {
            System.err.println("JDBC LOG ERROR (JdbcTemplate): " + e.getMessage());
        }
    }
}