package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.enums.ReservationStatus;
import com.example.hotel_management_system.model.Reservation;
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

@DisplayName("Reservation Repository Tests")
class ReservationRepositoryTest {

    private ReservationRepository reservationRepository;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        reservationRepository = new ReservationRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    @DisplayName("Trebalo bi uspešno sačuvati rezervaciju")
    void testSaveReservation() throws SQLException {
        // Arrange
        Date now = new Date();
        Reservation reservation = new Reservation(1L, now, now, now, 2,
                ReservationStatus.CONFIRMED, 350.0, 1L, 1L, 1L);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> reservationRepository.save(reservation, mockConnection));

            verify(mockPreparedStatement, times(1)).setInt(4, 2);
            verify(mockPreparedStatement, times(1)).setString(5, "CONFIRMED");
            verify(mockPreparedStatement, times(1)).setDouble(6, 350.0);
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti rezervaciju po ID-u")
    void testFindReservationById() throws SQLException {
        // Arrange
        Long reservationId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getTimestamp("RESERVATION_DATE")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("CHECK_IN_DATE")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("CHECK_OUT_DATE")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("NUMBER_OF_GUESTS")).thenReturn(2);
        when(mockResultSet.getString("STATUS")).thenReturn("CONFIRMED");
        when(mockResultSet.getDouble("TOTAL_PRICE")).thenReturn(350.0);
        when(mockResultSet.getLong("GUEST_ID")).thenReturn(1L);
        when(mockResultSet.getLong("ROOM_ID")).thenReturn(1L);
        when(mockResultSet.getLong("CREATED_BY")).thenReturn(1L);

        // Act
        Optional<Reservation> result = reservationRepository.findById(reservationId, mockConnection);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ReservationStatus.CONFIRMED, result.get().getStatus());
        assertEquals(350.0, result.get().getTotalPrice());
    }

    @Test
    @DisplayName("Trebalo bi vratiti prazan Optional ako rezervacija ne postoji")
    void testFindReservationByIdNotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Optional<Reservation> result = reservationRepository.findById(999L, mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sve rezervacije")
    void testFindAllReservations() throws SQLException {
        // Arrange
        when(mockConnection.createStatement()).thenReturn(mock(Statement.class));
        Statement stmt = mockConnection.createStatement();
        when(stmt.executeQuery(anyString())).thenReturn(mockResultSet);
        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);

        when(mockResultSet.getLong("ID"))
                .thenReturn(1L)
                .thenReturn(2L);
        when(mockResultSet.getTimestamp("RESERVATION_DATE"))
                .thenReturn(new Timestamp(System.currentTimeMillis()))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("CHECK_IN_DATE"))
                .thenReturn(new Timestamp(System.currentTimeMillis()))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("CHECK_OUT_DATE"))
                .thenReturn(new Timestamp(System.currentTimeMillis()))
                .thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("NUMBER_OF_GUESTS"))
                .thenReturn(2)
                .thenReturn(1);
        when(mockResultSet.getString("STATUS"))
                .thenReturn("CONFIRMED")
                .thenReturn("PENDING");
        when(mockResultSet.getDouble("TOTAL_PRICE"))
                .thenReturn(350.0)
                .thenReturn(150.0);
        when(mockResultSet.getLong("GUEST_ID"))
                .thenReturn(1L)
                .thenReturn(2L);
        when(mockResultSet.getLong("ROOM_ID"))
                .thenReturn(1L)
                .thenReturn(2L);
        when(mockResultSet.getLong("CREATED_BY"))
                .thenReturn(1L)
                .thenReturn(1L);

        // Act
        List<Reservation> result = reservationRepository.findAll(mockConnection);

        // Assert
        assertEquals(2, result.size());
        assertEquals(ReservationStatus.CONFIRMED, result.get(0).getStatus());
        assertEquals(ReservationStatus.PENDING, result.get(1).getStatus());
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati rezervaciju")
    void testUpdateReservation() throws SQLException {
        // Arrange
        Date now = new Date();
        Reservation reservation = new Reservation(1L, now, now, now, 3,
                ReservationStatus.PENDING, 500.0, 1L, 1L, 1L);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> reservationRepository.update(reservation, mockConnection));

            verify(mockPreparedStatement, times(1)).setInt(4, 3);
            verify(mockPreparedStatement, times(1)).setDouble(6, 500.0);
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati status rezervacije")
    void testUpdateReservationStatus() throws SQLException {
        // Arrange
        Long reservationId = 1L;
        ReservationStatus newStatus = ReservationStatus.CANCELLED;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> reservationRepository.updateStatus(reservationId, newStatus, mockConnection));

            verify(mockPreparedStatement, times(1)).setString(1, "CANCELLED");
            verify(mockPreparedStatement, times(1)).setLong(2, reservationId);
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno obrisati rezervaciju")
    void testDeleteReservation() throws SQLException {
        // Arrange
        Long reservationId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> reservationRepository.delete(reservationId, mockConnection));

            verify(mockPreparedStatement, times(1)).setLong(1, reservationId);
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti rezervaciju po ID-u i statusu")
    void testFindByIdAndStatus() throws SQLException {
        // Arrange
        Long reservationId = 1L;
        ReservationStatus status = ReservationStatus.CONFIRMED;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getTimestamp("RESERVATION_DATE")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("CHECK_IN_DATE")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getTimestamp("CHECK_OUT_DATE")).thenReturn(new Timestamp(System.currentTimeMillis()));
        when(mockResultSet.getInt("NUMBER_OF_GUESTS")).thenReturn(2);
        when(mockResultSet.getString("STATUS")).thenReturn("CONFIRMED");
        when(mockResultSet.getDouble("TOTAL_PRICE")).thenReturn(350.0);
        when(mockResultSet.getLong("GUEST_ID")).thenReturn(1L);
        when(mockResultSet.getLong("ROOM_ID")).thenReturn(1L);
        when(mockResultSet.getLong("CREATED_BY")).thenReturn(1L);

        // Act
        Optional<Reservation> result = reservationRepository.findByIdAndStatus(reservationId, status, mockConnection);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(ReservationStatus.CONFIRMED, result.get().getStatus());
    }
}
