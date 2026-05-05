package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.ExtraService;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorij zadužen za perzistenciju i upravljanje podacima o dodatnim uslugama hotela.
 * Komunicira direktno sa tabelom {@code NBP_SERVICE} koristeći JDBC protokol.
 * * <p>Ovaj repozitorij služi kao definicioni katalog usluga, gdje se čuvaju nazivi,
 * opisi i jedinične cijene koje kasnije koristi {@code ServiceUsageRepository}.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Repository
public class ExtraServiceRepository {

    /** SQL upit za unos nove usluge. Zahtijeva manuelno prolijeđen ID. */
    private final String INSERT_QUERY = """
            INSERT INTO NBP_SERVICE (ID, NAME, DESCRIPTION, UNIT_PRICE, AVAILABLE)
            VALUES (?, ?, ?, ?, ?)
            """;

    /** SQL upit za dobavljanje svih definisanih usluga, sortiranih po ID-u. */
    private final String SELECT_ALL_QUERY = """
            SELECT ID, NAME, DESCRIPTION, UNIT_PRICE, AVAILABLE
            FROM NBP_SERVICE
            ORDER BY ID
            """;

    /** SQL upit za dobavljanje specifične usluge putem primarnog ključa. */
    private final String SELECT_BY_ID_QUERY = """
            SELECT ID, NAME, DESCRIPTION, UNIT_PRICE, AVAILABLE
            FROM NBP_SERVICE
            WHERE ID = ?
            """;

    /** SQL upit za ažuriranje postojećih informacija o usluzi ili cijeni. */
    private final String UPDATE_QUERY = """
            UPDATE NBP_SERVICE
            SET NAME = ?, DESCRIPTION = ?, UNIT_PRICE = ?, AVAILABLE = ?
            WHERE ID = ?
            """;

    /** SQL upit za trajno brisanje usluge iz kataloga. */
    private final String DELETE_QUERY = """
            DELETE FROM NBP_SERVICE
            WHERE ID = ?
            """;

    /**
     * Spašava novu uslugu u bazu podataka.
     * * @param extraService Objekt koji sadrži definiciju usluge.
     * @param connection Aktivna JDBC konekcija.
     * @throws SQLException U slučaju narušavanja integriteta (npr. dupli ID).
     */
    public void save(ExtraService extraService, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, extraService.getId());
            ps.setString(2, extraService.getName());
            ps.setString(3, extraService.getDescription());
            ps.setDouble(4, extraService.getUnitPrice());
            ps.setString(5, extraService.getAvailable());
            ps.executeUpdate();

            // Evidentiranje operacije u sistemski log
            DatabaseLogger.log(connection, "POST", "NBP_SERVICE");
        }
    }

    /**
     * Pronalazi uslugu na osnovu njenog ID-a.
     * * @param id Jedinstveni identifikator usluge.
     * @param connection Aktivna JDBC konekcija.
     * @return Optional objekt koji sadrži uslugu ako postoji.
     */
    public Optional<ExtraService> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToExtraService(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Vraća listu svih usluga definisanih u sistemu.
     * * @param connection Aktivna JDBC konekcija.
     * @return List<ExtraService> Lista svih zapisa.
     */
    public List<ExtraService> findAll(Connection connection) throws SQLException {
        List<ExtraService> services = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                services.add(mapResultSetToExtraService(rs));
            }
        }
        return services;
    }

    /**
     * Ažurira podatke o usluzi (npr. promjena cijene doručka).
     * * @param extraService Objekt sa novim podacima.
     * @param connection Aktivna JDBC konekcija.
     */
    public void update(ExtraService extraService, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, extraService.getName());
            ps.setString(2, extraService.getDescription());
            ps.setDouble(3, extraService.getUnitPrice());
            ps.setString(4, extraService.getAvailable());
            ps.setLong(5, extraService.getId());
            ps.executeUpdate();

            DatabaseLogger.log(connection, "PUT", "NBP_SERVICE");
        }
    }

    /**
     * Uklanja uslugu iz baze podataka.
     * <p>Napomena: Brisanje može biti onemogućeno ukoliko postoje zapisi o korištenju
     * ove usluge u tabeli {@code NBP_SERVICE_USAGE} (Foreign Key constraint).</p>
     * * @param id ID usluge za brisanje.
     * @param connection Aktivna JDBC konekcija.
     */
    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            DatabaseLogger.log(connection, "DELETE", "NBP_SERVICE");
        }
    }

    /**
     * Pomoćna metoda za mapiranje redova ResultSet-a u objektni model {@link ExtraService}.
     */
    private ExtraService mapResultSetToExtraService(ResultSet rs) throws SQLException {
        return new ExtraService(
                rs.getLong("ID"),
                rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getDouble("UNIT_PRICE"),
                rs.getString("AVAILABLE")
        );
    }
}