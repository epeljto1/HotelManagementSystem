package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Payment;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Payment Repository Tests")
class PaymentRepositoryTest {

    private PaymentRepository paymentRepository;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        paymentRepository = new PaymentRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    @DisplayName("Trebalo bi uspešno sačuvati plaćanje")
    void testSavePayment() throws SQLException {
        // Arrange
        Payment payment = new Payment(1L, LocalDateTime.now(), 150.0, "CREDIT_CARD", 1L);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> paymentRepository.save(payment, mockConnection));

            verify(mockPreparedStatement, times(1)).setDouble(2, 150.0);
            verify(mockPreparedStatement, times(1)).setString(3, "CREDIT_CARD");
            verify(mockPreparedStatement, times(1)).setLong(4, 1L);
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti plaćanje po ID-u")
    void testFindPaymentById() throws SQLException {
        // Arrange
        Long paymentId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getTimestamp("PAYMENT_DATE")).thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getDouble("AMOUNT")).thenReturn(150.0);
        when(mockResultSet.getString("PAYMENT_METHOD")).thenReturn("CREDIT_CARD");
        when(mockResultSet.getLong("INVOICE_ID")).thenReturn(1L);

        // Act
        Optional<Payment> result = paymentRepository.findById(paymentId, mockConnection);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(150.0, result.get().getAmount());
        assertEquals("CREDIT_CARD", result.get().getPaymentMethod());
    }

    @Test
    @DisplayName("Trebalo bi vratiti prazan Optional ako plaćanje ne postoji")
    void testFindPaymentByIdNotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Optional<Payment> result = paymentRepository.findById(999L, mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sva plaćanja")
    void testFindAllPayments() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(mockResultSet.getLong("ID"))
                .thenReturn(1L)
                .thenReturn(2L);
        when(mockResultSet.getTimestamp("PAYMENT_DATE"))
                .thenReturn(Timestamp.valueOf(LocalDateTime.now()))
                .thenReturn(Timestamp.valueOf(LocalDateTime.now()));
        when(mockResultSet.getDouble("AMOUNT"))
                .thenReturn(150.0)
                .thenReturn(200.0);
        when(mockResultSet.getString("PAYMENT_METHOD"))
                .thenReturn("CREDIT_CARD")
                .thenReturn("CASH");
        when(mockResultSet.getLong("INVOICE_ID"))
                .thenReturn(1L)
                .thenReturn(2L);

        // Act
        List<Payment> result = paymentRepository.findAll(mockConnection);

        // Assert
        assertEquals(2, result.size());
        assertEquals(150.0, result.get(0).getAmount());
        assertEquals(200.0, result.get(1).getAmount());
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati plaćanje")
    void testUpdatePayment() throws SQLException {
        // Arrange
        Payment payment = new Payment(1L, LocalDateTime.now(), 250.0, "BANK_TRANSFER", 1L);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> paymentRepository.update(payment, mockConnection));

            verify(mockPreparedStatement, times(1)).setDouble(2, 250.0);
            verify(mockPreparedStatement, times(1)).setString(3, "BANK_TRANSFER");
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno obrisati plaćanje")
    void testDeletePayment() throws SQLException {
        // Arrange
        Long paymentId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> paymentRepository.delete(paymentId, mockConnection));

            verify(mockPreparedStatement, times(1)).setLong(1, paymentId);
        }
    }
}
