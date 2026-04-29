package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Guest;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Guest Repository Tests")
class GuestRepositoryTest {

    private GuestRepository guestRepository;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        guestRepository = new GuestRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    @DisplayName("Trebalo bi uspešno sačuvati gosta")
    void testSaveGuest() throws SQLException {
        // Arrange
        Guest guest = new Guest(1L, "Marko", "Marković", "marko@example.com",
                "+381123456789", new Date(), "12345678", 1L);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> guestRepository.save(guest, mockConnection));

            verify(mockPreparedStatement, times(1)).setLong(1, 1L);
            verify(mockPreparedStatement, times(1)).setString(2, "Marko");
            verify(mockPreparedStatement, times(1)).setString(3, "Marković");
            verify(mockPreparedStatement, times(1)).setString(4, "marko@example.com");
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti gosta po ID-u")
    void testFindGuestById() throws SQLException {
        // Arrange
        Long guestId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getString("FIRST_NAME")).thenReturn("Marko");
        when(mockResultSet.getString("LAST_NAME")).thenReturn("Marković");
        when(mockResultSet.getString("EMAIL")).thenReturn("marko@example.com");
        when(mockResultSet.getString("PHONE_NUMBER")).thenReturn("+381123456789");
        when(mockResultSet.getDate("DATE_OF_BIRTH")).thenReturn(new java.sql.Date(System.currentTimeMillis()));
        when(mockResultSet.getString("DOCUMENT_NUMBER")).thenReturn("12345678");
        when(mockResultSet.getLong("ADDRESS_ID")).thenReturn(1L);

        // Act
        Optional<Guest> result = guestRepository.findById(guestId, mockConnection);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Marko", result.get().getFirstName());
        assertEquals("marko@example.com", result.get().getEmail());
    }

    @Test
    @DisplayName("Trebalo bi vratiti prazan Optional ako gost ne postoji")
    void testFindGuestByIdNotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Optional<Guest> result = guestRepository.findById(999L, mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sve goste")
    void testFindAllGuests() throws SQLException {
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
        when(mockResultSet.getString("FIRST_NAME"))
                .thenReturn("Marko")
                .thenReturn("Jovan");
        when(mockResultSet.getString("LAST_NAME"))
                .thenReturn("Marković")
                .thenReturn("Jovanović");
        when(mockResultSet.getString("EMAIL"))
                .thenReturn("marko@example.com")
                .thenReturn("jovan@example.com");
        when(mockResultSet.getString("PHONE_NUMBER"))
                .thenReturn("+381123456789")
                .thenReturn("+381987654321");
        when(mockResultSet.getDate("DATE_OF_BIRTH"))
                .thenReturn(new java.sql.Date(System.currentTimeMillis()))
                .thenReturn(new java.sql.Date(System.currentTimeMillis()));
        when(mockResultSet.getString("DOCUMENT_NUMBER"))
                .thenReturn("12345678")
                .thenReturn("87654321");
        when(mockResultSet.getLong("ADDRESS_ID"))
                .thenReturn(1L)
                .thenReturn(2L);

        // Act
        List<Guest> result = guestRepository.findAll(mockConnection);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Marko", result.get(0).getFirstName());
        assertEquals("Jovan", result.get(1).getFirstName());
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati gosta")
    void testUpdateGuest() throws SQLException {
        // Arrange
        Guest guest = new Guest(1L, "Marko", "Marković Updated", "marko.updated@example.com",
                "+381111111111", new Date(), "87654321", 1L);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> guestRepository.update(guest, mockConnection));

            verify(mockPreparedStatement, times(1)).setString(1, "Marko");
            verify(mockPreparedStatement, times(1)).setString(2, "Marković Updated");
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno obrisati gosta")
    void testDeleteGuest() throws SQLException {
        // Arrange
        Long guestId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> guestRepository.delete(guestId, mockConnection));

            verify(mockPreparedStatement, times(1)).setLong(1, guestId);
        }
    }
}
