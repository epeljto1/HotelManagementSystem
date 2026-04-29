package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Invoice;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Invoice Repository Tests")
class InvoiceRepositoryTest {

    private InvoiceRepository invoiceRepository;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        invoiceRepository = new InvoiceRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    @DisplayName("Trebalo bi uspešno sačuvati fakturu")
    void testSaveInvoice() throws SQLException {
        // Arrange
        Invoice invoice = new Invoice(1L, LocalDate.now(), new BigDecimal("350.00"),
                "ISSUED", 1L, null, null, new BigDecimal("350.00"));

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> invoiceRepository.save(invoice, mockConnection));

            verify(mockPreparedStatement, times(1)).setDate(1, Date.valueOf(LocalDate.now()));
            verify(mockPreparedStatement, times(1)).setBigDecimal(2, new BigDecimal("350.00"));
            verify(mockPreparedStatement, times(1)).setString(3, "ISSUED");
            verify(mockPreparedStatement, times(1)).setLong(4, 1L);
        }
    }

    @Test
    @DisplayName("Trebalo bi sačuvati fakturu sa popustom")
    void testSaveInvoiceWithDiscount() throws SQLException {
        // Arrange
        Invoice invoice = new Invoice(1L, LocalDate.now(), new BigDecimal("350.00"),
                "ISSUED", 1L, 1L, new BigDecimal("70.00"), new BigDecimal("280.00"));

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> invoiceRepository.save(invoice, mockConnection));

            verify(mockPreparedStatement, times(1)).setLong(5, 1L);
            verify(mockPreparedStatement, times(1)).setBigDecimal(6, new BigDecimal("70.00"));
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti fakturu po ID-u")
    void testFindInvoiceById() throws SQLException {
        // Arrange
        Long invoiceId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getDate("ISSUE_DATE")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getBigDecimal("TOTAL_AMOUNT")).thenReturn(new BigDecimal("350.00"));
        when(mockResultSet.getString("STATUS")).thenReturn("ISSUED");
        when(mockResultSet.getLong("STAY_ID")).thenReturn(1L);
        when(mockResultSet.getLong("DISCOUNT_ID")).thenReturn(0L);
        when(mockResultSet.getBigDecimal("DISCOUNT_AMOUNT")).thenReturn(null);
        when(mockResultSet.getBigDecimal("FINAL_AMOUNT")).thenReturn(new BigDecimal("350.00"));

        // Act
        Invoice result = invoiceRepository.findById(invoiceId, mockConnection);

        // Assert
        assertNotNull(result);
        assertEquals("ISSUED", result.getStatus());
        assertEquals(new BigDecimal("350.00"), result.getTotalAmount());
    }

    @Test
    @DisplayName("Trebalo bi vratiti null ako faktura ne postoji")
    void testFindInvoiceByIdNotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Invoice result = invoiceRepository.findById(999L, mockConnection);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sve fakture")
    void testFindAllInvoices() throws SQLException {
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
        when(mockResultSet.getDate("ISSUE_DATE"))
                .thenReturn(Date.valueOf(LocalDate.now()))
                .thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getBigDecimal("TOTAL_AMOUNT"))
                .thenReturn(new BigDecimal("350.00"))
                .thenReturn(new BigDecimal("500.00"));
        when(mockResultSet.getString("STATUS"))
                .thenReturn("ISSUED")
                .thenReturn("PAID");
        when(mockResultSet.getLong("STAY_ID"))
                .thenReturn(1L)
                .thenReturn(2L);
        when(mockResultSet.getLong("DISCOUNT_ID"))
                .thenReturn(0L)
                .thenReturn(1L);
        when(mockResultSet.getBigDecimal("DISCOUNT_AMOUNT"))
                .thenReturn(null)
                .thenReturn(new BigDecimal("50.00"));
        when(mockResultSet.getBigDecimal("FINAL_AMOUNT"))
                .thenReturn(new BigDecimal("350.00"))
                .thenReturn(new BigDecimal("450.00"));

        // Act
        List<Invoice> result = invoiceRepository.findAll(mockConnection);

        // Assert
        assertEquals(2, result.size());
        assertEquals("ISSUED", result.get(0).getStatus());
        assertEquals("PAID", result.get(1).getStatus());
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati fakturu")
    void testUpdateInvoice() throws SQLException {
        // Arrange
        Invoice invoice = new Invoice(1L, LocalDate.now(), new BigDecimal("350.00"),
                "PAID", 1L, null, null, new BigDecimal("350.00"));

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> invoiceRepository.update(1L, invoice, mockConnection));

            verify(mockPreparedStatement, times(1)).setString(3, "PAID");
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno obrisati fakturu")
    void testDeleteInvoice() throws SQLException {
        // Arrange
        Long invoiceId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> invoiceRepository.delete(invoiceId, mockConnection));

            verify(mockPreparedStatement, times(1)).setLong(1, invoiceId);
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti fakturu po ID-u boravka")
    void testFindByStayId() throws SQLException {
        // Arrange
        Long stayId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getDate("ISSUE_DATE")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getBigDecimal("TOTAL_AMOUNT")).thenReturn(new BigDecimal("350.00"));
        when(mockResultSet.getString("STATUS")).thenReturn("ISSUED");
        when(mockResultSet.getLong("STAY_ID")).thenReturn(stayId);
        when(mockResultSet.getLong("DISCOUNT_ID")).thenReturn(0L);
        when(mockResultSet.getBigDecimal("DISCOUNT_AMOUNT")).thenReturn(null);
        when(mockResultSet.getBigDecimal("FINAL_AMOUNT")).thenReturn(new BigDecimal("350.00"));

        // Act
        Invoice result = invoiceRepository.findByStayId(stayId, mockConnection);

        // Assert
        assertNotNull(result);
        assertEquals(stayId, result.getStayId());
    }
}
