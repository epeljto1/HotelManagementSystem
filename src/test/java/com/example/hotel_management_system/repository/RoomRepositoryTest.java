package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.enums.RoomStatus;
import com.example.hotel_management_system.model.Room;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Room Repository Tests")
class RoomRepositoryTest {

    private RoomRepository roomRepository;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        roomRepository = new RoomRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    @DisplayName("Trebalo bi uspešno sačuvati sobu")
    void testSaveRoom() throws SQLException {
        // Arrange
        Room room = new Room(1L, "101", 1, RoomStatus.AVAILABLE, 1L, 1L);

        when(mockConnection.prepareStatement(anyString(), any(String[].class)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.getGeneratedKeys())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(1L);

        // Mock DatabaseLogger
        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> roomRepository.save(room, mockConnection));

            verify(mockPreparedStatement, times(1)).setString(1, "101");
            verify(mockPreparedStatement, times(1)).setInt(2, 1);
            verify(mockPreparedStatement, times(1)).setString(3, "AVAILABLE");
            verify(mockPreparedStatement, times(1)).setLong(4, 1L);
            verify(mockPreparedStatement, times(1)).setLong(5, 1L);
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sobu po ID-u")
    void testFindRoomById() throws SQLException {
        // Arrange
        Long roomId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getString("ROOM_NUMBER")).thenReturn("101");
        when(mockResultSet.getInt("FLOOR_NUMBER")).thenReturn(1);
        when(mockResultSet.getString("STATUS")).thenReturn("AVAILABLE");
        when(mockResultSet.getLong("HOTEL_ID")).thenReturn(1L);
        when(mockResultSet.getLong("ROOM_TYPE_ID")).thenReturn(1L);

        // Act
        Optional<Room> result = roomRepository.findById(roomId, mockConnection);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("101", result.get().getRoomNumber());
        assertEquals(RoomStatus.AVAILABLE, result.get().getStatus());
        verify(mockPreparedStatement, times(1)).setLong(1, roomId);
    }

    @Test
    @DisplayName("Trebalo bi vratiti prazan Optional ako soba ne postoji")
    void testFindRoomByIdNotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Optional<Room> result = roomRepository.findById(999L, mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sve sobe")
    void testFindAllRooms() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next())
                .thenReturn(true)  // Prva soba
                .thenReturn(true)  // Druga soba
                .thenReturn(false); // Kraj rezultata

        when(mockResultSet.getLong("ID"))
                .thenReturn(1L)
                .thenReturn(2L);
        when(mockResultSet.getString("ROOM_NUMBER"))
                .thenReturn("101")
                .thenReturn("102");
        when(mockResultSet.getInt("FLOOR_NUMBER"))
                .thenReturn(1)
                .thenReturn(1);
        when(mockResultSet.getString("STATUS"))
                .thenReturn("AVAILABLE")
                .thenReturn("OCCUPIED");
        when(mockResultSet.getLong("HOTEL_ID"))
                .thenReturn(1L)
                .thenReturn(1L);
        when(mockResultSet.getLong("ROOM_TYPE_ID"))
                .thenReturn(1L)
                .thenReturn(2L);

        // Act
        List<Room> result = roomRepository.findAll(mockConnection);

        // Assert
        assertEquals(2, result.size());
        assertEquals("101", result.get(0).getRoomNumber());
        assertEquals("102", result.get(1).getRoomNumber());
        assertEquals(RoomStatus.AVAILABLE, result.get(0).getStatus());
        assertEquals(RoomStatus.OCCUPIED, result.get(1).getStatus());
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati sobu")
    void testUpdateRoom() throws SQLException {
        // Arrange
        Room room = new Room(1L, "101", 2, RoomStatus.OUT_OF_SERVICE, 1L, 1L);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        // Mock DatabaseLogger
        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> roomRepository.update(room, mockConnection));

            verify(mockPreparedStatement, times(1)).setString(1, "101");
            verify(mockPreparedStatement, times(1)).setInt(2, 2);
            verify(mockPreparedStatement, times(1)).setString(3, "OUT_OF_SERVICE");
            verify(mockPreparedStatement, times(1)).setLong(4, 1L);
            verify(mockPreparedStatement, times(1)).setLong(5, 1L);
            verify(mockPreparedStatement, times(1)).setLong(6, 1L);
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati status sobe")
    void testUpdateRoomStatus() throws SQLException {
        // Arrange
        Long roomId = 1L;
        RoomStatus newStatus = RoomStatus.OCCUPIED;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        // Mock DatabaseLogger
        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> roomRepository.updateStatus(roomId, newStatus, mockConnection));

            verify(mockPreparedStatement, times(1)).setString(1, "OCCUPIED");
            verify(mockPreparedStatement, times(1)).setLong(2, roomId);
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno obrisati sobu")
    void testDeleteRoom() throws SQLException {
        // Arrange
        Long roomId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        // Mock DatabaseLogger
        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> roomRepository.delete(roomId, mockConnection));

            verify(mockPreparedStatement, times(1)).setLong(1, roomId);
        }
    }
}