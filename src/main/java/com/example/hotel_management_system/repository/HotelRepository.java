package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Hotel;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorij zadužen za upravljanje osnovnim informacijama o hotelu.
 * Komunicira direktno sa tabelom {@code NBP_HOTEL} unutar baze podataka.
 * * <p>Ova klasa predstavlja vrh hijerarhije u sistemu, s obzirom da su sobe
 * i ostali resursi direktno ili indirektno povezani sa entitetom hotela.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Repository
public class HotelRepository {

    /** SQL upit za unos novog hotela. */
    private final String INSERT_QUERY = """
            INSERT INTO NBP_HOTEL (ID, NAME, DESCRIPTION, PHONE_NUMBER, EMAIL, ADDRESS)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    /** SQL upit za dobavljanje svih hotela iz sistema. */
    private final String SELECT_ALL_QUERY = """
            SELECT ID, NAME, DESCRIPTION, PHONE_NUMBER, EMAIL, ADDRESS
            FROM NBP_HOTEL
            ORDER BY ID
            """;

    /** SQL upit za dobavljanje specifičnog hotela putem primarnog ključa. */
    private final String SELECT_BY_ID_QUERY = """
            SELECT ID, NAME, DESCRIPTION, PHONE_NUMBER, EMAIL, ADDRESS
            FROM NBP_HOTEL
            WHERE ID = ?
            """;

    /** SQL upit za ažuriranje opisa i kontakt informacija hotela. */
    private final String UPDATE_QUERY = """
            UPDATE NBP_HOTEL
            SET NAME = ?, DESCRIPTION = ?, PHONE_NUMBER = ?, EMAIL = ?, ADDRESS = ?
            WHERE ID = ?
            """;

    /** SQL upit za uklanjanje hotela iz sistema. */
    private final String DELETE_QUERY = """
            DELETE FROM NBP_HOTEL
            WHERE ID = ?
            """;

    /**
     * Trajno pohranjuje novi hotel u bazu podataka.
     * * @param hotel Objekt sa podacima hotela.
     * @param connection Aktivna SQL konekcija.
     * @throws SQLException U slučaju greške pri upisu ili narušavanja integriteta podataka.
     */
    public void save(Hotel hotel, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, hotel.getId());
            ps.setString(2, hotel.getName());
            ps.setString(3, hotel.getDescription());
            ps.setString(4, hotel.getPhoneNumber());
            ps.setString(5, hotel.getEmail());
            ps.setLong(6, hotel.getAddressId());
            ps.executeUpdate();

            // Logovanje administrativne akcije kreiranja
            DatabaseLogger.log(connection, "POST", "NBP_HOTEL");
        }
    }

    /**
     * Pronalazi hotel na osnovu njegovog ID-a.
     * * @param id Jedinstveni identifikator hotela.
     * @param connection Aktivna SQL konekcija.
     * @return Optional objekt sa informacijama o hotelu.
     */
    public Optional<Hotel> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToHotel(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Dobavlja listu svih registrovanih hotela u bazi.
     * Korisno za sisteme koji podržavaju više hotelskih jedinica.
     */
    public List<Hotel> findAll(Connection connection) throws SQLException {
        List<Hotel> hotels = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                hotels.add(mapResultSetToHotel(rs));
            }
        }
        return hotels;
    }

    /**
     * Ažurira postojeće podatke o hotelu (npr. promjena e-maila ili telefona).
     */
    public void update(Hotel hotel, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, hotel.getName());
            ps.setString(2, hotel.getDescription());
            ps.setString(3, hotel.getPhoneNumber());
            ps.setString(4, hotel.getEmail());
            ps.setLong(5, hotel.getAddressId());
            ps.setLong(6, hotel.getId());
            ps.executeUpdate();

            DatabaseLogger.log(connection, "PUT", "NBP_HOTEL");
        }
    }

    /**
     * Briše hotel iz evidencije.
     * <p><b>Napomena:</b> Ova operacija će biti odbijena od strane baze ako postoje
     * sobe ili osoblje povezano sa ovim hotelom (Foreign Key constraint).</p>
     */
    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            DatabaseLogger.log(connection, "DELETE", "NBP_HOTEL");
        }
    }

    /**
     * Pomoćna metoda za mapiranje SQL rezultata u {@link Hotel} entitet.
     */
    private Hotel mapResultSetToHotel(ResultSet rs) throws SQLException {
        return new Hotel(
                rs.getLong("ID"),
                rs.getString("NAME"),
                rs.getString("DESCRIPTION"),
                rs.getString("PHONE_NUMBER"),
                rs.getString("EMAIL"),
                rs.getLong("ADDRESS")
        );
    }
}