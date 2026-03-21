package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.ServiceUsage;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public class ServiceUsageRepository {

    private final JdbcTemplate jdbcTemplate;

    public ServiceUsageRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<ServiceUsage> findAll() {
        String sql = "SELECT * FROM NBP_SERVICE_USAGE";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new ServiceUsage(
                rs.getLong("ID"),
                rs.getLong("STAY_ID"),
                rs.getLong("SERVICE_ID"),
                rs.getInt("QUANTITY"),
                rs.getDate("USAGE_DATE").toLocalDate(),
                rs.getBigDecimal("TOTAL_PRICE")
        ));
    }

    public ServiceUsage findById(Long id) {
        String sql = "SELECT * FROM NBP_SERVICE_USAGE WHERE ID = ?";

        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> new ServiceUsage(
                rs.getLong("ID"),
                rs.getLong("STAY_ID"),
                rs.getLong("SERVICE_ID"),
                rs.getInt("QUANTITY"),
                rs.getDate("USAGE_DATE").toLocalDate(),
                rs.getBigDecimal("TOTAL_PRICE")
        ));
    }

    public void save(ServiceUsage serviceUsage) {
        String sql = "INSERT INTO NBP_SERVICE_USAGE (ID, STAY_ID, SERVICE_ID, QUANTITY, USAGE_DATE, TOTAL_PRICE) VALUES (?, ?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                serviceUsage.getId(),
                serviceUsage.getStayId(),
                serviceUsage.getServiceId(),
                serviceUsage.getQuantity(),
                Date.valueOf(serviceUsage.getUsageDate()),
                serviceUsage.getTotalPrice()
        );
    }

    public void update(Long id, ServiceUsage serviceUsage) {
        String sql = "UPDATE NBP_SERVICE_USAGE SET STAY_ID = ?, SERVICE_ID = ?, QUANTITY = ?, USAGE_DATE = ?, TOTAL_PRICE = ? WHERE ID = ?";

        jdbcTemplate.update(sql,
                serviceUsage.getStayId(),
                serviceUsage.getServiceId(),
                serviceUsage.getQuantity(),
                Date.valueOf(serviceUsage.getUsageDate()),
                serviceUsage.getTotalPrice(),
                id
        );
    }

    public void delete(Long id) {
        String sql = "DELETE FROM NBP_SERVICE_USAGE WHERE ID = ?";
        jdbcTemplate.update(sql, id);
    }
}