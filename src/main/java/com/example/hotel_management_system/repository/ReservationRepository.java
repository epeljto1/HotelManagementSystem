package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.enums.ReservationStatus;
import com.example.hotel_management_system.model.Reservation;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ReservationRepository {

    private final String INSERT_QUERY = """
            INSERT INTO NBP_RESERVATION (ID, RESERVATION_DATE, CHECK_IN_DATE, CHECK_OUT_DATE, 
                                       NUMBER_OF_GUESTS, STATUS, TOTAL_PRICE, GUEST_ID, ROOM_ID, CREATED_BY)
            VALUES (NBP_RESERVATION_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final String SELECT_ALL_QUERY = "SELECT * FROM NBP_RESERVATION ORDER BY ID";
    private final String SELECT_BY_ID_QUERY = "SELECT * FROM NBP_RESERVATION WHERE ID = ?";

    private final String UPDATE_QUERY = """
            UPDATE NBP_RESERVATION 
            SET RESERVATION_DATE = ?, CHECK_IN_DATE = ?, CHECK_OUT_DATE = ?, 
                NUMBER_OF_GUESTS = ?, STATUS = ?, TOTAL_PRICE = ?, 
                GUEST_ID = ?, ROOM_ID = ?, CREATED_BY = ?
            WHERE ID = ?
            """;

    private final String UPDATE_STATUS_QUERY = """
            UPDATE NBP_RESERVATION
            SET STATUS = ?
            WHERE ID = ?
            """;

    private final String DELETE_QUERY = "DELETE FROM NBP_RESERVATION WHERE ID = ?";

    private final String FIND_BY_RESERVATION_ID_AND_STATUS = """
            SELECT * FROM NBP_RESERVATION WHERE ID = ? AND STATUS = ?
            """;

    public void save(Reservation res, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_QUERY)) {
            ps.setTimestamp(1, new Timestamp(res.getReservationDate().getTime()));
            ps.setTimestamp(2, new Timestamp(res.getCheckInDate().getTime()));
            ps.setTimestamp(3, new Timestamp(res.getCheckOutDate().getTime()));
            ps.setInt(4, res.getNumberOfGuests());
            ps.setString(5, res.getStatus().name());
            ps.setDouble(6, res.getTotalPrice());
            ps.setLong(7, res.getGuestId());
            ps.setLong(8, res.getRoomId());
            ps.setLong(9, res.getCreatedBy());

            ps.executeUpdate();

            DatabaseLogger.log(conn, "POST", "NBP_RESERVATION");
        }
    }

    public List<Reservation> findAll(Connection conn) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SELECT_ALL_QUERY)) {
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        }
        return reservations;
    }

    public Optional<Reservation> findById(Long id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReservation(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void update(Reservation res, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_QUERY)) {
            ps.setTimestamp(1, new Timestamp(res.getReservationDate().getTime()));
            ps.setTimestamp(2, new Timestamp(res.getCheckInDate().getTime()));
            ps.setTimestamp(3, new Timestamp(res.getCheckOutDate().getTime()));
            ps.setInt(4, res.getNumberOfGuests());
            ps.setString(5, res.getStatus().name());
            ps.setDouble(6, res.getTotalPrice());
            ps.setLong(7, res.getGuestId());
            ps.setLong(8, res.getRoomId());
            ps.setLong(9, res.getCreatedBy());
            ps.setLong(10, res.getId());

            ps.executeUpdate();
            DatabaseLogger.log(conn, "PUT", "NBP_RESERVATION");
        }
    }

    public void updateStatus(Long id, ReservationStatus status, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS_QUERY)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();

            DatabaseLogger.log(conn, "PUT", "NBP_RESERVATION");
        }
    }

    public void delete(Long id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            DatabaseLogger.log(conn, "DELETE", "NBP_RESERVATION");
        }
    }

    public Optional<Reservation> findByIdAndStatus(Long id, ReservationStatus status, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(FIND_BY_RESERVATION_ID_AND_STATUS)) {
            ps.setLong(1, id);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReservation(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getLong("ID"),
                rs.getTimestamp("RESERVATION_DATE"),
                rs.getTimestamp("CHECK_IN_DATE"),
                rs.getTimestamp("CHECK_OUT_DATE"),
                rs.getInt("NUMBER_OF_GUESTS"),
                ReservationStatus.valueOf(rs.getString("STATUS")),
                rs.getDouble("TOTAL_PRICE"),
                rs.getLong("GUEST_ID"),
                rs.getLong("ROOM_ID"),
                rs.getLong("CREATED_BY")
        );
    }
}