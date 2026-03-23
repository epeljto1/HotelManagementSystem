package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.ExtraService;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class ExtraServiceRepository {

    private final String INSERT_QUERY = """
            INSERT INTO NBP_SERVICE (ID, NAME, DESCRIPTION, UNIT_PRICE, AVAILABLE)
            VALUES (?, ?, ?, ?, ?)
            """;

    private final String SELECT_ALL_QUERY = """
            SELECT ID, NAME, DESCRIPTION, UNIT_PRICE, AVAILABLE
            FROM NBP_SERVICE
            ORDER BY ID
            """;

    private final String SELECT_BY_ID_QUERY = """
            SELECT ID, NAME, DESCRIPTION, UNIT_PRICE, AVAILABLE
            FROM NBP_SERVICE
            WHERE ID = ?
            """;

    private final String UPDATE_QUERY = """
            UPDATE NBP_SERVICE
            SET NAME = ?, DESCRIPTION = ?, UNIT_PRICE = ?, AVAILABLE = ?
            WHERE ID = ?
            """;

    private final String DELETE_QUERY = """
            DELETE FROM NBP_SERVICE
            WHERE ID = ?
            """;

    public void save(ExtraService extraService, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, extraService.getId());
            ps.setString(2, extraService.getName());
            ps.setString(3, extraService.getDescription());
            ps.setDouble(4, extraService.getUnitPrice());
            ps.setString(5, extraService.getAvailable());
            ps.executeUpdate();
        }
    }

    public Optional<ExtraService> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToExtraService(rs));
                }
            }
        }
        return Optional.empty();
    }

    public List<ExtraService> findAll(Connection connection) throws SQLException {
        List<ExtraService> services = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                services.add(mapResultSetToExtraService(rs));
            }
        }
        return services;
    }

    public void update(ExtraService extraService, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, extraService.getName());
            ps.setString(2, extraService.getDescription());
            ps.setDouble(3, extraService.getUnitPrice());
            ps.setString(4, extraService.getAvailable());
            ps.setLong(5, extraService.getId());
            ps.executeUpdate();
        }
    }

    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private ExtraService mapResultSetToExtraService(ResultSet rs) throws SQLException {
        return new ExtraService(
                rs.getLong("ID"),
                rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getDouble("UNIT_PRICE"),
                rs.getString("AVAILABLE")
        );
    }
}