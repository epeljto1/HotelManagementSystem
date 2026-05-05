package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Guest;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorij zadužen za upravljanje podacima o gostima hotela.
 * Komunicira direktno sa tabelom {@code NBP_GUEST} u bazi podataka.
 * * <p>Ova klasa omogućava čuvanje matičnih podataka o fizičkim licima,
 * uključujući referencu na njihovu adresu ({@code ADDRESS_ID}) i identifikacione
 * dokumente, što je neophodno za proces prijave i zakonsku evidenciju.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Repository
public class GuestRepository {

    /** SQL upit za unos novog gosta. ID se proslijeđuje manuelno. */
    private final String INSERT_QUERY = """
            INSERT INTO NBP_GUEST (ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, DATE_OF_BIRTH, DOCUMENT_NUMBER, ADDRESS_ID)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """;

    /** SQL upit za dobavljanje svih gostiju sortiranih prema ID-u. */
    private final String SELECT_ALL_QUERY = """
            SELECT ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, DATE_OF_BIRTH, DOCUMENT_NUMBER, ADDRESS_ID
            FROM NBP_GUEST
            ORDER BY ID
            """;

    /** SQL upit za pretragu gosta putem primarnog ključa. */
    private final String SELECT_BY_ID_QUERY = """
            SELECT ID, FIRST_NAME, LAST_NAME, EMAIL, PHONE_NUMBER, DATE_OF_BIRTH, DOCUMENT_NUMBER, ADDRESS_ID
            FROM NBP_GUEST
            WHERE ID = ?
            """;

    /** SQL upit za ažuriranje ličnih i kontakt podataka gosta. */
    private final String UPDATE_QUERY = """
            UPDATE NBP_GUEST
            SET FIRST_NAME = ?, LAST_NAME = ?, EMAIL = ?, PHONE_NUMBER = ?, DATE_OF_BIRTH = ?, DOCUMENT_NUMBER = ?, ADDRESS_ID = ?
            WHERE ID = ?
            """;

    /** SQL upit za trajno uklanjanje gosta iz evidencije. */
    private final String DELETE_QUERY = """
            DELETE FROM NBP_GUEST
            WHERE ID = ?
            """;

    /**
     * Trajno pohranjuje podatke o novom gostu u bazu podataka.
     * * @param guest Objekt sa podacima gosta.
     * @param connection Aktivna JDBC konekcija.
     * @throws SQLException U slučaju narušavanja Foreign Key ograničenja za ADDRESS_ID ili duplog ID-a.
     */
    public void save(Guest guest, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, guest.getId());
            ps.setString(2, guest.getFirstName());
            ps.setString(3, guest.getLastName());
            ps.setString(4, guest.getEmail());
            ps.setString(5, guest.getPhoneNumber());
            ps.setDate(6, new java.sql.Date(guest.getDateOfBirth().getTime()));
            ps.setString(7, guest.getDocumentNumber());
            ps.setLong(8, guest.getAddressId());
            ps.executeUpdate();

            // Evidencija akcije u DatabaseLogger
            DatabaseLogger.log(connection, "POST", "NBP_GUEST");
        }
    }

    /**
     * Pronalazi gosta na osnovu ID-a.
     * * @param id Jedinstveni identifikator gosta.
     * @param connection Aktivna JDBC konekcija.
     * @return Optional objekt sa podacima o gostu.
     */
    public Optional<Guest> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToGuest(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Vraća listu svih registrovanih gostiju u sistemu.
     */
    public List<Guest> findAll(Connection connection) throws SQLException {
        List<Guest> guests = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    guests.add(mapResultSetToGuest(rs));
                }
            }
        }
        return guests;
    }

    /**
     * Modifikuje postojeće podatke o gostu.
     * * @param guest Objekt sa ažuriranim podacima.
     * @param connection Aktivna JDBC konekcija.
     */
    public void update(Guest guest, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, guest.getFirstName());
            ps.setString(2, guest.getLastName());
            ps.setString(3, guest.getEmail());
            ps.setString(4, guest.getPhoneNumber());
            ps.setDate(5, new java.sql.Date(guest.getDateOfBirth().getTime()));
            ps.setString(6, guest.getDocumentNumber());
            ps.setLong(7, guest.getAddressId());
            ps.setLong(8, guest.getId());
            ps.executeUpdate();

            DatabaseLogger.log(connection, "PUT", "NBP_GUEST");
        }
    }

    /**
     * Brisanje gosta iz baze podataka.
     * <p><b>Upozorenje:</b> Operacija će rezultovati greškom ako gost ima aktivne rezervacije
     * zbog Foreign Key restrikcija.</p>
     */
    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            DatabaseLogger.log(connection, "DELETE", "NBP_GUEST");
        }
    }

    /**
     * Pomoćna metoda za mapiranje SQL rezultata u {@link Guest} model.
     * Vrši konverziju SQL Date tipa u Java Date.
     */
    private Guest mapResultSetToGuest(ResultSet rs) throws SQLException {
        return new Guest(
                rs.getLong("ID"),
                rs.getString("FIRST_NAME"),
                rs.getString("LAST_NAME"),
                rs.getString("EMAIL"),
                rs.getString("PHONE_NUMBER"),
                rs.getDate("DATE_OF_BIRTH"),
                rs.getString("DOCUMENT_NUMBER"),
                rs.getLong("ADDRESS_ID")
        );
    }
}