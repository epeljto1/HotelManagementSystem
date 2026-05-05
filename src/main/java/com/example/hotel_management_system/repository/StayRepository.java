package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Stay;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorij zadužen za upravljanje podacima o stvarnom boravku gostiju.
 * Komunicira sa tabelom {@code NBP_STAY} koja služi kao most između
 * rezervacije i finalne fakture.
 * * <p>Ova klasa prati precizna vremena ulaska i izlaska iz sobe (Timestamp),
 * što omogućava razlikovanje planiranog boravka od stvarno provedenog vremena.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Repository
public class StayRepository {

    /** SQL upit za evidentiranje novog boravka nakon izvršenog Check-in procesa. */
    private final String INSERT_QUERY = """
            INSERT INTO NBP_STAY (ID, CHECK_IN_TIME, CHECK_OUT_TIME, RESERVATION_ID, ACTUAL_TOTAL_PRICE)
            VALUES (?, ?, ?, ?, ?)
            """;

    /** SQL upit za dobavljanje historije svih boravaka. */
    private final String SELECT_ALL_QUERY = """
            SELECT ID, CHECK_IN_TIME, CHECK_OUT_TIME, RESERVATION_ID, ACTUAL_TOTAL_PRICE
            FROM NBP_STAY
            ORDER BY ID
            """;

    /** SQL upit za pretragu boravka putem ID-a. */
    private final String SELECT_BY_ID_QUERY = """
            SELECT ID, CHECK_IN_TIME, CHECK_OUT_TIME, RESERVATION_ID, ACTUAL_TOTAL_PRICE
            FROM NBP_STAY
            WHERE ID = ?
            """;

    /** SQL upit za pronalazak boravka vezanog za određenu rezervaciju. */
    private final String SELECT_BY_RESERVATION_ID_QUERY = """
            SELECT ID, CHECK_IN_TIME, CHECK_OUT_TIME, RESERVATION_ID, ACTUAL_TOTAL_PRICE
            FROM NBP_STAY
            WHERE RESERVATION_ID = ?
            """;

    /** SQL upit za ažuriranje podataka o boravku (najčešće prilikom Check-out-a). */
    private final String UPDATE_QUERY = """
            UPDATE NBP_STAY
            SET CHECK_IN_TIME = ?, CHECK_OUT_TIME = ?, RESERVATION_ID = ?, ACTUAL_TOTAL_PRICE = ?
            WHERE ID = ?
            """;

    /** SQL upit za brisanje zapisa o boravku. */
    private final String DELETE_QUERY = """
            DELETE FROM NBP_STAY
            WHERE ID = ?
            """;

    /** SQL upit za manuelno generisanje sljedećeg ID-a (korisno ako se ne koristi DB sekvenca). */
    private final String NEXT_ID_QUERY = """
            SELECT NVL(MAX(ID), 0) + 1 AS NEXT_ID
            FROM NBP_STAY
            """;

    /**
     * Spašava novi boravak u bazu.
     * Rukuje opcionim Check-out vremenom koje je na početku boravka obično {@code null}.
     * * @param stay Objekt sa podacima o boravku.
     * @param connection Aktivna JDBC konekcija.
     */
    public void save(Stay stay, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, stay.getId());
            ps.setTimestamp(2, Timestamp.valueOf(stay.getCheckInTime()));

            if (stay.getCheckOutTime() != null) {
                ps.setTimestamp(3, Timestamp.valueOf(stay.getCheckOutTime()));
            } else {
                ps.setNull(3, Types.TIMESTAMP);
            }

            ps.setLong(4, stay.getReservationId());
            ps.setDouble(5, stay.getActualTotalPrice());
            ps.executeUpdate();

            DatabaseLogger.log(connection, "POST", "NBP_STAY");
        }
    }

    /**
     * Pronalazi boravak na osnovu ID-a.
     */
    public Optional<Stay> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStay(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Pronalazi aktivan ili završen boravak povezan sa ID-om rezervacije.
     * Korisno za prelazak iz faze planiranja u fazu naplate.
     */
    public Optional<Stay> findByReservationId(Long reservationId, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_RESERVATION_ID_QUERY)) {
            ps.setLong(1, reservationId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStay(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Vraća sve zapise o boravcima iz baze podataka.
     */
    public List<Stay> findAll(Connection connection) throws SQLException {
        List<Stay> stays = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                stays.add(mapResultSetToStay(rs));
            }
        }
        return stays;
    }

    /**
     * Izračunava sljedeći ID za unos novog boravka.
     * Implementira logiku {@code MAX(ID) + 1}.
     */
    public Long getNextId(Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(NEXT_ID_QUERY);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong("NEXT_ID");
            }
        }
        return 1L;
    }

    /**
     * Ažurira podatke o boravku. Najčešće se koristi za postavljanje
     * {@code checkOutTime} i finalne cijene pri odlasku gosta.
     */
    public void update(Stay stay, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setTimestamp(1, Timestamp.valueOf(stay.getCheckInTime()));

            if (stay.getCheckOutTime() != null) {
                ps.setTimestamp(2, Timestamp.valueOf(stay.getCheckOutTime()));
            } else {
                ps.setNull(2, Types.TIMESTAMP);
            }

            ps.setLong(3, stay.getReservationId());
            ps.setDouble(4, stay.getActualTotalPrice());
            ps.setLong(5, stay.getId());
            ps.executeUpdate();

            DatabaseLogger.log(connection, "PUT", "NBP_STAY");
        }
    }

    /**
     * Brisanje zapisa o boravku iz sistema.
     */
    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            DatabaseLogger.log(connection, "DELETE", "NBP_STAY");
        }
    }

    /**
     * Pomoćna metoda za mapiranje SQL rezultata u {@link Stay} entitet.
     * Sigurno rukuje konverzijom SQL Timestamp-a u Java LocalDateTime.
     */
    private Stay mapResultSetToStay(ResultSet rs) throws SQLException {
        Timestamp checkInTs = rs.getTimestamp("CHECK_IN_TIME");
        Timestamp checkOutTs = rs.getTimestamp("CHECK_OUT_TIME");

        return new Stay(
                rs.getLong("ID"),
                checkInTs != null ? checkInTs.toLocalDateTime() : null,
                checkOutTs != null ? checkOutTs.toLocalDateTime() : null,
                rs.getLong("RESERVATION_ID"),
                rs.getDouble("ACTUAL_TOTAL_PRICE")
        );
    }
}