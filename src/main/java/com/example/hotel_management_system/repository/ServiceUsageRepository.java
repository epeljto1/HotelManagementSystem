package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.ServiceUsage;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ServiceUsageRepository {

    private final String SELECT_ALL_QUERY = """
            SELECT ID, STAY_ID, SERVICE_ID, QUANTITY, USAGE_DATE, TOTAL_PRICE
            FROM NBP_SERVICE_USAGE
            ORDER BY ID
            """;

    private final String SELECT_BY_ID_QUERY = """
            SELECT ID, STAY_ID, SERVICE_ID, QUANTITY, USAGE_DATE, TOTAL_PRICE
            FROM NBP_SERVICE_USAGE
            WHERE ID = ?
            """;

    private final String INSERT_QUERY = """
            INSERT INTO NBP_SERVICE_USAGE (ID, STAY_ID, SERVICE_ID, QUANTITY, USAGE_DATE, TOTAL_PRICE)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    private final String UPDATE_QUERY = """
            UPDATE NBP_SERVICE_USAGE
            SET STAY_ID = ?, SERVICE_ID = ?, QUANTITY = ?, USAGE_DATE = ?, TOTAL_PRICE = ?
            WHERE ID = ?
            """;

    private final String DELETE_QUERY = """
            DELETE FROM NBP_SERVICE_USAGE
            WHERE ID = ?
            """;

    private final String FIND_BY_STAY_ID_QUERY = """
            SELECT ID, STAY_ID, SERVICE_ID, QUANTITY, USAGE_DATE, TOTAL_PRICE
            FROM NBP_SERVICE_USAGE
            WHERE STAY_ID = ?
            """;

    public List<ServiceUsage> findAll(Connection connection) throws SQLException {
        List<ServiceUsage> list = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }

        return list;
    }

    public ServiceUsage findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }

        return null;
    }

    public void save(ServiceUsage serviceUsage, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, serviceUsage.getId());
            ps.setLong(2, serviceUsage.getStayId());
            ps.setLong(3, serviceUsage.getServiceId());
            ps.setInt(4, serviceUsage.getQuantity());
            ps.setDate(5, Date.valueOf(serviceUsage.getUsageDate()));
            ps.setBigDecimal(6, serviceUsage.getTotalPrice());
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "POST", "NBP_SERVICE_USAGE");
        }
    }

    public void update(Long id, ServiceUsage serviceUsage, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setLong(1, serviceUsage.getStayId());
            ps.setLong(2, serviceUsage.getServiceId());
            ps.setInt(3, serviceUsage.getQuantity());
            ps.setDate(4, Date.valueOf(serviceUsage.getUsageDate()));
            ps.setBigDecimal(5, serviceUsage.getTotalPrice());
            ps.setLong(6, id);
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "PUT", "NBP_SERVICE_USAGE");
        }
    }

    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "DELETE", "NBP_SERVICE_USAGE");
        }
    }

    public List<ServiceUsage> findByStayId(Long stayId, Connection connection) throws SQLException {
        List<ServiceUsage> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_STAY_ID_QUERY)) {
            ps.setLong(1, stayId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    public String findServiceNameById(Long serviceId, Connection conn) throws SQLException {
        String sql = "SELECT NAME FROM NBP_SERVICE WHERE ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, serviceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("NAME");
                }
            }
        }
        return "Unknown service";
    }

    private ServiceUsage mapResultSet(ResultSet rs) throws SQLException {
        return new ServiceUsage(
                rs.getLong("ID"),
                rs.getLong("STAY_ID"),
                rs.getLong("SERVICE_ID"),
                rs.getInt("QUANTITY"),
                rs.getDate("USAGE_DATE").toLocalDate(),
                rs.getBigDecimal("TOTAL_PRICE")
        );
    }
}