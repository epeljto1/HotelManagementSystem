package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.enums.ReservationStatus;
import com.example.hotel_management_system.model.Reservation;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorij zadužen za upravljanje rezervacijama u hotelu.
 * Komunicira sa tabelom {@code NBP_RESERVATION} i prati kompletan životni ciklus
 * od momenta bukiranja do realizacije boravka.
 * * <p>Ovaj repozitorij igra ključnu ulogu u koordinaciji između gostiju i slobodnih soba,
 * osiguravajući da svaki zapis sadrži informaciju o autoru rezervacije ({@code CREATED_BY}).</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Repository
public class ReservationRepository {

    /** SQL upit za unos nove rezervacije koristeći sekvencu za ID. */
    private final String INSERT_QUERY = """
            INSERT INTO NBP_RESERVATION (ID, RESERVATION_DATE, CHECK_IN_DATE, CHECK_OUT_DATE, 
                                       NUMBER_OF_GUESTS, STATUS, TOTAL_PRICE, GUEST_ID, ROOM_ID, CREATED_BY)
            VALUES (NBP_RESERVATION_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

    /** SQL upit za dobavljanje svih rezervacija. */
    private final String SELECT_ALL_QUERY = "SELECT * FROM NBP_RESERVATION ORDER BY ID";

    /** SQL upit za pretragu rezervacije po ID-u. */
    private final String SELECT_BY_ID_QUERY = "SELECT * FROM NBP_RESERVATION WHERE ID = ?";

    /** SQL upit za potpunu nadogradnju podataka rezervacije. */
    private final String UPDATE_QUERY = """
            UPDATE NBP_RESERVATION 
            SET RESERVATION_DATE = ?, CHECK_IN_DATE = ?, CHECK_OUT_DATE = ?, 
                NUMBER_OF_GUESTS = ?, STATUS = ?, TOTAL_PRICE = ?, 
                GUEST_ID = ?, ROOM_ID = ?, CREATED_BY = ?
            WHERE ID = ?
            """;

    /** SQL upit za brzu promjenu statusa rezervacije (npr. kod otkazivanja ili check-ina). */
    private final String UPDATE_STATUS_QUERY = """
            UPDATE NBP_RESERVATION
            SET STATUS = ?
            WHERE ID = ?
            """;

    /** SQL upit za brisanje rezervacije. */
    private final String DELETE_QUERY = "DELETE FROM NBP_RESERVATION WHERE ID = ?";

    /** SQL upit za validaciju rezervacije na osnovu njenog trenutnog stanja. */
    private final String FIND_BY_RESERVATION_ID_AND_STATUS = """
            SELECT * FROM NBP_RESERVATION WHERE ID = ? AND STATUS = ?
            """;

    /**
     * Trajno pohranjuje novu rezervaciju.
     * Konvertuje Java Date u SQL Timestamp radi preciznosti vremena prijave i odjave.
     * * @param res Objekt rezervacije.
     * @param conn Aktivna JDBC konekcija.
     * @throws SQLException U slučaju greške u bazi podataka.
     */
    public void save(Reservation res, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(INSERT_QUERY)) {
            ps.setTimestamp(1, new Timestamp(res.getReservationDate().getTime()));
            ps.setTimestamp(2, new Timestamp(res.getCheckInDate().getTime()));
            ps.setTimestamp(3, new Timestamp(res.getCheckOutDate().getTime()));
            ps.setInt(4, res.getNumberOfGuests());
            ps.setString(5, res.getStatus().name());
            ps.setDouble(6, res.getTotalPrice());
            ps.setLong(7, res.getGuestId());
            ps.setLong(8, res.getRoomId());
            ps.setLong(9, res.getCreatedBy());

            ps.executeUpdate();

            // Logovanje kreiranja nove rezervacije
            DatabaseLogger.log(conn, "POST", "NBP_RESERVATION");
        }
    }

    /**
     * Dobavlja listu svih rezervacija iz sistema.
     */
    public List<Reservation> findAll(Connection conn) throws SQLException {
        List<Reservation> reservations = new ArrayList<>();
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(SELECT_ALL_QUERY)) {
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        }
        return reservations;
    }

    /**
     * Pronalazi rezervaciju putem njenog primarnog ključa.
     */
    public Optional<Reservation> findById(Long id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReservation(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Ažurira sve parametre postojeće rezervacije.
     */
    public void update(Reservation res, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_QUERY)) {
            ps.setTimestamp(1, new Timestamp(res.getReservationDate().getTime()));
            ps.setTimestamp(2, new Timestamp(res.getCheckInDate().getTime()));
            ps.setTimestamp(3, new Timestamp(res.getCheckOutDate().getTime()));
            ps.setInt(4, res.getNumberOfGuests());
            ps.setString(5, res.getStatus().name());
            ps.setDouble(6, res.getTotalPrice());
            ps.setLong(7, res.getGuestId());
            ps.setLong(8, res.getRoomId());
            ps.setLong(9, res.getCreatedBy());
            ps.setLong(10, res.getId());

            ps.executeUpdate();
            DatabaseLogger.log(conn, "PUT", "NBP_RESERVATION");
        }
    }

    /**
     * Specijalizovana metoda za promjenu samo statusa rezervacije.
     * Koristi se za brze operacije unutar kompleksnih transakcija.
     * * @param id ID rezervacije.
     * @param status Novi {@link ReservationStatus}.
     */
    public void updateStatus(Long id, ReservationStatus status, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(UPDATE_STATUS_QUERY)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();

            DatabaseLogger.log(conn, "PUT", "NBP_RESERVATION");
        }
    }

    /**
     * Uklanja rezervaciju iz baze podataka.
     */
    public void delete(Long id, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            DatabaseLogger.log(conn, "DELETE", "NBP_RESERVATION");
        }
    }

    /**
     * Pomoćna metoda za validaciju i pretragu rezervacija po stanju.
     */
    public Optional<Reservation> findByIdAndStatus(Long id, ReservationStatus status, Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(FIND_BY_RESERVATION_ID_AND_STATUS)) {
            ps.setLong(1, id);
            ps.setString(2, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToReservation(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Mapira red iz baze podataka u Java objekt {@link Reservation}.
     * Vrši konverziju SQL Stringa u odgovarajući Enum tip.
     */
    private Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        return new Reservation(
                rs.getLong("ID"),
                rs.getTimestamp("RESERVATION_DATE"),
                rs.getTimestamp("CHECK_IN_DATE"),
                rs.getTimestamp("CHECK_OUT_DATE"),
                rs.getInt("NUMBER_OF_GUESTS"),
                ReservationStatus.valueOf(rs.getString("STATUS")),
                rs.getDouble("TOTAL_PRICE"),
                rs.getLong("GUEST_ID"),
                rs.getLong("ROOM_ID"),
                rs.getLong("CREATED_BY")
        );
    }
}