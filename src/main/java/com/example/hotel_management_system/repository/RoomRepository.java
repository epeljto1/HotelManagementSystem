package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.enums.RoomStatus;
import com.example.hotel_management_system.model.Room;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorij zadužen za upravljanje podacima o sobama u okviru hotela.
 * Komunicira sa tabelom {@code NBP_ROOM} i rukuje informacijama o broju sobe,
 * spratnosti, trenutnom statusu i vizuelnim prikazima (BLOB slike).
 * * @author Tvoje Ime
 * @version 1.0
 */
@Repository
public class RoomRepository {

    private final String INSERT_QUERY = """
        INSERT INTO NBP_ROOM (ID, ROOM_NUMBER, FLOOR_NUMBER, STATUS, HOTEL_ID, ROOM_TYPE_ID, IMAGE)
        VALUES (NBP_ROOM_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?)
    """;

    private final String SELECT_ALL_QUERY = "SELECT * FROM NBP_ROOM ORDER BY ID";
    private final String SELECT_BY_ID_QUERY = "SELECT * FROM NBP_ROOM WHERE ID = ?";

    private final String UPDATE_QUERY = """
        UPDATE NBP_ROOM
        SET ROOM_NUMBER = ?, FLOOR_NUMBER = ?, STATUS = ?, HOTEL_ID = ?, ROOM_TYPE_ID = ?, IMAGE = ?
        WHERE ID = ?
    """;

    private final String UPDATE_STATUS_QUERY = "UPDATE NBP_ROOM SET STATUS = ? WHERE ID = ?";
    private final String DELETE_QUERY = "DELETE FROM NBP_ROOM WHERE ID = ?";

    private static final String FIND_AVAILABLE_ROOMS_QUERY = """
        SELECT * FROM NBP_ROOM r
        WHERE r.STATUS = 'AVAILABLE'
        AND r.ID NOT IN (
            SELECT res.ROOM_ID
            FROM NBP_RESERVATION res
            WHERE (res.CHECK_IN_DATE <= ? AND res.CHECK_OUT_DATE >= ?)
        )
        ORDER BY r.ID
    """;

    /**
     * DODATA METODA KOJA JE NEDOSTAJALA: Vraća sve sobe iz baze.
     */
    public List<Room> findAll(Connection connection) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                rooms.add(mapResultSetToRoom(rs));
            }
        }
        return rooms;
    }

    public void save(Room room, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, new String[]{"ID"})) {
            ps.setString(1, room.getRoomNumber());
            ps.setObject(2, room.getFloorNumber(), Types.INTEGER);
            ps.setString(3, room.getStatus().name());
            ps.setLong(4, room.getHotelId());
            ps.setLong(5, room.getRoomTypeId());
            ps.setBytes(6, room.getImage());

            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    room.setId(generatedKeys.getLong(1));
                }
            }
            DatabaseLogger.log(connection, "POST", "NBP_ROOM");
        }
    }

    public Optional<Room> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRoom(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Room> findAvailableRooms(Connection connection, Date from, Date to) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(FIND_AVAILABLE_ROOMS_QUERY)) {
            ps.setDate(1, to);
            ps.setDate(2, from);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    rooms.add(mapResultSetToRoom(rs));
                }
            }
        }
        return rooms;
    }

    public void update(Room room, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, room.getRoomNumber());
            ps.setObject(2, room.getFloorNumber(), Types.INTEGER);
            ps.setString(3, room.getStatus().name());
            ps.setLong(4, room.getHotelId());
            ps.setLong(5, room.getRoomTypeId());
            ps.setBytes(6, room.getImage());
            ps.setLong(7, room.getId());

            ps.executeUpdate();
            DatabaseLogger.log(connection, "PUT", "NBP_ROOM");
        }
    }

    public void updateStatus(Long id, RoomStatus status, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_STATUS_QUERY)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();
            DatabaseLogger.log(connection, "PUT", "NBP_ROOM");
        }
    }

    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            DatabaseLogger.log(connection, "DELETE", "NBP_ROOM");
        }
    }

    private static Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        String statusStr = rs.getString("STATUS");
        RoomStatus status = statusStr != null ? RoomStatus.valueOf(statusStr) : RoomStatus.AVAILABLE;
        int floor = rs.getInt("FLOOR_NUMBER");
        Integer floorNumber = rs.wasNull() ? null : floor;

        return new Room(
                rs.getLong("ID"),
                rs.getString("ROOM_NUMBER"),
                floorNumber,
                status,
                rs.getLong("HOTEL_ID"),
                rs.getLong("ROOM_TYPE_ID"),
                rs.getBytes("IMAGE")
        );
    }
}