package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Hotel;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class HotelRepository {
    
    private final String INSERT_QUERY = """
            INSERT INTO NBP_HOTEL (ID, NAME, DESCRIPTION, PHONE_NUMBER, EMAIL, ADDRESS)
            VALUES (?, ?, ?, ?, ?, ?)
            """;
    
    private final String SELECT_ALL_QUERY = """
            SELECT ID, NAME, DESCRIPTION, PHONE_NUMBER, EMAIL, ADDRESS
            FROM NBP_HOTEL
            ORDER BY ID
            """;
    
    private final String SELECT_BY_ID_QUERY = """
            SELECT ID, NAME, DESCRIPTION, PHONE_NUMBER, EMAIL, ADDRESS
            FROM NBP_HOTEL
            WHERE ID = ?
            """;
    
    private final String UPDATE_QUERY = """
            UPDATE NBP_HOTEL
            SET NAME = ?, DESCRIPTION = ?, PHONE_NUMBER = ?, EMAIL = ?, ADDRESS = ?
            WHERE ID = ?
            """;
    
    private final String DELETE_QUERY = """
            DELETE FROM NBP_HOTEL
            WHERE ID = ?
            """;

    public void save(Hotel hotel, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, hotel.getId());
            ps.setString(2, hotel.getName());
            ps.setString(3, hotel.getDescription());
            ps.setString(4, hotel.getPhoneNumber());
            ps.setString(5, hotel.getEmail());
            ps.setLong(6, hotel.getAddressId());
            ps.executeUpdate();
        }
    }

    public Optional<Hotel> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToHotel(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Hotel> findAll(Connection connection) throws SQLException {
        List<Hotel> hotels = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hotels.add(mapResultSetToHotel(rs));
            }
        }
        return hotels;
    }

    public void update(Hotel hotel, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, hotel.getName());
            ps.setString(2, hotel.getDescription());
            ps.setString(3, hotel.getPhoneNumber());
            ps.setString(4, hotel.getEmail());
            ps.setLong(5, hotel.getAddressId());
            ps.setLong(6, hotel.getId());
            ps.executeUpdate();
        }
    }

    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Hotel mapResultSetToHotel(ResultSet rs) throws SQLException {
        return new Hotel(
                rs.getLong("ID"),
                rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getString("PHONE_NUMBER"),
                rs.getString("EMAIL"),
                rs.getLong("ADDRESS")
        );
    }
}

