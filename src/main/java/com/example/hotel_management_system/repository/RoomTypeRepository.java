package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.RoomType;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorijum zadužen za upravljanje kategorijama soba u hotelu.
 * Komunicira sa tabelom {@code NBP_ROOM_TYPE} i čuva definicije kapaciteta i cena.
 * * <p>Ova klasa je centralno mesto za promenu cenovne politike hotela, jer se osnovna
 * cena noćenja definiše upravo na nivou tipa sobe, a ne na nivou pojedinačne sobe.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Repository
public class RoomTypeRepository {

    /** SQL upit za unos novog tipa sobe. Zahteva manuelno prosleđen ID. */
    private final String INSERT_QUERY = """
            INSERT INTO NBP_ROOM_TYPE (ID, NAME, DESCRIPTION, CAPACITY, PRICE_PER_NIGHT)
            VALUES (?, ?, ?, ?, ?)
            """;

    /** SQL upit za listanje svih kategorija soba. */
    private final String SELECT_ALL_QUERY = """
            SELECT ID, NAME, DESCRIPTION, CAPACITY, PRICE_PER_NIGHT
            FROM NBP_ROOM_TYPE
            ORDER BY ID
            """;

    /** SQL upit za dobavljanje specifičnog tipa sobe putem primarnog ključa. */
    private final String SELECT_BY_ID_QUERY = """
            SELECT ID, NAME, DESCRIPTION, CAPACITY, PRICE_PER_NIGHT
            FROM NBP_ROOM_TYPE
            WHERE ID = ?
            """;

    /** SQL upit za ažuriranje opisa, kapaciteta ili cene tipa sobe. */
    private final String UPDATE_QUERY = """
            UPDATE NBP_ROOM_TYPE
            SET NAME = ?, DESCRIPTION = ?, CAPACITY = ?, PRICE_PER_NIGHT = ?
            WHERE ID = ?
            """;

    /** SQL upit za uklanjanje tipa sobe iz kataloga. */
    private final String DELETE_QUERY = """
            DELETE FROM NBP_ROOM_TYPE
            WHERE ID = ?
            """;

    /**
     * Spašava novi tip sobe u bazu podataka.
     * * @param roomType Objekt koji definiše novu kategoriju (npr. 'Suite').
     * @param connection Aktivna JDBC konekcija prosleđena iz servisnog sloja.
     * @throws SQLException U slučaju dupliranja primarnog ključa ili greške u bazi.
     */
    public void save(RoomType roomType, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, roomType.getId());
            ps.setString(2, roomType.getName());
            ps.setString(3, roomType.getDescription());
            ps.setInt(4, roomType.getCapacity());
            ps.setDouble(5, roomType.getPricePerNight());
            ps.executeUpdate();

            // Evidentiranje akcije u DatabaseLogger
            DatabaseLogger.log(connection, "POST", "NBP_ROOM_TYPE");
        }
    }

    /**
     * Pronalazi tip sobe na osnovu ID-a.
     * * @param id Jedinstveni identifikator kategorije.
     * @param connection Aktivna JDBC konekcija.
     * @return Optional objekt sa podacima o tipu sobe.
     */
    public Optional<RoomType> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSet(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Vraća listu svih dostupnih tipova soba u hotelu.
     */
    public List<RoomType> findAll(Connection connection) throws SQLException {
        List<RoomType> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Modifikuje podatke o postojećem tipu sobe (npr. povećanje cene po noćenju).
     */
    public void update(RoomType roomType, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, roomType.getName());
            ps.setString(2, roomType.getDescription());
            ps.setInt(3, roomType.getCapacity());
            ps.setDouble(4, roomType.getPricePerNight());
            ps.setLong(5, roomType.getId());
            ps.executeUpdate();

            DatabaseLogger.log(connection, "PUT", "NBP_ROOM_TYPE");
        }
    }

    /**
     * Brisanje tipa sobe iz baze podataka.
     * <p><b>Napomena:</b> Brisanje neće uspeti ako u sistemu postoje sobe (tabela {@code NBP_ROOM})
     * koje su povezane sa ovim tipom sobe.</p>
     */
    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            DatabaseLogger.log(connection, "DELETE", "NBP_ROOM_TYPE");
        }
    }

    /**
     * Pomoćna metoda za mapiranje SQL rezultata u {@link RoomType} model.
     */
    private RoomType mapResultSet(ResultSet rs) throws SQLException {
        return new RoomType(
                rs.getLong("ID"),
                rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getInt("CAPACITY"),
                rs.getDouble("PRICE_PER_NIGHT")
        );
    }
}