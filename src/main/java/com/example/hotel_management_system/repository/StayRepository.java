package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Stay;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class StayRepository {

    private final String INSERT_QUERY = """
            INSERT INTO NBP_STAY (ID, CHECK_IN_TIME, CHECK_OUT_TIME, RESERVATION_ID, ACTUAL_TOTAL_PRICE)
            VALUES (?, ?, ?, ?, ?)
            """;

    private final String SELECT_ALL_QUERY = """
            SELECT ID, CHECK_IN_TIME, CHECK_OUT_TIME, RESERVATION_ID, ACTUAL_TOTAL_PRICE
            FROM NBP_STAY
            ORDER BY ID
            """;

    private final String SELECT_BY_ID_QUERY = """
            SELECT ID, CHECK_IN_TIME, CHECK_OUT_TIME, RESERVATION_ID, ACTUAL_TOTAL_PRICE
            FROM NBP_STAY
            WHERE ID = ?
            """;

    private final String UPDATE_QUERY = """
            UPDATE NBP_STAY
            SET CHECK_IN_TIME = ?, CHECK_OUT_TIME = ?, RESERVATION_ID = ?, ACTUAL_TOTAL_PRICE = ?
            WHERE ID = ?
            """;

    private final String DELETE_QUERY = """
            DELETE FROM NBP_STAY
            WHERE ID = ?
            """;

    public void save(Stay stay, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, stay.getId());
            ps.setTimestamp(2, Timestamp.valueOf(stay.getCheckInTime()));
            ps.setTimestamp(3, Timestamp.valueOf(stay.getCheckOutTime()));
            ps.setLong(4, stay.getReservationId());
            ps.setDouble(5, stay.getActualTotalPrice());
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "POST", "NBP_STAY");
        }
    }

    public Optional<Stay> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStay(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Stay> findAll(Connection connection) throws SQLException {
        List<Stay> stays = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stays.add(mapResultSetToStay(rs));
            }
        }
        return stays;
    }

    public void update(Stay stay, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setTimestamp(1, Timestamp.valueOf(stay.getCheckInTime()));
            ps.setTimestamp(2, Timestamp.valueOf(stay.getCheckOutTime()));
            ps.setLong(3, stay.getReservationId());
            ps.setDouble(4, stay.getActualTotalPrice());
            ps.setLong(5, stay.getId());
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "PUT", "NBP_STAY");
        }
    }

    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "DELETE", "NBP_STAY");
        }
    }

    private Stay mapResultSetToStay(ResultSet rs) throws SQLException {
        return new Stay(
                rs.getLong("ID"),
                rs.getTimestamp("CHECK_IN_TIME").toLocalDateTime(),
                rs.getTimestamp("CHECK_OUT_TIME").toLocalDateTime(),
                rs.getLong("RESERVATION_ID"),
                rs.getDouble("ACTUAL_TOTAL_PRICE")
        );
    }
}