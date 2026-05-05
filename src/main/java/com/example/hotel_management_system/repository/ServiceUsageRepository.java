package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.ServiceUsage;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repozitorij zadužen za praćenje konzumacije dodatnih usluga tokom boravka gosta.
 * Povezuje tabelu {@code NBP_SERVICE_USAGE} sa specifičnim boravkom (Stay) i
 * definicijom usluge (Service).
 * * <p>Ovaj repozitorij omogućava dinamičko dodavanje troškova koji će se
 * kasnije sumirati na finalnoj fakturi gosta.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Repository
public class ServiceUsageRepository {

    /** SQL upit za dobavljanje svih zapisa o korištenju usluga. */
    private final String SELECT_ALL_QUERY = """
            SELECT ID, STAY_ID, SERVICE_ID, QUANTITY, USAGE_DATE, TOTAL_PRICE
            FROM NBP_SERVICE_USAGE
            ORDER BY ID
            """;

    /** SQL upit za pretragu specifičnog zapisa konzumacije po ID-u. */
    private final String SELECT_BY_ID_QUERY = """
            SELECT ID, STAY_ID, SERVICE_ID, QUANTITY, USAGE_DATE, TOTAL_PRICE
            FROM NBP_SERVICE_USAGE
            WHERE ID = ?
            """;

    /** SQL upit za unos nove konzumacije usluge. */
    private final String INSERT_QUERY = """
            INSERT INTO NBP_SERVICE_USAGE (ID, STAY_ID, SERVICE_ID, QUANTITY, USAGE_DATE, TOTAL_PRICE)
            VALUES (?, ?, ?, ?, ?, ?)
            """;

    /** SQL upit za ažuriranje podataka o konzumaciji (npr. promjena količine). */
    private final String UPDATE_QUERY = """
            UPDATE NBP_SERVICE_USAGE
            SET STAY_ID = ?, SERVICE_ID = ?, QUANTITY = ?, USAGE_DATE = ?, TOTAL_PRICE = ?
            WHERE ID = ?
            """;

    /** SQL upit za brisanje zapisa o konzumaciji. */
    private final String DELETE_QUERY = """
            DELETE FROM NBP_SERVICE_USAGE
            WHERE ID = ?
            """;

    /** SQL upit za dobavljanje svih usluga koje su konzumirane tokom jednog specifičnog boravka. */
    private final String FIND_BY_STAY_ID_QUERY = """
            SELECT ID, STAY_ID, SERVICE_ID, QUANTITY, USAGE_DATE, TOTAL_PRICE
            FROM NBP_SERVICE_USAGE
            WHERE STAY_ID = ?
            """;

    /**
     * Vraća kompletnu listu svih zapisa o korištenju usluga u sistemu.
     */
    public List<ServiceUsage> findAll(Connection connection) throws SQLException {
        List<ServiceUsage> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(mapResultSet(rs));
            }
        }
        return list;
    }

    /**
     * Pronalazi detalje o konzumaciji na osnovu ID-a zapisa.
     */
    public ServiceUsage findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Evidentira novu konzumaciju usluge (npr. gost je naručio piće).
     * * @param serviceUsage Objekt sa podacima o boravku, usluzi, količini i cijeni.
     * @param connection Aktivna JDBC konekcija.
     */
    public void save(ServiceUsage serviceUsage, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, serviceUsage.getId());
            ps.setLong(2, serviceUsage.getStayId());
            ps.setLong(3, serviceUsage.getServiceId());
            ps.setInt(4, serviceUsage.getQuantity());
            ps.setDate(5, Date.valueOf(serviceUsage.getUsageDate()));
            ps.setBigDecimal(6, serviceUsage.getTotalPrice());
            ps.executeUpdate();

            DatabaseLogger.log(connection, "POST", "NBP_SERVICE_USAGE");
        }
    }

    /**
     * Ažurira postojeći zapis o konzumaciji.
     */
    public void update(Long id, ServiceUsage serviceUsage, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setLong(1, serviceUsage.getStayId());
            ps.setLong(2, serviceUsage.getServiceId());
            ps.setInt(3, serviceUsage.getQuantity());
            ps.setDate(4, Date.valueOf(serviceUsage.getUsageDate()));
            ps.setBigDecimal(5, serviceUsage.getTotalPrice());
            ps.setLong(6, id);
            ps.executeUpdate();

            DatabaseLogger.log(connection, "PUT", "NBP_SERVICE_USAGE");
        }
    }

    /**
     * Briše zapis o konzumaciji usluge.
     */
    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            DatabaseLogger.log(connection, "DELETE", "NBP_SERVICE_USAGE");
        }
    }

    /**
     * Dobavlja sve usluge konzumirane za konkretan boravak.
     * Ključno za generisanje specifikacije troškova uz račun.
     * * @param stayId ID boravka gosta.
     */
    public List<ServiceUsage> findByStayId(Long stayId, Connection connection) throws SQLException {
        List<ServiceUsage> list = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_STAY_ID_QUERY)) {
            ps.setLong(1, stayId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSet(rs));
                }
            }
        }
        return list;
    }

    /**
     * Pomoćna metoda koja vrši JOIN "u hodu" kako bi dobavila naziv usluge.
     * Korisno za prikaz na korisničkom interfejsu (npr. "Wellness" umjesto ID: 5).
     */
    public String findServiceNameById(Long serviceId, Connection conn) throws SQLException {
        String sql = "SELECT NAME FROM NBP_SERVICE WHERE ID = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, serviceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("NAME");
                }
            }
        }
        return "Unknown service";
    }

    /**
     * Mapira SQL ResultSet u {@link ServiceUsage} domenski model.
     */
    private ServiceUsage mapResultSet(ResultSet rs) throws SQLException {
        return new ServiceUsage(
                rs.getLong("ID"),
                rs.getLong("STAY_ID"),
                rs.getLong("SERVICE_ID"),
                rs.getInt("QUANTITY"),
                rs.getDate("USAGE_DATE").toLocalDate(),
                rs.getBigDecimal("TOTAL_PRICE")
        );
    }
}