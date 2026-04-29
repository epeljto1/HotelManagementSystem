package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.ServiceUsage;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Service Usage Repository Tests")
class ServiceUsageRepositoryTest {

    private ServiceUsageRepository serviceUsageRepository;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        serviceUsageRepository = new ServiceUsageRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    @DisplayName("Trebalo bi uspešno sačuvati korišćenje servisa")
    void testSaveServiceUsage() throws SQLException {
        // Arrange
        LocalDate now = LocalDate.now();
        ServiceUsage serviceUsage = new ServiceUsage(1L, 1L, 1L, 2, now, new BigDecimal("50.00"));

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> serviceUsageRepository.save(serviceUsage, mockConnection));

            // Provjera svih 6 parametara prema redoslijedu u ServiceUsageRepository.save()
            verify(mockPreparedStatement, times(1)).setLong(1, 1L);      // ID
            verify(mockPreparedStatement, times(1)).setLong(2, 1L);      // STAY_ID
            verify(mockPreparedStatement, times(1)).setLong(3, 1L);      // SERVICE_ID
            verify(mockPreparedStatement, times(1)).setInt(4, 2);        // QUANTITY
            verify(mockPreparedStatement, times(1)).setDate(5, Date.valueOf(now)); // USAGE_DATE
            verify(mockPreparedStatement, times(1)).setBigDecimal(6, new BigDecimal("50.00")); // TOTAL_PRICE

            verify(mockPreparedStatement, times(1)).executeUpdate();
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti korišćenje servisa po ID-u")
    void testFindServiceUsageById() throws SQLException {
        // Arrange
        Long serviceUsageId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getLong("STAY_ID")).thenReturn(1L);
        when(mockResultSet.getLong("SERVICE_ID")).thenReturn(1L);
        when(mockResultSet.getInt("QUANTITY")).thenReturn(2);
        when(mockResultSet.getDate("USAGE_DATE")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getBigDecimal("TOTAL_PRICE")).thenReturn(new BigDecimal("50.00"));

        // Act
        ServiceUsage result = serviceUsageRepository.findById(serviceUsageId, mockConnection);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("50.00"), result.getTotalPrice());
        assertEquals(Integer.valueOf(2), result.getQuantity());
    }

    @Test
    @DisplayName("Trebalo bi vratiti null ako korišćenje servisa ne postoji")
    void testFindServiceUsageByIdNotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        ServiceUsage result = serviceUsageRepository.findById(999L, mockConnection);

        // Assert
        assertNull(result);
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sva korišćenja servisa")
    void testFindAllServiceUsages() throws SQLException {
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
        when(mockResultSet.getLong("STAY_ID"))
                .thenReturn(1L)
                .thenReturn(1L);
        when(mockResultSet.getLong("SERVICE_ID"))
                .thenReturn(1L)
                .thenReturn(2L);
        when(mockResultSet.getInt("QUANTITY"))
                .thenReturn(2)
                .thenReturn(3);
        when(mockResultSet.getDate("USAGE_DATE"))
                .thenReturn(Date.valueOf(LocalDate.now()))
                .thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getBigDecimal("TOTAL_PRICE"))
                .thenReturn(new BigDecimal("50.00"))
                .thenReturn(new BigDecimal("75.00"));

        // Act
        List<ServiceUsage> result = serviceUsageRepository.findAll(mockConnection);

        // Assert
        assertEquals(2, result.size());
        assertEquals(new BigDecimal("50.00"), result.get(0).getTotalPrice());
        assertEquals(new BigDecimal("75.00"), result.get(1).getTotalPrice());
    }

    @Test
    @DisplayName("Trebalo bi preuzeti korišćenja servisa po ID-u boravka")
    void testFindServiceUsagesByStayId() throws SQLException {
        // Arrange
        Long stayId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next())
                .thenReturn(true)
                .thenReturn(false);

        when(mockResultSet.getLong("ID")).thenReturn(1L);
        when(mockResultSet.getLong("STAY_ID")).thenReturn(stayId);
        when(mockResultSet.getLong("SERVICE_ID")).thenReturn(1L);
        when(mockResultSet.getInt("QUANTITY")).thenReturn(2);
        when(mockResultSet.getDate("USAGE_DATE")).thenReturn(Date.valueOf(LocalDate.now()));
        when(mockResultSet.getBigDecimal("TOTAL_PRICE")).thenReturn(new BigDecimal("50.00"));

        // Act
        List<ServiceUsage> result = serviceUsageRepository.findByStayId(stayId, mockConnection);

        // Assert
        assertEquals(1, result.size());
        assertEquals(stayId, result.get(0).getStayId());
    }

    @Test
    @DisplayName("Trebalo bi uspešno ažurirati korišćenje servisa")
    void testUpdateServiceUsage() throws SQLException {
        // Arrange
        Long serviceUsageId = 1L;
        ServiceUsage serviceUsage = new ServiceUsage(serviceUsageId, 1L, 1L, 3, LocalDate.now(), new BigDecimal("75.00"));

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> serviceUsageRepository.update(serviceUsageId, serviceUsage, mockConnection));

            // Redoslijed parametara prema kodu:
            // 1. STAY_ID, 2. SERVICE_ID, 3. QUANTITY, 4. USAGE_DATE, 5. TOTAL_PRICE, 6. ID
            verify(mockPreparedStatement, times(1)).setLong(1, 1L);      // stayId
            verify(mockPreparedStatement, times(1)).setLong(2, 1L);      // serviceId
            verify(mockPreparedStatement, times(1)).setInt(3, 3);        // quantity
            verify(mockPreparedStatement, times(1)).setDate(4, Date.valueOf(LocalDate.now()));  // usageDate
            verify(mockPreparedStatement, times(1)).setBigDecimal(5, new BigDecimal("75.00")); // totalPrice
            verify(mockPreparedStatement, times(1)).setLong(6, serviceUsageId); // id
        }
    }

    @Test
    @DisplayName("Trebalo bi uspešno obrisati korišćenje servisa")
    void testDeleteServiceUsage() throws SQLException {
        // Arrange
        Long serviceUsageId = 1L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act & Assert
            assertDoesNotThrow(() -> serviceUsageRepository.delete(serviceUsageId, mockConnection));

            verify(mockPreparedStatement, times(1)).setLong(1, serviceUsageId);
        }
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sva korišćenja servisa kada je lista prazna")
    void testFindAllServiceUsagesEmpty() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<ServiceUsage> result = serviceUsageRepository.findAll(mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi baciti SQLException pri greški")
    void testSaveServiceUsageWithException() throws SQLException {
        // Arrange
        ServiceUsage serviceUsage = new ServiceUsage(1L, 1L, 1L, 2, LocalDate.now(), new BigDecimal("50.00"));

        when(mockConnection.prepareStatement(anyString()))
                .thenThrow(new SQLException("Database connection failed"));

        // Act & Assert
        assertThrows(SQLException.class, () -> serviceUsageRepository.save(serviceUsage, mockConnection));
    }

    @Test
    @DisplayName("Trebalo bi preuzeti prazan rezultat korišćenja servisa za boravak")
    void testFindServiceUsagesByStayIdNotFound() throws SQLException {
        // Arrange
        Long stayId = 999L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        List<ServiceUsage> result = serviceUsageRepository.findByStayId(stayId, mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi uspešno preuzeti korišćenje servisa sa različitim vrednostima")
    void testFindServiceUsageByIdWithVariousValues() throws SQLException {
        // Arrange
        Long serviceUsageId = 5L;

        when(mockConnection.prepareStatement(anyString()))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery())
                .thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong("ID")).thenReturn(5L);
        when(mockResultSet.getLong("STAY_ID")).thenReturn(3L);
        when(mockResultSet.getLong("SERVICE_ID")).thenReturn(2L);
        when(mockResultSet.getInt("QUANTITY")).thenReturn(5);
        when(mockResultSet.getDate("USAGE_DATE")).thenReturn(Date.valueOf(LocalDate.of(2026, 4, 29)));
        when(mockResultSet.getBigDecimal("TOTAL_PRICE")).thenReturn(new BigDecimal("150.50"));

        // Act
        ServiceUsage result = serviceUsageRepository.findById(serviceUsageId, mockConnection);

        // Assert
        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals(3L, result.getStayId());
        assertEquals(2L, result.getServiceId());
        assertEquals(Integer.valueOf(5), result.getQuantity());
        assertEquals(new BigDecimal("150.50"), result.getTotalPrice());
    }
}