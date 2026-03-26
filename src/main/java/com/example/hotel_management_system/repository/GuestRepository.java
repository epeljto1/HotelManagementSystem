package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Guest;
import com.example.hotel_management_system.model.Hotel;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class GuestRepository {
    private final String INSERT_QUERY = """
            INSERT INTO NBP_GUEST (ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, DATE_OF_BIRTH, DOCUMENT_NUMBER, ADDRESS_ID)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    private final String SELECT_ALL_QUERY = """
            SELECT ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, DATE_OF_BIRTH, DOCUMENT_NUMBER, ADDRESS_ID
            FROM NBP_GUEST
            ORDER BY ID
            """;

    private final String SELECT_BY_ID_QUERY = """
            SELECT ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, DATE_OF_BIRTH, DOCUMENT_NUMBER, ADDRESS_ID
            FROM NBP_GUEST
            WHERE ID = ?
            """;

    private final String UPDATE_QUERY = """
            UPDATE NBP_GUEST
            SET FIRST_NAME = ?, LAST_NAME = ?, EMAIL = ?, PHONE_NUMBER = ?, DATE_OF_BIRTH = ?, DOCUMENT_NUMBER = ?, ADDRESS_ID = ?
            WHERE ID = ?
            """;

    private final String DELETE_QUERY = """
            DELETE FROM NBP_GUEST
            WHERE ID = ?
            """;

    public void save(Guest guest, Connection connection) throws SQLException{
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY);) {
            ps.setLong(1, guest.getId());
            ps.setString(2, guest.getFirstName());
            ps.setString(3, guest.getLastName());
            ps.setString(4, guest.getEmail());
            ps.setString(5, guest.getPhoneNumber());
            ps.setDate(6, new java.sql.Date(guest.getDateOfBirth().getTime()));
            ps.setString(7, guest.getDocumentNumber());
            ps.setLong(8, guest.getAddressId());
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "POST", "NBP_GUEST");
        }
    }

    public Optional<Guest> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)){
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuest(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<Guest> findAll(Connection connection) throws SQLException {
        List<Guest> guests = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY)){
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    guests.add(mapResultSetToGuest(rs));
                }
            }
        }
        return guests;
    }

    public void update(Guest guest, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, guest.getFirstName());
            ps.setString(2, guest.getLastName());
            ps.setString(3, guest.getEmail());
            ps.setString(4, guest.getPhoneNumber());
            ps.setDate(5, new java.sql.Date(guest.getDateOfBirth().getTime()));
            ps.setString(6, guest.getDocumentNumber());
            ps.setLong(7, guest.getAddressId());
            ps.setLong(8, guest.getId());
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "PUT", "NBP_GUEST");
        }
    }

    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "DELETE", "NBP_GUEST");
        }
    }

    private Guest mapResultSetToGuest(ResultSet rs) throws SQLException {
        return new Guest(
                rs.getLong("ID"),
                rs.getString("FIRST_NAME"),
                rs.getString("LAST_NAME"),
                rs.getString("EMAIL"),
                rs.getString("PHONE_NUMBER"),
                rs.getDate("DATE_OF_BIRTH"),
                rs.getString("DOCUMENT_NUMBER"),
                rs.getLong("ADDRESS_ID")
        );
    }

}
