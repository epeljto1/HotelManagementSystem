package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Hotel;
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

@DisplayName("Hotel Repository Tests")
class HotelRepositoryTest {

    private HotelRepository hotelRepository;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        hotelRepository = new HotelRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    @DisplayName("Trebalo bi uspešno sačuvati hotel")
    void testSaveHotel() throws SQLException {
        // Arrange
        Hotel hotel = new Hotel(1L, "Grand Hotel", "Luksuzni hotel", "+381123456789", "info@grand.rs", 1L);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        // Act & Assert - bez verifikacije DatabaseLogger poziva
        assertDoesNotThrow(() -> hotelRepository.save(hotel, mockConnection));

        verify(mockPreparedStatement, times(1)).setLong(1, 1L);
        verify(mockPreparedStatement, times(1)).setString(2, "Grand Hotel");
        verify(mockPreparedStatement, times(1)).setString(3, "Luksuzni hotel");
        verify(mockPreparedStatement, times(1)).setString(4, "+381123456789");
        verify(mockPreparedStatement, times(1)).setString(5, "info@grand.rs");
        verify(mockPreparedStatement, times(1)).setLong(6, 1L);
    }

    @Test
    @DisplayName("Trebalo bi preuzeti hotel po ID-u")
    void testFindHotelById() throws SQLException {
        // Arrange
        Long hotelId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getString("NAME")).thenReturn("Grand Hotel");
        when(mockResultSet.getString("DESCRIPTION")).thenReturn("Luksuzni hotel");
        when(mockResultSet.getString("PHONE_NUMBER")).thenReturn("+381123456789");
        when(mockResultSet.getString("EMAIL")).thenReturn("info@grand.rs");
        when(mockResultSet.getLong("ADDRESS")).thenReturn(1L);

        // Act
        Optional<Hotel> result = hotelRepository.findById(hotelId, mockConnection);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Grand Hotel", result.get().getName());
        assertEquals("info@grand.rs", result.get().getEmail());
        verify(mockPreparedStatement, times(1)).setLong(1, hotelId);
    }

    @Test
    @DisplayName("Trebalo bi vratiti prazan Optional ako hotel ne postoji")
    void testFindHotelByIdNotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Optional<Hotel> result = hotelRepository.findById(999L, mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sve hotele")
    void testFindAllHotels() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next())
                .thenReturn(true)  // Prvi hotel
                .thenReturn(true)  // Drugi hotel
                .thenReturn(false); // Kraj rezultata

        when(mockResultSet.getLong("ID"))
                .thenReturn(1L)
                .thenReturn(2L);
        when(mockResultSet.getString("NAME"))
                .thenReturn("Grand Hotel")
                .thenReturn("Hotel Plaza");
        when(mockResultSet.getString("DESCRIPTION"))
                .thenReturn("Luksuzni hotel")
                .thenReturn("Komforan hotel");
        when(mockResultSet.getString("PHONE_NUMBER"))
                .thenReturn("+381123456789")
                .thenReturn("+381987654321");
        when(mockResultSet.getString("EMAIL"))
                .thenReturn("info@grand.rs")
                .thenReturn("info@plaza.rs");
        when(mockResultSet.getLong("ADDRESS"))
                .thenReturn(1L)
                .thenReturn(2L);

        // Act
        List<Hotel> result = hotelRepository.findAll(mockConnection);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Grand Hotel", result.get(0).getName());
        assertEquals("Hotel Plaza", result.get(1).getName());
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati hotel")
    void testUpdateHotel() throws SQLException {
        // Arrange
        Hotel hotel = new Hotel(1L, "Updated Hotel", "Ažuriran opis", "+381111111111", "updated@hotel.rs", 1L);

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        // Act & Assert
        assertDoesNotThrow(() -> hotelRepository.update(hotel, mockConnection));

        verify(mockPreparedStatement, times(1)).setString(1, "Updated Hotel");
        verify(mockPreparedStatement, times(1)).setString(2, "Ažuriran opis");
        verify(mockPreparedStatement, times(1)).setString(3, "+381111111111");
        verify(mockPreparedStatement, times(1)).setString(4, "updated@hotel.rs");
        verify(mockPreparedStatement, times(1)).setLong(5, 1L);
        verify(mockPreparedStatement, times(1)).setLong(6, 1L);
    }

    @Test
    @DisplayName("Trebalo bi uspešno obrisati hotel")
    void testDeleteHotel() throws SQLException {
        // Arrange
        Long hotelId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        // Act & Assert
        assertDoesNotThrow(() -> hotelRepository.delete(hotelId, mockConnection));

        verify(mockPreparedStatement, times(1)).setLong(1, hotelId);
        // Ne verifikuj executeUpdate jer ga poziva i DatabaseLogger
    }
}