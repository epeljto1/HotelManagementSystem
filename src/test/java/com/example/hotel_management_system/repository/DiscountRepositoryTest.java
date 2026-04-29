package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Discount;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Discount Repository Tests")
class DiscountRepositoryTest {

    private DiscountRepository discountRepository;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        discountRepository = new DiscountRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    @DisplayName("Trebalo bi uspešno sačuvati popust")
    void testSaveDiscount() throws SQLException {
        // Arrange
        Discount discount = new Discount(1L, "Summer Sale", 20.0, LocalDate.now(),
                LocalDate.now().plusDays(30), "Letnjena akcija");

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> discountRepository.save(discount, mockConnection));

            verify(mockPreparedStatement, times(1)).setString(1, "Summer Sale");
            verify(mockPreparedStatement, times(1)).setDouble(2, 20.0);
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti popust po ID-u")
    void testFindDiscountById() throws SQLException {
        // Arrange
        Long discountId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getString("NAME")).thenReturn("Summer Sale");
        when(mockResultSet.getDouble("PERCENTAGE")).thenReturn(20.0);
        when(mockResultSet.getDate("START_DATE")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getDate("END_DATE")).thenReturn(Date.valueOf(LocalDate.now().plusDays(30)));
        when(mockResultSet.getString("DESCRIPTION")).thenReturn("Letnjena akcija");

        // Act
        Optional<Discount> result = discountRepository.findById(discountId, mockConnection);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Summer Sale", result.get().getName());
        assertEquals(20.0, result.get().getPercentage());
    }

    @Test
    @DisplayName("Trebalo bi vratiti prazan Optional ako popust ne postoji")
    void testFindDiscountByIdNotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Optional<Discount> result = discountRepository.findById(999L, mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sve popuste")
    void testFindAllDiscounts() throws SQLException {
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
        when(mockResultSet.getString("NAME"))
                .thenReturn("Summer Sale")
                .thenReturn("Winter Sale");
        when(mockResultSet.getDouble("PERCENTAGE"))
                .thenReturn(20.0)
                .thenReturn(30.0);
        when(mockResultSet.getDate("START_DATE"))
                .thenReturn(Date.valueOf(LocalDate.now()))
                .thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getDate("END_DATE"))
                .thenReturn(Date.valueOf(LocalDate.now().plusDays(30)))
                .thenReturn(Date.valueOf(LocalDate.now().plusDays(60)));
        when(mockResultSet.getString("DESCRIPTION"))
                .thenReturn("Letnjena akcija")
                .thenReturn("Zimska akcija");

        // Act
        List<Discount> result = discountRepository.findAll(mockConnection);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Summer Sale", result.get(0).getName());
        assertEquals("Winter Sale", result.get(1).getName());
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati popust")
    void testUpdateDiscount() throws SQLException {
        // Arrange
        Discount discount = new Discount(1L, "Summer Sale Updated", 25.0, LocalDate.now(),
                LocalDate.now().plusDays(45), "Ažurirana letnja akcija");

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> discountRepository.update(discount, mockConnection));

            verify(mockPreparedStatement, times(1)).setString(1, "Summer Sale Updated");
            verify(mockPreparedStatement, times(1)).setDouble(2, 25.0);
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno obrisati popust")
    void testDeleteDiscount() throws SQLException {
        // Arrange
        Long discountId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> discountRepository.delete(discountId, mockConnection));

            verify(mockPreparedStatement, times(1)).setLong(1, discountId);
        }
    }
}
