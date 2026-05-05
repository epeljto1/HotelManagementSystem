package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Discount;
import org.springframework.stereotype.Repository;
import com.example.hotel_management_system.util.DatabaseLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorij zadužen za direktnu komunikaciju sa tabelom NBP_DISCOUNT u bazi podataka.
 * Implementira standardne CRUD operacije koristeći JDBC i PreparedStatement za zaštitu
 * od SQL injekcija.
 * * <p>Ova klasa upravlja i logikom pronalaženja najpovoljnijeg aktivnog popusta
 * na osnovu trenutnog datuma.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Repository
public class DiscountRepository {

    // Upiti su definisani kao konstante radi preglednosti i lakšeg održavanja
    private final String INSERT_QUERY = """
            INSERT INTO NBP_DISCOUNT (ID, NAME, PERCENTAGE, START_DATE, END_DATE, DESCRIPTION)
            VALUES (NBP_DISCOUNT_SEQ.NEXTVAL, ?, ?, ?, ?, ?)
            """;

    private final String SELECT_ALL_QUERY = "SELECT * FROM NBP_DISCOUNT ORDER BY ID";

    private final String SELECT_BY_ID_QUERY = "SELECT * FROM NBP_DISCOUNT WHERE ID = ?";

    private final String UPDATE_QUERY = """
            UPDATE NBP_DISCOUNT
            SET NAME = ?, PERCENTAGE = ?, START_DATE = ?, END_DATE = ?, DESCRIPTION = ?
            WHERE ID = ?
            """;

    private final String DELETE_QUERY = "DELETE FROM NBP_DISCOUNT WHERE ID = ?";

    /**
     * Upit koji pronalazi važeći popust za zadati datum.
     * Ukoliko postoji više preklapajućih popusta, bira se onaj sa najvećim procentom.
     */
    private final String FIND_ACTIVE_DISCOUNT_BY_DATE_QUERY = """
        SELECT *
        FROM NBP_DISCOUNT
        WHERE ? BETWEEN START_DATE AND END_DATE
        ORDER BY PERCENTAGE DESC, ID ASC
        FETCH FIRST 1 ROWS ONLY
        """;

    /**
     * Snima novi popust u bazu.
     * @param discount Objekt popusta.
     * @param connection Aktivna SQL konekcija proslijeđena iz servisa.
     * @throws SQLException U slučaju greške u SQL sintaksi ili ograničenjima baze.
     */
    public void save(Discount discount, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setString(1, discount.getName());
            ps.setDouble(2, discount.getPercentage());
            ps.setDate(3, Date.valueOf(discount.getStartDate()));
            ps.setDate(4, Date.valueOf(discount.getEndDate()));
            ps.setString(5, discount.getDescription());
            ps.executeUpdate();

            // Automatsko logovanje akcije u tabelu za praćenje promjena
            DatabaseLogger.log(connection, "POST", "NBP_DISCOUNT");
        }
    }

    /**
     * Vraća sve popuste sortirane po ID-u.
     */
    public List<Discount> findAll(Connection connection) throws SQLException {
        List<Discount> discounts = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                discounts.add(mapResultSetToDiscount(rs));
            }
        }
        return discounts;
    }

    /**
     * Pronalazi popust putem primarnog ključa.
     */
    public Optional<Discount> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDiscount(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Ažurira podatke postojećeg popusta.
     */
    public void update(Discount discount, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, discount.getName());
            ps.setDouble(2, discount.getPercentage());
            ps.setDate(3, Date.valueOf(discount.getStartDate()));
            ps.setDate(4, Date.valueOf(discount.getEndDate()));
            ps.setString(5, discount.getDescription());
            ps.setLong(6, discount.getId());
            ps.executeUpdate();

            DatabaseLogger.log(connection, "PUT", "NBP_DISCOUNT");
        }
    }

    /**
     * Briše popust iz baze podataka.
     */
    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            DatabaseLogger.log(connection, "DELETE", "NBP_DISCOUNT");
        }
    }

    /**
     * Pronalazi aktivan popust za određeni datum (npr. datum izdavanja računa).
     * Fokusira se na popust sa najvećim procentom ako se termini preklapaju.
     * * @param date Datum za koji se traži popust.
     * @return Optional sa popustom ili empty ako popusta nema.
     */
    public Optional<Discount> findActiveDiscountByDate(Date date, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(FIND_ACTIVE_DISCOUNT_BY_DATE_QUERY)) {
            ps.setDate(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDiscount(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Pomoćna metoda za mapiranje reda iz baze (ResultSet) u model objekt Discount.
     * Konvertuje SQL datume u Java 8 LocalDate tip.
     */
    private Discount mapResultSetToDiscount(ResultSet rs) throws SQLException {
        return new Discount(
                rs.getLong("ID"),
                rs.getString("NAME"),
                rs.getDouble("PERCENTAGE"),
                rs.getDate("START_DATE").toLocalDate(),
                rs.getDate("END_DATE").toLocalDate(),
                rs.getString("DESCRIPTION")
        );
    }
}