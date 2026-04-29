package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.ExtraService;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@DisplayName("Extra Service Repository Tests")
class ExtraServiceRepositoryTest {

    private ExtraServiceRepository extraServiceRepository;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        extraServiceRepository = new ExtraServiceRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    @DisplayName("Trebalo bi uspešno sačuvati dodatnu uslugu")
    void testSaveExtraService() throws SQLException {
        // Arrange
        ExtraService service = new ExtraService(1L, "Spa", "Full body massage", 50.0, "Y");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> extraServiceRepository.save(service, mockConnection));

            // Provjera redoslijeda parametara prema INSERT_QUERY
            verify(mockPreparedStatement).setLong(1, 1L);
            verify(mockPreparedStatement).setString(2, "Spa");
            verify(mockPreparedStatement).setString(3, "Full body massage");
            verify(mockPreparedStatement).setDouble(4, 50.0);
            verify(mockPreparedStatement).setString(5, "Y");
            verify(mockPreparedStatement).executeUpdate();

            mockedLogger.verify(() -> DatabaseLogger.log(mockConnection, "POST", "NBP_SERVICE"));
        }
    }

    @Test
    @DisplayName("Trebalo bi pronaći uslugu po ID-u")
    void testFindById() throws SQLException {
        // Arrange
        Long serviceId = 1L;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);

        // Simulacija mapiranja ResultSet-a
        when(mockResultSet.getLong("ID")).thenReturn(serviceId);
        when(mockResultSet.getString("NAME")).thenReturn("Spa");
        when(mockResultSet.getString("DESCRIPTION")).thenReturn("Massage");
        when(mockResultSet.getDouble("UNIT_PRICE")).thenReturn(50.0);
        when(mockResultSet.getString("AVAILABLE")).thenReturn("Y");

        // Act
        Optional<ExtraService> result = extraServiceRepository.findById(serviceId, mockConnection);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Spa", result.get().getName());
        verify(mockPreparedStatement).setLong(1, serviceId);
    }

    @Test
    @DisplayName("Trebalo bi vratiti empty Optional ako usluga ne postoji")
    void testFindByIdNotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Optional<ExtraService> result = extraServiceRepository.findById(999L, mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sve usluge")
    void testFindAll() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false); // Dva rezultata

        when(mockResultSet.getLong("ID")).thenReturn(1L, 2L);
        when(mockResultSet.getString("NAME")).thenReturn("Spa", "Gym");

        // Act
        List<ExtraService> result = extraServiceRepository.findAll(mockConnection);

        // Assert
        assertEquals(2, result.size());
        assertEquals("Spa", result.get(0).getName());
        assertEquals("Gym", result.get(1).getName());
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati uslugu")
    void testUpdateExtraService() throws SQLException {
        // Arrange
        ExtraService service = new ExtraService(1L, "Spa Updated", "Better massage", 60.0, "Y");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act
            extraServiceRepository.update(service, mockConnection);

            // Provjera redoslijeda parametara prema UPDATE_QUERY
            verify(mockPreparedStatement).setString(1, "Spa Updated");
            verify(mockPreparedStatement).setString(2, "Better massage");
            verify(mockPreparedStatement).setDouble(3, 60.0);
            verify(mockPreparedStatement).setString(4, "Y");
            verify(mockPreparedStatement).setLong(5, 1L); // ID ide na kraju u WHERE klauzuli

            mockedLogger.verify(() -> DatabaseLogger.log(mockConnection, "PUT", "NBP_SERVICE"));
        }
    }

    @Test
    @DisplayName("Trebalo bi obrisati uslugu")
    void testDeleteExtraService() throws SQLException {
        // Arrange
        Long serviceId = 1L;
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act
            extraServiceRepository.delete(serviceId, mockConnection);

            // Assert
            verify(mockPreparedStatement).setLong(1, serviceId);
            verify(mockPreparedStatement).executeUpdate();
            mockedLogger.verify(() -> DatabaseLogger.log(mockConnection, "DELETE", "NBP_SERVICE"));
        }
    }

    @Test
    @DisplayName("Trebalo bi baciti SQLException pri grešci u bazi")
    void testSaveWithException() throws SQLException {
        // Arrange
        ExtraService service = new ExtraService(1L, "Fail", "Desc", 10.0, "Y");
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB Error"));

        // Act & Assert
        assertThrows(SQLException.class, () -> extraServiceRepository.save(service, mockConnection));
    }
}