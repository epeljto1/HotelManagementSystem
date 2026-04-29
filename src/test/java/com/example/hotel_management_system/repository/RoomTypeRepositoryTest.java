package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.RoomType;
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

@DisplayName("Room Type Repository Tests")
class RoomTypeRepositoryTest {

    private RoomTypeRepository roomTypeRepository;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        roomTypeRepository = new RoomTypeRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    @DisplayName("Trebalo bi uspešno sačuvati tip sobe")
    void testSaveRoomType() throws SQLException {
        // Arrange
        RoomType roomType = new RoomType(1L, "Deluxe", "Luksuzna soba sa pogledom", 2, 150.0);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> roomTypeRepository.save(roomType, mockConnection));

            verify(mockPreparedStatement, times(1)).setLong(1, 1L);
            verify(mockPreparedStatement, times(1)).setString(2, "Deluxe");
            verify(mockPreparedStatement, times(1)).setInt(4, 2);
            verify(mockPreparedStatement, times(1)).setDouble(5, 150.0);
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti tip sobe po ID-u")
    void testFindRoomTypeById() throws SQLException {
        // Arrange
        Long roomTypeId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getString("NAME")).thenReturn("Deluxe");
        when(mockResultSet.getString("DESCRIPTION")).thenReturn("Luksuzna soba");
        when(mockResultSet.getInt("CAPACITY")).thenReturn(2);
        when(mockResultSet.getDouble("PRICE_PER_NIGHT")).thenReturn(150.0);

        // Act
        Optional<RoomType> result = roomTypeRepository.findById(roomTypeId, mockConnection);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Deluxe", result.get().getName());
        assertEquals(150.0, result.get().getPricePerNight());
    }

    @Test
    @DisplayName("Trebalo bi vratiti prazan Optional ako tip sobe ne postoji")
    void testFindRoomTypeByIdNotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Optional<RoomType> result = roomTypeRepository.findById(999L, mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sve tipove soba")
    void testFindAllRoomTypes() throws SQLException {
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
                .thenReturn("Deluxe")
                .thenReturn("Standard");
        when(mockResultSet.getString("DESCRIPTION"))
                .thenReturn("Luksuzna soba")
                .thenReturn("Standardna soba");
        when(mockResultSet.getInt("CAPACITY"))
                .thenReturn(2)
                .thenReturn(1);
        when(mockResultSet.getDouble("PRICE_PER_NIGHT"))
                .thenReturn(150.0)
                .thenReturn(80.0);

        // Act
        List<RoomType> result = roomTypeRepository.findAll(mockConnection);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Deluxe", result.get(0).getName());
        assertEquals("Standard", result.get(1).getName());
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati tip sobe")
    void testUpdateRoomType() throws SQLException {
        // Arrange
        RoomType roomType = new RoomType(1L, "Deluxe Suite", "Luksuzna soba sa balkonom", 3, 200.0);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> roomTypeRepository.update(roomType, mockConnection));

            verify(mockPreparedStatement, times(1)).setString(1, "Deluxe Suite");
            verify(mockPreparedStatement, times(1)).setInt(3, 3);
            verify(mockPreparedStatement, times(1)).setDouble(4, 200.0);
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno obrisati tip sobe")
    void testDeleteRoomType() throws SQLException {
        // Arrange
        Long roomTypeId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> roomTypeRepository.delete(roomTypeId, mockConnection));

            verify(mockPreparedStatement, times(1)).setLong(1, roomTypeId);
        }
    }
}
