package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Invoice;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public class InvoiceRepository {

    private final JdbcTemplate jdbcTemplate;

    public InvoiceRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Invoice> findAll() {
        String sql = "SELECT * FROM NBP_INVOICE";

        return jdbcTemplate.query(sql, (rs, rowNum) -> new Invoice(
                rs.getLong("ID"),
                rs.getDate("ISSUE_DATE").toLocalDate(),
                rs.getBigDecimal("TOTAL_AMOUNT"),
                rs.getString("STATUS"),
                rs.getLong("STAY_ID")
        ));
    }

    public Invoice findById(Long id) {
        String sql = "SELECT * FROM NBP_INVOICE WHERE ID = ?";

        return jdbcTemplate.queryForObject(sql, new Object[]{id}, (rs, rowNum) -> new Invoice(
                rs.getLong("ID"),
                rs.getDate("ISSUE_DATE").toLocalDate(),
                rs.getBigDecimal("TOTAL_AMOUNT"),
                rs.getString("STATUS"),
                rs.getLong("STAY_ID")
        ));
    }

    public void save(Invoice invoice) {
        String sql = "INSERT INTO NBP_INVOICE (ID, ISSUE_DATE, TOTAL_AMOUNT, STATUS, STAY_ID) VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(sql,
                invoice.getId(),
                Date.valueOf(invoice.getIssueDate()),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                invoice.getStayId()
        );
    }

    public void update(Long id, Invoice invoice) {
        String sql = "UPDATE NBP_INVOICE SET ISSUE_DATE = ?, TOTAL_AMOUNT = ?, STATUS = ?, STAY_ID = ? WHERE ID = ?";

        jdbcTemplate.update(sql,
                Date.valueOf(invoice.getIssueDate()),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                invoice.getStayId(),
                id
        );
    }

    public void delete(Long id) {
        String sql = "DELETE FROM NBP_INVOICE WHERE ID = ?";
        jdbcTemplate.update(sql, id);
    }
}