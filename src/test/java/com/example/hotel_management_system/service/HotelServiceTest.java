package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.HotelDTO;
import com.example.hotel_management_system.model.Hotel;
import com.example.hotel_management_system.repository.HotelRepository;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@DisplayName("Hotel Service Tests")
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private Connection mockConnection;

    @InjectMocks
    private HotelService hotelService;

    private HotelDTO testHotelDTO;
    private Hotel testHotel;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Priprema test podataka
        testHotelDTO = new HotelDTO(
                1L,
                "Grand Hotel",
                "Luksuzni hotel u centru",
                "+381123456789",
                "info@grandhotel.rs",
                1L
        );

        testHotel = new Hotel(
                1L,
                "Grand Hotel",
                "Luksuzni hotel u centru",
                "+381123456789",
                "info@grandhotel.rs",
                1L
        );
    }

    @Test
    @DisplayName("Trebalo bi uspešno kreirati hotel")
    void testCreateHotelSuccess() throws SQLException {
        // Arrange - Mock DbConfig.getConnection()
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);

            // Act
            HotelDTO result = hotelService.createHotel(testHotelDTO);

            // Assert
            assertNotNull(result);
            assertEquals("Grand Hotel", result.getName());
            assertEquals("info@grandhotel.rs", result.getEmail());
            verify(hotelRepository, times(1)).save(any(Hotel.class), any(Connection.class));
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti hotel po ID-u")
    void testGetHotelByIdSuccess() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);

            when(hotelRepository.findById(eq(1L), any(Connection.class)))
                    .thenReturn(Optional.of(testHotel));

            // Act
            HotelDTO result = hotelService.getHotelById(1L);

            // Assert
            assertNotNull(result);
            assertEquals("Grand Hotel", result.getName());
            verify(hotelRepository, times(1)).findById(eq(1L), any(Connection.class));
        }
    }

    @Test
    @DisplayName("Trebalo bi vratiti null ako hotel ne postoji")
    void testGetHotelByIdNotFound() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);

            when(hotelRepository.findById(eq(999L), any(Connection.class)))
                    .thenReturn(Optional.empty());

            // Act
            HotelDTO result = hotelService.getHotelById(999L);

            // Assert
            assertNull(result);
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sve hotele")
    void testGetAllHotelsSuccess() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);

            List<Hotel> hotels = new ArrayList<>();
            hotels.add(testHotel);
            hotels.add(new Hotel(2L, "Hotel Plaza", "Komforan hotel", "+381987654321", "info@plaza.rs", 2L));

            when(hotelRepository.findAll(any(Connection.class)))
                    .thenReturn(hotels);

            // Act
            List<HotelDTO> result = hotelService.getAllHotels();

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());
            verify(hotelRepository, times(1)).findAll(any(Connection.class));
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati hotel")
    void testUpdateHotelSuccess() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);

            when(hotelRepository.findById(eq(1L), any(Connection.class)))
                    .thenReturn(Optional.of(testHotel));

            HotelDTO updatedDTO = new HotelDTO(1L, "Grand Hotel Updated", "Ažuriran opis", "+381123456789", "info@grandhotel.rs", 1L);

            // Act
            HotelDTO result = hotelService.updateHotel(1L, updatedDTO);

            // Assert
            assertNotNull(result);
            assertEquals("Grand Hotel Updated", result.getName());
            verify(hotelRepository, times(1)).update(any(Hotel.class), any(Connection.class));
        }
    }

    @Test
    @DisplayName("Trebalo bi vratiti null ako hotel za update ne postoji")
    void testUpdateHotelNotFound() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);

            when(hotelRepository.findById(eq(999L), any(Connection.class)))
                    .thenReturn(Optional.empty());

            // Act
            HotelDTO result = hotelService.updateHotel(999L, testHotelDTO);

            // Assert
            assertNull(result);
            verify(hotelRepository, never()).update(any(Hotel.class), any(Connection.class));
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno obrisati hotel")
    void testDeleteHotelSuccess() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);

            when(hotelRepository.findById(eq(1L), any(Connection.class)))
                    .thenReturn(Optional.of(testHotel));

            // Act
            boolean result = hotelService.deleteHotel(1L);

            // Assert
            assertTrue(result);
            verify(hotelRepository, times(1)).delete(eq(1L), any(Connection.class));
        }
    }

    @Test
    @DisplayName("Trebalo bi vratiti false ako hotel za brisanje ne postoji")
    void testDeleteHotelNotFound() throws SQLException {
        try (MockedStatic<DbConfig> mockedDbConfig = mockStatic(DbConfig.class)) {
            mockedDbConfig.when(DbConfig::getConnection).thenReturn(mockConnection);

            when(hotelRepository.findById(eq(999L), any(Connection.class)))
                    .thenReturn(Optional.empty());

            // Act
            boolean result = hotelService.deleteHotel(999L);

            // Assert
            assertFalse(result);
            verify(hotelRepository, never()).delete(any(), any(Connection.class));
        }
    }
}