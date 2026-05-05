package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.User;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorijum zadužen za upravljanje korisničkim nalozima i autentifikacionim podacima.
 * Specifičnost ovog repozitorijuma je dualni upis: sinhronizuje podatke između lokalne
 * {@code NBP_USER} tabele i eksterne {@code NBP.NBP_USER} tabele.
 * * <p>Ovaj pristup omogućava da hotel sistem ima svoju evidenciju korisnika,
 * dok se stvarni identiteti čuvaju u centralizovanoj šemi baze podataka.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Repository
public class UserRepository {

    /**
     * Kreira korisnika u obe tabele. Prvo se vrši upis u eksternu šemu kako bi se
     * dobio globalni {@code USER_ID}, koji se potom koristi kao strani ključ lokalno.
     * * @param user Objekt korisnika sa lozinkom i ulogom.
     * @param conn Aktivna JDBC konekcija sa podrškom za transakcije.
     */
    public void save(User user, Connection conn) throws SQLException {
        // 1. Korak: Kreiranje zapisa u eksternoj NBP šemi
        long generatedNbpId = createInNbpSchema(user, conn);

        // Mapiranje dobijenog eksternog ID-a na lokalni model
        user.setUserId(generatedNbpId);

        // 2. Korak: Kreiranje lokalne kopije zapisa u aplikativnoj šemi
        String sql = """
            INSERT INTO NBP_USER (ID, USER_ID, ROLE_ID, USERNAME, EMAIL, PASSWORD_HASH, ROLE, CREATED_DATE)
            VALUES (NBP_USER_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, CURRENT_DATE)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, user.getUserId());
            ps.setLong(2, user.getRoleId());
            ps.setString(3, user.getUsername());
            ps.setString(4, user.getEmail());
            ps.setString(5, user.getPasswordHash());
            ps.setString(6, user.getRole());

            ps.executeUpdate();

            // Eksplicitna potvrda transakcije ako auto-commit nije aktivan
            if (!conn.getAutoCommit()) {
                conn.commit();
            }
        }
    }

    /**
     * Pomoćna metoda za upis u sistemsku tabelu {@code NBP.NBP_USER}.
     * Koristi {@code getGeneratedKeys} za preuzimanje ID-a koji generiše sekvenca eksterne šeme.
     */
    private long createInNbpSchema(User user, Connection conn) throws SQLException {
        String sql = """
            INSERT INTO NBP.NBP_USER (ID, FIRST_NAME, LAST_NAME, EMAIL, PASSWORD, USERNAME, ROLE_ID)
            VALUES (NBP.NBP_USER_ID_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql, new String[]{"ID"})) {
            ps.setString(1, user.getFirstName());
            ps.setString(2, user.getLastName());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPasswordHash());
            ps.setString(5, user.getUsername());
            ps.setLong(6, user.getRoleId());

            ps.executeUpdate();

            DatabaseLogger.log(conn, "POST", "NBP_USER");

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getLong(1);
                } else {
                    throw new SQLException("Neuspješno preuzimanje generisanog ID-a iz NBP.NBP_USER.");
                }
            }
        }
    }

    /**
     * Vraća listu svih korisnika bez uključivanja hash-a lozinke radi sigurnosti.
     */
    public List<User> findAll(Connection conn) throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM NBP_USER";
        try (PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                users.add(new User(
                        rs.getLong("ID"),
                        rs.getLong("USER_ID"),
                        rs.getLong("ROLE_ID"),
                        rs.getString("USERNAME"),
                        rs.getString("EMAIL"),
                        null, // Sigurnosna mera: ne vraćamo passwordHash
                        rs.getString("ROLE"),
                        rs.getDate("CREATED_DATE") != null ? rs.getDate("CREATED_DATE").toLocalDate() : null,
                        null, null
                ));
            }
        }
        return users;
    }

    /**
     * Pronalazi korisnika na osnovu korisničkog imena.
     * Koristi se primarno u procesu prijave (Login).
     */
    public Optional<User> findByUsername(String username, Connection conn) throws SQLException {
        String sql = "SELECT * FROM NBP_USER WHERE USERNAME = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getLong("ID"),
                            rs.getLong("USER_ID"),
                            rs.getLong("ROLE_ID"),
                            rs.getString("USERNAME"),
                            rs.getString("EMAIL"),
                            rs.getString("PASSWORD_HASH"), // Ovde je hash potreban radi verifikacije
                            rs.getString("ROLE"),
                            rs.getDate("CREATED_DATE") != null ? rs.getDate("CREATED_DATE").toLocalDate() : null,
                            null, null
                    ));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Pronalazi korisnika putem internog ID-a.
     */
    public Optional<User> findById(Long id, Connection conn) throws SQLException {
        String sql = "SELECT * FROM NBP_USER WHERE ID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getLong("ID"),
                            rs.getLong("USER_ID"),
                            rs.getLong("ROLE_ID"),
                            rs.getString("USERNAME"),
                            rs.getString("EMAIL"),
                            null,
                            rs.getString("ROLE"),
                            rs.getDate("CREATED_DATE") != null ? rs.getDate("CREATED_DATE").toLocalDate() : null,
                            null, null
                    ));
                }
            }
        }
        return Optional.empty();
    }
}