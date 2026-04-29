package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.UserDTO;
import com.example.hotel_management_system.dto.UserRegistrationDTO;
import com.example.hotel_management_system.model.User;
import com.example.hotel_management_system.repository.UserRepository;
import com.example.hotel_management_system.util.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private Connection connection;

    @InjectMocks
    private UserService userService;

    @Test
    void registerEncodesPasswordAndPersistsMappedUser() throws Exception {
        UserRegistrationDTO dto = new UserRegistrationDTO();
        dto.setRoleId(3L);
        dto.setUsername("amina");
        dto.setEmail("amina@example.com");
        dto.setRole("RECEPTIONIST");
        dto.setFirstName("Amina");
        dto.setLastName("Hadzic");
        dto.setPassword("secret");

        when(passwordEncoder.encode("secret")).thenReturn("hashed-secret");

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            userService.register(dto);
        }

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture(), eq(connection));

        User savedUser = userCaptor.getValue();
        assertEquals(3L, savedUser.getRoleId());
        assertEquals("amina", savedUser.getUsername());
        assertEquals("amina@example.com", savedUser.getEmail());
        assertEquals("RECEPTIONIST", savedUser.getRole());
        assertEquals("Amina", savedUser.getFirstName());
        assertEquals("Hadzic", savedUser.getLastName());
        assertEquals("hashed-secret", savedUser.getPasswordHash());
    }

    @Test
    void loginReturnsJwtWhenCredentialsMatch() throws Exception {
        User user = new User(1L, 10L, 2L, "amina", "amina@example.com", "hashed-secret",
                "ADMIN", LocalDate.now(), null, null);

        when(userRepository.findByUsername("amina", connection)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hashed-secret")).thenReturn(true);
        when(jwtUtil.generateToken("amina", "ADMIN")).thenReturn("jwt-token");

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            assertEquals("jwt-token", userService.login("amina", "secret"));
        }
    }

    @Test
    void loginThrowsWhenPasswordDoesNotMatch() throws Exception {
        User user = new User(1L, 10L, 2L, "amina", "amina@example.com", "hashed-secret",
                "ADMIN", LocalDate.now(), null, null);

        when(userRepository.findByUsername("amina", connection)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "hashed-secret")).thenReturn(false);

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> userService.login("amina", "wrong-password"));
            assertTrue(exception.getMessage().contains("korisni"));
        }

        verify(jwtUtil, never()).generateToken("amina", "ADMIN");
    }

    @Test
    void getAllUsersMapsRepositoryEntitiesToDtos() throws Exception {
        User firstUser = new User(1L, 11L, 1L, "admin", "admin@example.com", null,
                "ADMIN", LocalDate.of(2026, 4, 20), null, null);
        User secondUser = new User(2L, 12L, 2L, "reception", "reception@example.com", null,
                "RECEPTIONIST", LocalDate.of(2026, 4, 21), null, null);

        when(userRepository.findAll(connection)).thenReturn(List.of(firstUser, secondUser));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            List<UserDTO> result = userService.getAllUsers();
            assertEquals(2, result.size());
            assertUserSummary(result.get(0), "admin", "ADMIN");
            assertUserSummary(result.get(1), "reception", "RECEPTIONIST");
        }
    }

    private static void assertUserSummary(UserDTO user, String username, String role) {
        assertEquals(username, user.getUsername());
        assertEquals(role, user.getRole());
    }
}
