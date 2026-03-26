package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.RoomType;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class RoomTypeRepository {

    private final String INSERT_QUERY = """
            INSERT INTO NBP_ROOM_TYPE (ID, NAME, DESCRIPTION, CAPACITY, PRICE_PER_NIGHT)
            VALUES (?, ?, ?, ?, ?)
            """;

    private final String SELECT_ALL_QUERY = """
            SELECT ID, NAME, DESCRIPTION, CAPACITY, PRICE_PER_NIGHT
            FROM NBP_ROOM_TYPE
            ORDER BY ID
            """;

    private final String SELECT_BY_ID_QUERY = """
            SELECT ID, NAME, DESCRIPTION, CAPACITY, PRICE_PER_NIGHT
            FROM NBP_ROOM_TYPE
            WHERE ID = ?
            """;

    private final String UPDATE_QUERY = """
            UPDATE NBP_ROOM_TYPE
            SET NAME = ?, DESCRIPTION = ?, CAPACITY = ?, PRICE_PER_NIGHT = ?
            WHERE ID = ?
            """;

    private final String DELETE_QUERY = """
            DELETE FROM NBP_ROOM_TYPE
            WHERE ID = ?
            """;

    public void save(RoomType roomType, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, roomType.getId());
            ps.setString(2, roomType.getName());
            ps.setString(3, roomType.getDescription());
            ps.setInt(4, roomType.getCapacity());
            ps.setDouble(5, roomType.getPricePerNight());
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "POST", "NBP_ROOM_TYPE");
        }
    }

    public Optional<RoomType> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<RoomType> findAll(Connection connection) throws SQLException {
        List<RoomType> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    public void update(RoomType roomType, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, roomType.getName());
            ps.setString(2, roomType.getDescription());
            ps.setInt(3, roomType.getCapacity());
            ps.setDouble(4, roomType.getPricePerNight());
            ps.setLong(5, roomType.getId());
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "PUT", "NBP_ROOM_TYPE");
        }
    }

    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "DELETE", "NBP_ROOM_TYPE");
        }
    }

    private RoomType mapResultSet(ResultSet rs) throws SQLException {
        return new RoomType(
                rs.getLong("ID"),
                rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getInt("CAPACITY"),
                rs.getDouble("PRICE_PER_NIGHT")
        );
    }
}