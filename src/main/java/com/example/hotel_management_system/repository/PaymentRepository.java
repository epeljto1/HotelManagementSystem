package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Payment;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class PaymentRepository {

    private final String INSERT_QUERY = """
            INSERT INTO NBP_PAYMENT (ID, PAYMENT_DATE, AMOUNT, PAYMENT_METHOD, INVOICE_ID)
            VALUES (NBP_PAYMENT_SEQ.NEXTVAL, ?, ?, ?, ?)
            """;

    private final String SELECT_ALL_QUERY = "SELECT * FROM NBP_PAYMENT ORDER BY ID";

    private final String SELECT_BY_ID_QUERY = "SELECT * FROM NBP_PAYMENT WHERE ID = ?";

    private final String UPDATE_QUERY = """
            UPDATE NBP_PAYMENT
            SET PAYMENT_DATE = ?, AMOUNT = ?, PAYMENT_METHOD = ?, INVOICE_ID = ?
            WHERE ID = ?
            """;

    private final String DELETE_QUERY = "DELETE FROM NBP_PAYMENT WHERE ID = ?";

    public void save(Payment payment, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setTimestamp(1, Timestamp.valueOf(payment.getPaymentDate()));
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());
            ps.setLong(4, payment.getInvoiceId());
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "POST", "NBP_PAYMENT");
        }
    }

    public List<Payment> findAll(Connection connection) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        }
        return payments;
    }

    public Optional<Payment> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void update(Payment payment, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setTimestamp(1, Timestamp.valueOf(payment.getPaymentDate()));
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());
            ps.setLong(4, payment.getInvoiceId());
            ps.setLong(5, payment.getId());
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "PUT", "NBP_PAYMENT");
        }
    }

    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "DELETE", "NBP_PAYMENT");
        }
    }

    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        return new Payment(
                rs.getLong("ID"),
                rs.getTimestamp("PAYMENT_DATE").toLocalDateTime(),
                rs.getDouble("AMOUNT"),
                rs.getString("PAYMENT_METHOD"),
                rs.getLong("INVOICE_ID")
        );
    }
}