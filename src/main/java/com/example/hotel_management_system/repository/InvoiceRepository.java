package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Invoice;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class InvoiceRepository {

    private final String SELECT_ALL_QUERY = """
            SELECT ID, ISSUE_DATE, TOTAL_AMOUNT, STATUS, STAY_ID
            FROM NBP_INVOICE
            ORDER BY ID
            """;

    private final String SELECT_BY_ID_QUERY = """
            SELECT ID, ISSUE_DATE, TOTAL_AMOUNT, STATUS, STAY_ID
            FROM NBP_INVOICE
            WHERE ID = ?
            """;

    private final String INSERT_QUERY = """
            INSERT INTO NBP_INVOICE (ID, ISSUE_DATE, TOTAL_AMOUNT, STATUS, STAY_ID)
            VALUES (?, ?, ?, ?, ?)
            """;

    private final String UPDATE_QUERY = """
            UPDATE NBP_INVOICE
            SET ISSUE_DATE = ?, TOTAL_AMOUNT = ?, STATUS = ?, STAY_ID = ?
            WHERE ID = ?
            """;

    private final String DELETE_QUERY = """
            DELETE FROM NBP_INVOICE
            WHERE ID = ?
            """;

    public List<Invoice> findAll(Connection connection) throws SQLException {
        List<Invoice> invoices = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                invoices.add(mapResultSetToInvoice(rs));
            }
        }

        return invoices;
    }

    public Invoice findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        }

        return null;
    }

    public void save(Invoice invoice, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, invoice.getId());
            ps.setDate(2, Date.valueOf(invoice.getIssueDate()));
            ps.setBigDecimal(3, invoice.getTotalAmount());
            ps.setString(4, invoice.getStatus());
            ps.setLong(5, invoice.getStayId());
            ps.executeUpdate();
        }
    }

    public void update(Long id, Invoice invoice, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setDate(1, Date.valueOf(invoice.getIssueDate()));
            ps.setBigDecimal(2, invoice.getTotalAmount());
            ps.setString(3, invoice.getStatus());
            ps.setLong(4, invoice.getStayId());
            ps.setLong(5, id);
            ps.executeUpdate();
        }
    }

    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        }
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        return new Invoice(
                rs.getLong("ID"),
                rs.getDate("ISSUE_DATE").toLocalDate(),
                rs.getBigDecimal("TOTAL_AMOUNT"),
                rs.getString("STATUS"),
                rs.getLong("STAY_ID")
        );
    }
}