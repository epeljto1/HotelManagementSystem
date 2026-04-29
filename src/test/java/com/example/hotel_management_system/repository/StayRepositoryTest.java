package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Stay;
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

@DisplayName("Stay Repository Tests")
class StayRepositoryTest {

    private StayRepository stayRepository;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        stayRepository = new StayRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    @DisplayName("Trebalo bi uspešno sačuvati boravak")
    void testSaveStay() throws SQLException {
        // Arrange
        LocalDateTime checkIn = LocalDateTime.now();
        LocalDateTime checkOut = LocalDateTime.now().plusDays(3);
        Stay stay = new Stay(1L, checkIn, checkOut, 1L, 350.0);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> stayRepository.save(stay, mockConnection));

            verify(mockPreparedStatement, times(1)).setLong(1, 1L);
            verify(mockPreparedStatement, times(1)).setLong(4, 1L);
            verify(mockPreparedStatement, times(1)).setDouble(5, 350.0);
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti boravak po ID-u")
    void testFindStayById() throws SQLException {
        // Arrange
        Long stayId = 1L;
        LocalDateTime checkIn = LocalDateTime.now();
        LocalDateTime checkOut = LocalDateTime.now().plusDays(3);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getTimestamp("CHECK_IN_TIME")).thenReturn(Timestamp.valueOf(checkIn));
        when(mockResultSet.getTimestamp("CHECK_OUT_TIME")).thenReturn(Timestamp.valueOf(checkOut));
        when(mockResultSet.getLong("RESERVATION_ID")).thenReturn(1L);
        when(mockResultSet.getDouble("ACTUAL_TOTAL_PRICE")).thenReturn(350.0);

        // Act
        Optional<Stay> result = stayRepository.findById(stayId, mockConnection);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getReservationId());
        assertEquals(350.0, result.get().getActualTotalPrice());
    }

    @Test
    @DisplayName("Trebalo bi vratiti prazan Optional ako boravak ne postoji")
    void testFindStayByIdNotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Optional<Stay> result = stayRepository.findById(999L, mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi preuzeti boravak po ID-u rezervacije")
    void testFindStayByReservationId() throws SQLException {
        // Arrange
        Long reservationId = 1L;
        LocalDateTime checkIn = LocalDateTime.now();
        LocalDateTime checkOut = LocalDateTime.now().plusDays(3);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getTimestamp("CHECK_IN_TIME")).thenReturn(Timestamp.valueOf(checkIn));
        when(mockResultSet.getTimestamp("CHECK_OUT_TIME")).thenReturn(Timestamp.valueOf(checkOut));
        when(mockResultSet.getLong("RESERVATION_ID")).thenReturn(reservationId);
        when(mockResultSet.getDouble("ACTUAL_TOTAL_PRICE")).thenReturn(350.0);

        // Act
        Optional<Stay> result = stayRepository.findByReservationId(reservationId, mockConnection);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(reservationId, result.get().getReservationId());
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sve boravke")
    void testFindAllStays() throws SQLException {
        // Arrange
        LocalDateTime checkIn = LocalDateTime.now();
        LocalDateTime checkOut = LocalDateTime.now().plusDays(3);

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
        when(mockResultSet.getTimestamp("CHECK_IN_TIME"))
                .thenReturn(Timestamp.valueOf(checkIn))
                .thenReturn(Timestamp.valueOf(checkIn));
        when(mockResultSet.getTimestamp("CHECK_OUT_TIME"))
                .thenReturn(Timestamp.valueOf(checkOut))
                .thenReturn(Timestamp.valueOf(checkOut));
        when(mockResultSet.getLong("RESERVATION_ID"))
                .thenReturn(1L)
                .thenReturn(2L);
        when(mockResultSet.getDouble("ACTUAL_TOTAL_PRICE"))
                .thenReturn(350.0)
                .thenReturn(500.0);

        // Act
        List<Stay> result = stayRepository.findAll(mockConnection);

        // Assert
        assertEquals(2, result.size());
        assertEquals(350.0, result.get(0).getActualTotalPrice());
        assertEquals(500.0, result.get(1).getActualTotalPrice());
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati boravak")
    void testUpdateStay() throws SQLException {
        // Arrange
        LocalDateTime checkIn = LocalDateTime.now();
        LocalDateTime checkOut = LocalDateTime.now().plusDays(5);
        Stay stay = new Stay(1L, checkIn, checkOut, 1L, 500.0);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> stayRepository.update(stay, mockConnection));

            // Ispravka: redosled parametara je setTimestamp(1), setTimestamp(2), setLong(3), setDouble(4), setLong(5)
            verify(mockPreparedStatement, times(1)).setTimestamp(1, Timestamp.valueOf(checkIn));
            verify(mockPreparedStatement, times(1)).setTimestamp(2, Timestamp.valueOf(checkOut));
            verify(mockPreparedStatement, times(1)).setLong(3, 1L);
            verify(mockPreparedStatement, times(1)).setDouble(4, 500.0);
            verify(mockPreparedStatement, times(1)).setLong(5, 1L);
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno obrisati boravak")
    void testDeleteStay() throws SQLException {
        // Arrange
        Long stayId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> stayRepository.delete(stayId, mockConnection));

            verify(mockPreparedStatement, times(1)).setLong(1, stayId);
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sledeći ID")
    void testGetNextId() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("NEXT_ID")).thenReturn(5L);

        // Act
        Long nextId = stayRepository.getNextId(mockConnection);

        // Assert
        assertEquals(5L, nextId);
    }

    @Test
    @DisplayName("Trebalo bi vratiti prazan Optional ako boravak za rezervaciju ne postoji")
    void testFindStayByReservationIdNotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Optional<Stay> result = stayRepository.findByReservationId(999L, mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sve boravke kada je lista prazna")
    void testFindAllStaysEmpty() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<Stay> result = stayRepository.findAll(mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi baciti SQLException pri greški u getNextId")
    void testGetNextIdWithException() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Database connection failed"));

        // Act & Assert
        assertThrows(SQLException.class, () -> stayRepository.getNextId(mockConnection));
    }

    @Test
    @DisplayName("Trebalo bi sačuvati boravak sa null checkout vremenom")
    void testSaveStayWithNullCheckOut() throws SQLException {
        // Arrange
        LocalDateTime checkIn = LocalDateTime.now();
        Stay stay = new Stay(1L, checkIn, null, 1L, 0.0);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> stayRepository.save(stay, mockConnection));

            // Provera da je setNull pozvan za checkout vreme
            verify(mockPreparedStatement, times(1)).setNull(3, Types.TIMESTAMP);
        }
    }
}