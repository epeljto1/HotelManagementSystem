package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Payment;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repozitorij zadužen za evidenciju i upravljanje transakcijama plaćanja.
 * Komunicira sa tabelom {@code NBP_PAYMENT} i omogućava praćenje uplata vezanih za fakture.
 * * <p>Ova klasa podržava različite metode plaćanja (gotovina, kartica, itd.) i precizno
 * bilježi vremenske oznake (timestamps) svake transakcije.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Repository
public class PaymentRepository {

    /** SQL upit za unos nove uplate koristeći sekvencu NBP_PAYMENT_SEQ. */
    private final String INSERT_QUERY = """
            INSERT INTO NBP_PAYMENT (ID, PAYMENT_DATE, AMOUNT, PAYMENT_METHOD, INVOICE_ID)
            VALUES (NBP_PAYMENT_SEQ.NEXTVAL, ?, ?, ?, ?)
            """;

    /** SQL upit za dobavljanje svih transakcija sortiranih po ID-u. */
    private final String SELECT_ALL_QUERY = "SELECT * FROM NBP_PAYMENT ORDER BY ID";

    /** SQL upit za pretragu konkretne uplate putem ID-a. */
    private final String SELECT_BY_ID_QUERY = "SELECT * FROM NBP_PAYMENT WHERE ID = ?";

    /** SQL upit za ažuriranje podataka o transakciji. */
    private final String UPDATE_QUERY = """
            UPDATE NBP_PAYMENT
            SET PAYMENT_DATE = ?, AMOUNT = ?, PAYMENT_METHOD = ?, INVOICE_ID = ?
            WHERE ID = ?
            """;

    /** SQL upit za brisanje zapisa o uplati. */
    private final String DELETE_QUERY = "DELETE FROM NBP_PAYMENT WHERE ID = ?";

    /**
     * Trajno pohranjuje zapis o uplati u bazu podataka.
     * * @param payment Objekt sa podacima o transakciji.
     * @param connection Aktivna JDBC konekcija.
     * @throws SQLException U slučaju greške pri radu sa SQL-om.
     */
    public void save(Payment payment, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setTimestamp(1, Timestamp.valueOf(payment.getPaymentDate()));
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());

            // Provjera null vrijednosti za Invoice ID (opcionalna veza)
            if (payment.getInvoiceId() != null) {
                ps.setLong(4, payment.getInvoiceId());
            } else {
                ps.setNull(4, java.sql.Types.NUMERIC);
            }
            ps.executeUpdate();

            // Logovanje finansijske akcije
            DatabaseLogger.log(connection, "POST", "NBP_PAYMENT");
        }
    }

    /**
     * Vraća listu svih izvršenih uplata u sistemu.
     */
    public List<Payment> findAll(Connection connection) throws SQLException {
        List<Payment> payments = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                payments.add(mapResultSetToPayment(rs));
            }
        }
        return payments;
    }

    /**
     * Pronalazi uplatu na osnovu primarnog ključa.
     */
    public Optional<Payment> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToPayment(rs));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Ažurira podatke o postojećoj uplati.
     */
    public void update(Payment payment, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setTimestamp(1, Timestamp.valueOf(payment.getPaymentDate()));
            ps.setDouble(2, payment.getAmount());
            ps.setString(3, payment.getPaymentMethod());

            if (payment.getInvoiceId() != null) {
                ps.setLong(4, payment.getInvoiceId());
            } else {
                ps.setNull(4, java.sql.Types.NUMERIC);
            }
            ps.setLong(5, payment.getId());
            ps.executeUpdate();

            DatabaseLogger.log(connection, "PUT", "NBP_PAYMENT");
        }
    }

    /**
     * Uklanja zapis o uplati iz baze.
     */
    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            DatabaseLogger.log(connection, "DELETE", "NBP_PAYMENT");
        }
    }

    /**
     * Pomoćna metoda za mapiranje SQL rezultata u {@link Payment} model.
     * Ispravno rukuje opcionim {@code INVOICE_ID} poljem provjerom {@code wasNull()}.
     */
    private Payment mapResultSetToPayment(ResultSet rs) throws SQLException {
        Long invoiceId = rs.getLong("INVOICE_ID");
        if (rs.wasNull()) {
            invoiceId = null;
        }

        return new Payment(
                rs.getLong("ID"),
                rs.getTimestamp("PAYMENT_DATE").toLocalDateTime(),
                rs.getDouble("AMOUNT"),
                rs.getString("PAYMENT_METHOD"),
                invoiceId
        );
    }

    /**
     * Izračunava ukupni uplaćeni iznos za određenu fakturu.
     * Korisno za validaciju statusa fakture (npr. da li je plaćena u cijelosti).
     * * @param invoiceId ID fakture za koju se vrši obračun.
     * @param connection Aktivna SQL konekcija.
     * @return double Ukupna suma svih povezanih uplata.
     */
    public double getTotalPaidForInvoice(Long invoiceId, Connection connection) throws SQLException {
        String query = "SELECT SUM(AMOUNT) FROM NBP_PAYMENT WHERE INVOICE_ID = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, invoiceId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    double total = rs.getDouble(1);
                    return rs.wasNull() ? 0.0 : total;
                }
            }
        }

        return 0.0;
    }
}