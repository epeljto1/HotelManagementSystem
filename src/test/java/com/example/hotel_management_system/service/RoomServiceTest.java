package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.RoomDTO;
import com.example.hotel_management_system.enums.RoomStatus;
import com.example.hotel_management_system.model.Room;
import com.example.hotel_management_system.repository.RoomRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Room Service Tests")
class RoomServiceTest {

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private Connection mockConnection;

    @InjectMocks
    private RoomService roomService;

    private RoomDTO testRoomDTO;
    private Room testRoom;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testRoomDTO = new RoomDTO(
                1L,
                "101",
                1,
                RoomStatus.AVAILABLE,
                1L,
                1L
        );

        testRoom = new Room(
                1L,
                "101",
                1,
                RoomStatus.AVAILABLE,
                1L,
                1L
        );
    }

    @Test
    @DisplayName("Trebalo bi uspešno kreirati sobu")
    void testCreateRoomSuccess() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);
            doNothing().when(mockConnection).close();

            // Act
            RoomDTO result = roomService.createRoom(testRoomDTO);

            // Assert
            assertNotNull(result);
            assertEquals("101", result.getRoomNumber());
            assertEquals(RoomStatus.AVAILABLE, result.getStatus());
            verify(roomRepository, times(1)).save(any(Room.class), any(Connection.class));
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sobu po ID-u")
    void testGetRoomByIdSuccess() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);
            doNothing().when(mockConnection).close();

            when(roomRepository.findById(eq(1L), any(Connection.class)))
                    .thenReturn(Optional.of(testRoom));

            // Act
            RoomDTO result = roomService.getRoomById(1L);

            // Assert
            assertNotNull(result);
            assertEquals("101", result.getRoomNumber());
            verify(roomRepository, times(1)).findById(eq(1L), any(Connection.class));
        }
    }

    @Test
    @DisplayName("Trebalo bi vratiti null ako soba ne postoji")
    void testGetRoomByIdNotFound() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);
            doNothing().when(mockConnection).close();

            when(roomRepository.findById(eq(999L), any(Connection.class)))
                    .thenReturn(Optional.empty());

            // Act
            RoomDTO result = roomService.getRoomById(999L);

            // Assert
            assertNull(result);
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sve sobe")
    void testGetAllRoomsSuccess() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);
            doNothing().when(mockConnection).close();

            List<Room> rooms = new ArrayList<>();
            rooms.add(testRoom);
            rooms.add(new Room(2L, "102", 1, RoomStatus.OCCUPIED, 1L, 1L));

            when(roomRepository.findAll(any(Connection.class)))
                    .thenReturn(rooms);

            // Act
            List<RoomDTO> result = roomService.getAllRooms();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(roomRepository, times(1)).findAll(any(Connection.class));
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati sobu")
    void testUpdateRoomSuccess() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);
            doNothing().when(mockConnection).close();

            RoomDTO updatedDTO = new RoomDTO(1L, "101", 2, RoomStatus.OUT_OF_SERVICE, 1L, 1L);

            // Act
            RoomDTO result = roomService.updateRoom(1L, updatedDTO);

            // Assert
            assertNotNull(result);
            assertEquals("101", result.getRoomNumber());
            assertEquals(2, result.getFloorNumber());
            verify(roomRepository, times(1)).update(any(Room.class), any(Connection.class));
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno obrisati sobu")
    void testDeleteRoomSuccess() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);
            doNothing().when(mockConnection).close();

            // Act
            boolean result = roomService.deleteRoom(1L);

            // Assert
            assertTrue(result);
            verify(roomRepository, times(1)).delete(eq(1L), any(Connection.class));
        }
    }

    @Test
    @DisplayName("Trebalo bi vratiti false ako brisanje sobe ne uspe")
    void testDeleteRoomFailure() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);
            doNothing().when(mockConnection).close();

            doThrow(new SQLException("Database error"))
                    .when(roomRepository).delete(anyLong(), any(Connection.class));

            // Act
            boolean result = roomService.deleteRoom(1L);

            // Assert
            assertFalse(result);
        }
    }
}