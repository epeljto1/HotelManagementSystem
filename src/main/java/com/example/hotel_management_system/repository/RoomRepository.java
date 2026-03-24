package com.example.hotel_management_system.repository;


import com.example.hotel_management_system.enums.RoomStatus;
import com.example.hotel_management_system.model.Room;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RoomRepository {
    private final String INSERT_QUERY = """
        INSERT INTO NBP_ROOM (ID, ROOM_NUMBER, FLOOR_NUMBER, STATUS, HOTEL_ID, ROOM_TYPE_ID)
        VALUES (?, ?, ?, ?, ?, ?)
        """;
    private final String SELECT_ALL_QUERY = "SELECT * FROM NBP_ROOM ORDER BY ID";

    private final String SELECT_BY_ID_QUERY = "SELECT * FROM NBP_ROOM WHERE ID = ?";

    private final String UPDATE_QUERY = """
        UPDATE NBP_ROOM
        SET ROOM_NUMBER = ?, FLOOR_NUMBER = ?, STATUS = ?, HOTEL_ID = ?, ROOM_TYPE_ID = ?
        WHERE ID = ?
        """;

    private final String DELETE_QUERY = "DELETE FROM NBP_ROOM WHERE ID = ?";

    public void save(Room room, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, room.getId());
            ps.setString(2, room.getRoomNumber());

            if (room.getFloorNumber() != null) {
                ps.setInt(3, room.getFloorNumber());
            } else {
                ps.setNull(3, Types.INTEGER);
            }

            ps.setString(4, room.getStatus().name());
            ps.setLong(5, room.getHotelId());
            ps.setLong(6, room.getRoomTypeId());

            ps.executeUpdate();
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

    public void update(Room room, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, room.getRoomNumber());
            if (room.getFloorNumber() != null) {
                ps.setInt(2, room.getFloorNumber());
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setString(3, room.getStatus().name());
            ps.setLong(4, room.getHotelId());
            ps.setLong(5, room.getRoomTypeId());
            ps.setLong(6, room.getId());
            ps.executeUpdate();
        }
    }

    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Room mapResultSetToRoom(ResultSet rs) throws SQLException {
        String statusStr = rs.getString("STATUS");
        RoomStatus status = (statusStr != null) ? RoomStatus.valueOf(statusStr) : RoomStatus.AVAILABLE;

        int floor = rs.getInt("FLOOR_NUMBER");
        Integer floorNumber = rs.wasNull() ? null : floor;

        return new Room(
                rs.getLong("ID"),
                rs.getString("ROOM_NUMBER"),
                floorNumber,
                status,
                rs.getLong("HOTEL_ID"),
                rs.getLong("ROOM_TYPE_ID")
        );
    }
}
