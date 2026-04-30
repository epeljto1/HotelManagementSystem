package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.User;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository {

    public void save(User user, Connection conn) throws SQLException {
        long generatedNbpId = createInNbpSchema(user, conn);

        user.setUserId(generatedNbpId);

        String sql = """
            INSERT INTO NBP_USER (ID, USER_ID, ROLE_ID, USERNAME, EMAIL, PASSWORD_HASH, ROLE, CREATED_DATE)
            VALUES (NBP_USER_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, CURRENT_DATE)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, user.getUserId());
            ps.setLong(2, user.getRoleId());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPasswordHash());
            ps.setString(6, user.getRole());

            ps.executeUpdate();

            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        }
    }

    private long createInNbpSchema(User user, Connection conn) throws SQLException {
        String sql = """
            INSERT INTO NBP.NBP_USER (ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, USERNAME, ROLE_ID)
            VALUES (NBP.NBP_USER_ID_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID"})) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getUsername());
            ps.setLong(6, user.getRoleId());

            ps.executeUpdate();

            DatabaseLogger.log(conn, "POST", "NBP_USER");


            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new SQLException("Neuspješno preuzimanje generisanog ID-a iz NBP.NBP_USER.");
                }
            }
        }
    }

    public List<User> findAll(Connection conn) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM NBP_USER";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                        rs.getLong("ID"),
                        rs.getLong("USER_ID"),
                        rs.getLong("ROLE_ID"),
                        rs.getString("USERNAME"),
                        rs.getString("EMAIL"),
                        null, // Ne vraćamo password hash radi sigurnosti
                        rs.getString("ROLE"),
                        rs.getDate("CREATED_DATE") != null ? rs.getDate("CREATED_DATE").toLocalDate() : null,
                        null, null
                ));
            }
        }
        return users;
    }

    public Optional<User> findByUsername(String username, Connection conn) throws SQLException {
        String sql = "SELECT * FROM NBP_USER WHERE USERNAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getLong("ID"),
                            rs.getLong("USER_ID"),
                            rs.getLong("ROLE_ID"),
                            rs.getString("USERNAME"),
                            rs.getString("EMAIL"),
                            rs.getString("PASSWORD_HASH"),
                            rs.getString("ROLE"),
                            rs.getDate("CREATED_DATE") != null ? rs.getDate("CREATED_DATE").toLocalDate() : null,
                            null, // firstName (nije u ovoj tabeli)
                            null  // lastName (nije u ovoj tabeli)
                    ));
                }
            }
        }
        return Optional.empty();
    }

    public Optional<User> findById(Long id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM NBP_USER WHERE ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getLong("ID"),
                            rs.getLong("USER_ID"),
                            rs.getLong("ROLE_ID"),
                            rs.getString("USERNAME"),
                            rs.getString("EMAIL"),
                            null,
                            rs.getString("ROLE"),
                            rs.getDate("CREATED_DATE") != null ? rs.getDate("CREATED_DATE").toLocalDate() : null,
                            null, null
                    ));
                }
            }
        }
        return Optional.empty();
    }
}