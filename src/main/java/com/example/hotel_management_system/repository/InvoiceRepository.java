package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Invoice;
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
public class InvoiceRepository {

    private final String SELECT_ALL_QUERY = """
        SELECT ID, ISSUE_DATE, TOTAL_AMOUNT, STATUS, STAY_ID, DISCOUNT_ID, DISCOUNT_AMOUNT, FINAL_AMOUNT, INVOICE_PDF
        FROM NBP_INVOICE
        ORDER BY ID
        """;

    private final String SELECT_BY_ID_QUERY = """
        SELECT ID, ISSUE_DATE, TOTAL_AMOUNT, STATUS, STAY_ID, DISCOUNT_ID, DISCOUNT_AMOUNT, FINAL_AMOUNT, INVOICE_PDF
        FROM NBP_INVOICE
        WHERE ID = ?
        """;

    private final String INSERT_QUERY = """
        INSERT INTO NBP_INVOICE (ID, ISSUE_DATE, TOTAL_AMOUNT, STATUS, STAY_ID, DISCOUNT_ID, DISCOUNT_AMOUNT, FINAL_AMOUNT, INVOICE_PDF)
        VALUES (NBP_INVOICE_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    private final String UPDATE_QUERY = """
        UPDATE NBP_INVOICE
        SET ISSUE_DATE = ?, TOTAL_AMOUNT = ?, STATUS = ?, STAY_ID = ?, DISCOUNT_ID = ?, DISCOUNT_AMOUNT = ?, FINAL_AMOUNT = ?, INVOICE_PDF = ?
        WHERE ID = ?
        """;

    private final String DELETE_QUERY = """
        DELETE FROM NBP_INVOICE
        WHERE ID = ?
        """;

    private final String FIND_BY_STAY_ID_QUERY = """
        SELECT ID, ISSUE_DATE, TOTAL_AMOUNT, STATUS, STAY_ID, DISCOUNT_ID, DISCOUNT_AMOUNT, FINAL_AMOUNT, INVOICE_PDF
        FROM NBP_INVOICE
        WHERE STAY_ID = ?
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
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, new String[]{"ID"})) {
            ps.setDate(1, Date.valueOf(invoice.getIssueDate()));
            ps.setBigDecimal(2, invoice.getTotalAmount());
            ps.setString(3, invoice.getStatus());
            ps.setLong(4, invoice.getStayId());
            if (invoice.getDiscountId() != null) {
                ps.setLong(5, invoice.getDiscountId());
            } else {
                ps.setNull(5, java.sql.Types.NUMERIC);
            }

            if (invoice.getDiscountAmount() != null) {
                ps.setBigDecimal(6, invoice.getDiscountAmount());
            } else {
                ps.setNull(6, java.sql.Types.NUMERIC);
            }

            if (invoice.getFinalAmount() != null) {
                ps.setBigDecimal(7, invoice.getFinalAmount());
            } else {
                ps.setNull(7, java.sql.Types.NUMERIC);
            }

            ps.setBytes(8, invoice.getInvoicePdf()); // Upis PDF bajtova u BLOB

            ps.executeUpdate();

            DatabaseLogger.log(connection, "POST", "NBP_INVOICE");

            connection.commit();
            System.out.println("DEBUG: Invoice uspješno upisan i COMMIT izvršen!");
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    public void update(Long id, Invoice invoice, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setDate(1, Date.valueOf(invoice.getIssueDate()));
            ps.setBigDecimal(2, invoice.getTotalAmount());
            ps.setString(3, invoice.getStatus());
            ps.setLong(4, invoice.getStayId());

            if (invoice.getDiscountId() != null) {
                ps.setLong(5, invoice.getDiscountId());
            } else {
                ps.setNull(5, java.sql.Types.NUMERIC);
            }

            if (invoice.getDiscountAmount() != null) {
                ps.setBigDecimal(6, invoice.getDiscountAmount());
            } else {
                ps.setNull(6, java.sql.Types.NUMERIC);
            }

            if (invoice.getFinalAmount() != null) {
                ps.setBigDecimal(7, invoice.getFinalAmount());
            } else {
                ps.setNull(7, java.sql.Types.NUMERIC);
            }

            ps.setBytes(8, invoice.getInvoicePdf());
            ps.setLong(9, id);

            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "PUT", "NBP_INVOICE");
        }
    }

    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "DELETE", "NBP_INVOICE");
        }
    }

    public Invoice findByStayId(Long stayId, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_STAY_ID_QUERY)) {
            ps.setLong(1, stayId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        }
        return null;
    }

    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getLong("ID"));
        invoice.setIssueDate(rs.getDate("ISSUE_DATE").toLocalDate());
        invoice.setTotalAmount(rs.getBigDecimal("TOTAL_AMOUNT"));
        invoice.setStatus(rs.getString("STATUS"));
        invoice.setStayId(rs.getLong("STAY_ID"));
        invoice.setDiscountId(rs.getObject("DISCOUNT_ID") != null ? rs.getLong("DISCOUNT_ID") : null);
        invoice.setDiscountAmount(rs.getBigDecimal("DISCOUNT_AMOUNT"));
        invoice.setFinalAmount(rs.getBigDecimal("FINAL_AMOUNT"));
        invoice.setInvoicePdf(rs.getBytes("INVOICE_PDF")); // Čitanje PDF-a iz BLOB kolone
        return invoice;
    }
}