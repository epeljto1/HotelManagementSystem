package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.User;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("User Repository Tests")
class UserRepositoryTest {

    private UserRepository userRepository;
    private Connection mockConnection;
    private PreparedStatement mockPreparedStatement;
    private ResultSet mockResultSet;

    @BeforeEach
    void setUp() throws SQLException {
        userRepository = new UserRepository();
        mockConnection = mock(Connection.class);
        mockPreparedStatement = mock(PreparedStatement.class);
        mockResultSet = mock(ResultSet.class);
    }

    @Test
    @DisplayName("Trebalo bi uspješno sačuvati korisnika u obje šeme")
    void testSaveUser() throws SQLException {
        // Arrange
        User user = new User(null, null, 1L, "jdoe", "john@example.com", "hash", "ADMIN", null, "John", "Doe");

        // Mocking za createInNbpSchema (prvi INSERT)
        when(mockConnection.prepareStatement(contains("INSERT INTO NBP.NBP_USER"), any(String[].class)))
                .thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeUpdate()).thenReturn(1);
        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getLong(1)).thenReturn(100L); // Generisani ID

        // Mocking za lokalni save (drugi INSERT)
        PreparedStatement mockLocalPs = mock(PreparedStatement.class);
        when(mockConnection.prepareStatement(contains("INSERT INTO NBP_USER")))
                .thenReturn(mockLocalPs);
        when(mockConnection.getAutoCommit()).thenReturn(false);

        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            // Act
            assertDoesNotThrow(() -> userRepository.save(user, mockConnection));

            // Assert - Provjera prvog inserta (NBP šema)
            verify(mockPreparedStatement).setString(1, "John");
            verify(mockPreparedStatement).setString(2, "Doe");
            verify(mockPreparedStatement).setString(3, "john@example.com");
            verify(mockPreparedStatement).setString(5, "jdoe");

            // Assert - Provjera drugog inserta (Lokalna tabela)
            verify(mockLocalPs).setLong(1, 100L); // Provjera da li je proslijeđen generisani ID
            verify(mockLocalPs).setString(3, "jdoe");
            verify(mockLocalPs).setString(6, "ADMIN");

            verify(mockConnection).commit();
            mockedLogger.verify(() -> DatabaseLogger.log(eq(mockConnection), eq("POST"), eq("NBP_USER")));
        }
    }

    @Test
    @DisplayName("Trebalo bi baciti SQLException ako preuzimanje generisanog ID-a ne uspije")
    void testSaveUserFailsIdRetrieval() throws SQLException {
        // Arrange
        User user = new User(null, null, 1L, "test", "test@test.com", "hash", "USER", null, "A", "B");

        // Obezbjeđujemo da bilo koji poziv prepareStatement vrati mockPreparedStatement
        // kako DatabaseLogger ne bi dobio null
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockConnection.prepareStatement(anyString(), any(String[].class))).thenReturn(mockPreparedStatement);

        when(mockPreparedStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false); // Simuliramo neuspjeh preuzimanja ključa

        // Act & Assert
        // Koristimo try-with-resources za statički mock logera kako bismo izbjegli neželjene nuspojave
        try (MockedStatic<DatabaseLogger> mockedLogger = mockStatic(DatabaseLogger.class)) {
            SQLException ex = assertThrows(SQLException.class, () -> userRepository.save(user, mockConnection));
            assertTrue(ex.getMessage().contains("Neuspješno preuzimanje generisanog ID-a"));
        }
    }

    @Test
    @DisplayName("Trebalo bi pronaći korisnika po korisničkom imenu")
    void testFindByUsername() throws SQLException {
        // Arrange
        String username = "adminUser";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getString("USERNAME")).thenReturn(username);
        when(mockResultSet.getString("EMAIL")).thenReturn("admin@hotel.com");
        when(mockResultSet.getString("ROLE")).thenReturn("ADMIN");

        // Act
        Optional<User> result = userRepository.findByUsername(username, mockConnection);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(username, result.get().getUsername());
        verify(mockPreparedStatement).setString(1, username);
    }

    @Test
    @DisplayName("Trebalo bi vratiti empty Optional ako korisnik ne postoji")
    void testFindByUsernameNotFound() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        // Act
        Optional<User> result = userRepository.findByUsername("unknown", mockConnection);

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Trebalo bi preuzeti sve korisnike bez lozinki")
    void testFindAll() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, true, false);

        when(mockResultSet.getString("USERNAME")).thenReturn("user1", "user2");
        when(mockResultSet.getString("EMAIL")).thenReturn("u1@test.com", "u2@test.com");
        when(mockResultSet.getString("ROLE")).thenReturn("GUEST");

        // Act
        List<User> result = userRepository.findAll(mockConnection);

        // Assert
        assertEquals(2, result.size());
        assertNull(result.get(0).getPasswordHash()); // Provjera sigurnosnog zahtjeva iz koda
        assertEquals("user1", result.get(0).getUsername());
    }

    @Test
    @DisplayName("Trebalo bi ispravno rukovati null datumom kreiranja")
    void testFindAllWithNullDate() throws SQLException {
        // Arrange
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockPreparedStatement);
        when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getDate("CREATED_DATE")).thenReturn(null);

        // Act
        List<User> result = userRepository.findAll(mockConnection);

        // Assert
        assertNotNull(result);
        assertNull(result.get(0).getCreatedDate());
    }
}