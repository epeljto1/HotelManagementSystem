package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.UserDTO;
import com.example.hotel_management_system.dto.UserRegistrationDTO;
import com.example.hotel_management_system.model.User;
import com.example.hotel_management_system.repository.UserRepository;
import com.example.hotel_management_system.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servisni sloj zadužen za sigurnost sistema i upravljanje korisničkim nalozima.
 * Implementira logiku registracije, sigurnosnog hesiranja lozinki i izdavanje
 * JWT tokena prilikom prijave.
 * * <p>Sigurnosne mjere uključuju:
 * <ul>
 * <li>BCrypt algoritam za zaštitu lozinki (ne čuvaju se u čistom tekstu).</li>
 * <li>JWT (Stateless) autentifikaciju za siguran rad sa API-jem.</li>
 * </ul>
 * </p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    /**
     * Konstruktor sa ubrizgavanjem zavisnosti za repozitorij i sigurnosne komponente.
     */
    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Registruje novog korisnika u sistemu.
     * Lozinka se automatski hesira pomoću BCrypt algoritma prije upisa u bazu.
     * * @param dto Podaci za registraciju (username, email, role, password).
     * @throws SQLException Ako dođe do greške pri upisu u bazu podataka.
     */
    public void register(UserRegistrationDTO dto) throws SQLException {
        User user = new User();
        user.setRoleId(dto.getRoleId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());

        // Sigurnosno hesiranje lozinke
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));

        try (Connection conn = DbConfig.getConnection()) {
            userRepository.save(user, conn);
        }
    }

    /**
     * Autentifikuje korisnika na osnovu korisničkog imena i lozinke.
     * <p>Ukoliko su podaci ispravni, generiše se JWT token koji klijent koristi
     * za autorizaciju budućih zahtjeva.</p>
     * * @param username Korisničko ime.
     * @param password Lozinka u čistom tekstu (validira se protiv hesirane verzije).
     * @return String JWT token spreman za klijentski Authorization header.
     * @throws RuntimeException Ako su kredencijali neispravni.
     * @throws SQLException U slučaju SQL greške.
     */
    public String login(String username, String password) throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            Optional<User> userOpt = userRepository.findByUsername(username, conn);

            if (userOpt.isPresent()) {
                User user = userOpt.get();

                // Upoređivanje ulazne lozinke sa hesiranim zapisom iz baze
                if (passwordEncoder.matches(password, user.getPasswordHash())) {
                    return jwtUtil.generateToken(user.getUsername(), user.getRole());
                }
            }
            throw new RuntimeException("Pogrešno korisničko ime ili lozinka!");
        }
    }

    /**
     * Dobavlja osnovne informacije o korisniku na osnovu korisničkog imena.
     * @param username Korisničko ime.
     * @return Optional sa UserDTO objektom bez osjetljivih podataka (lozinke).
     */
    public Optional<UserDTO> getByUsername(String username) throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            return userRepository.findByUsername(username, conn)
                    .map(this::convertToDTO);
        }
    }

    /**
     * Vraća listu svih korisnika sistema.
     * @return List<UserDTO>
     */
    public List<UserDTO> getAllUsers() throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            return userRepository.findAll(conn).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Pomoćna metoda za konverziju modela User u UserDTO.
     * Osigurava da se osjetljivi podaci poput lozinki nikada ne šalju ka klijentu.
     */
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