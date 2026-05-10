package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.UserDTO;
import com.example.hotel_management_system.dto.UserRegistrationDTO;
import com.example.hotel_management_system.model.User;
import com.example.hotel_management_system.repository.JwtTokenRepository;
import com.example.hotel_management_system.repository.UserRepository;
import com.example.hotel_management_system.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final JwtTokenRepository jwtTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public UserService(UserRepository userRepository,
                       JwtTokenRepository jwtTokenRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtTokenRepository = jwtTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public void register(UserRegistrationDTO dto) throws SQLException {
        User user = new User();
        user.setRoleId(dto.getRoleId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        user.setPasswordHash(passwordEncoder.encode(dto.getPassword())); // hesiranje lozinke

        try (Connection conn = DbConfig.getConnection()) {
            userRepository.save(user, conn);
        }
    }

    public String login(String username, String password) throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            Optional<User> userOpt = userRepository.findByUsername(username, conn);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                if (passwordEncoder.matches(password, user.getPasswordHash())) {
                    String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
                    jwtTokenRepository.save(
                            token,
                            user.getUsername(),
                            user.getRole(),
                            jwtUtil.extractIssuedAt(token),
                            jwtUtil.extractExpiration(token),
                            conn
                    );
                    return token;
                }
            }
            throw new RuntimeException("Pogrešno korisničko ime ili lozinka!");
        }
    }

    public void logout(String token) throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            jwtTokenRepository.deleteByToken(token, conn);
        }
    }

    public Optional<UserDTO> getByUsername(String username) throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            return userRepository.findByUsername(username, conn)
                    .map(this::convertToDTO);
        }
    }

    public List<UserDTO> getAllUsers() throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            return userRepository.findAll(conn).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
    }

    private UserDTO convertToDTO(User user) {
        if (user == null) return null;
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedDate()
        );
    }
}
