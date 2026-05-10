package com.example.hotel_management_system.repository;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Repository
public class JwtTokenRepository {

    public void save(String token, String username, String role, LocalDateTime issuedAt, LocalDateTime expiresAt,
                     Connection conn) throws SQLException {
        String sql = """
            INSERT INTO NBP_JWT_TOKENS (TOKEN, USERNAME, ROLE, ISSUED_AT, EXPIRES_AT)
            VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setString(2, username);
            ps.setString(3, role);
            ps.setTimestamp(4, Timestamp.valueOf(issuedAt));
            ps.setTimestamp(5, Timestamp.valueOf(expiresAt));
            ps.executeUpdate();
        }
    }

    public boolean existsActiveToken(String token, Connection conn) throws SQLException {
        String sql = """
            SELECT 1
            FROM NBP_JWT_TOKENS
            WHERE TOKEN = ?
              AND EXPIRES_AT > CURRENT_TIMESTAMP
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            return ps.executeQuery().next();
        }
    }

    public void deleteByToken(String token, Connection conn) throws SQLException {
        String sql = "DELETE FROM NBP_JWT_TOKENS WHERE TOKEN = ?";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.executeUpdate();
        }
    }

}
