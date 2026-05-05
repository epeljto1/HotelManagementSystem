package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Invoice;
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
 * Repozitorij zadužen za upravljanje fakturama (računima) unutar sistema.
 * Komunicira sa tabelom {@code NBP_INVOICE} i rukuje finansijskim proračunima,
 * popustima i generisanim PDF dokumentima.
 * * <p>Implementira podršku za čuvanje binarnih podataka (BLOB) kako bi se
 * fakture mogle preuzeti direktno iz baze podataka u izvornom obliku.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Repository
public class InvoiceRepository {

    /** SQL upit za dobavljanje svih faktura. */
    private final String SELECT_ALL_QUERY = """
        SELECT ID, ISSUE_DATE, TOTAL_AMOUNT, STATUS, STAY_ID, DISCOUNT_ID, DISCOUNT_AMOUNT, FINAL_AMOUNT, INVOICE_PDF
        FROM NBP_INVOICE
        ORDER BY ID
        """;

    /** SQL upit za dobavljanje fakture putem primarnog ključa. */
    private final String SELECT_BY_ID_QUERY = """
        SELECT ID, ISSUE_DATE, TOTAL_AMOUNT, STATUS, STAY_ID, DISCOUNT_ID, DISCOUNT_AMOUNT, FINAL_AMOUNT, INVOICE_PDF
        FROM NBP_INVOICE
        WHERE ID = ?
        """;

    /** SQL upit za kreiranje nove fakture koristeći sekvencu za generisanje ID-a. */
    private final String INSERT_QUERY = """
        INSERT INTO NBP_INVOICE (ID, ISSUE_DATE, TOTAL_AMOUNT, STATUS, STAY_ID, DISCOUNT_ID, DISCOUNT_AMOUNT, FINAL_AMOUNT, INVOICE_PDF)
        VALUES (NBP_INVOICE_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

    /** SQL upit za ažuriranje postojećih podataka na fakturi. */
    private final String UPDATE_QUERY = """
        UPDATE NBP_INVOICE
        SET ISSUE_DATE = ?, TOTAL_AMOUNT = ?, STATUS = ?, STAY_ID = ?, DISCOUNT_ID = ?, DISCOUNT_AMOUNT = ?, FINAL_AMOUNT = ?, INVOICE_PDF = ?
        WHERE ID = ?
        """;

    /** SQL upit za brisanje fakture. */
    private final String DELETE_QUERY = """
        DELETE FROM NBP_INVOICE
        WHERE ID = ?
        """;

    /** SQL upit za pronalazak fakture vezane za određeni boravak (Stay). */
    private final String FIND_BY_STAY_ID_QUERY = """
        SELECT ID, ISSUE_DATE, TOTAL_AMOUNT, STATUS, STAY_ID, DISCOUNT_ID, DISCOUNT_AMOUNT, FINAL_AMOUNT, INVOICE_PDF
        FROM NBP_INVOICE
        WHERE STAY_ID = ?
        """;

    /**
     * Vraća listu svih faktura iz baze.
     */
    public List<Invoice> findAll(Connection connection) throws SQLException {
        List<Invoice> invoices = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                invoices.add(mapResultSetToInvoice(rs));
            }
        }
        return invoices;
    }

    /**
     * Pronalazi fakturu na osnovu ID-a.
     */
    public Invoice findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        }
        return null;
    }

    /**
     * Spašava novu fakturu u bazu podataka sa transakcijskom kontrolom.
     * <p>Ova metoda manuelno upravlja {@code commit} i {@code rollback} operacijama
     * kako bi osigurala integritet finansijskih zapisa.</p>
     * * @param invoice Objekt fakture sa obračunatim iznosima i PDF dokumentom.
     * @param connection Aktivna JDBC konekcija.
     * @throws SQLException Ako dođe do greške, vrši se poništavanje promjena (rollback).
     */
    public void save(Invoice invoice, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY, new String[]{"ID"})) {
            ps.setDate(1, Date.valueOf(invoice.getIssueDate()));
            ps.setBigDecimal(2, invoice.getTotalAmount());
            ps.setString(3, invoice.getStatus());
            ps.setLong(4, invoice.getStayId());

            // Rukovanje opcionim poljima za popuste (NULL values)
            if (invoice.getDiscountId() != null) {
                ps.setLong(5, invoice.getDiscountId());
            } else {
                ps.setNull(5, java.sql.Types.NUMERIC);
            }

            if (invoice.getDiscountAmount() != null) {
                ps.setBigDecimal(6, invoice.getDiscountAmount());
            } else {
                ps.setNull(6, java.sql.Types.NUMERIC);
            }

            if (invoice.getFinalAmount() != null) {
                ps.setBigDecimal(7, invoice.getFinalAmount());
            } else {
                ps.setNull(7, java.sql.Types.NUMERIC);
            }

            // Upis generisanog PDF-a kao niz bajtova u BLOB kolonu
            ps.setBytes(8, invoice.getInvoicePdf());

            ps.executeUpdate();

            DatabaseLogger.log(connection, "POST", "NBP_INVOICE");

            // Eksplicitna potvrda transakcije za finansijski zapis
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }
    }

    /**
     * Ažurira podatke na fakturi (npr. promjena statusa u 'PAID').
     */
    public void update(Long id, Invoice invoice, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setDate(1, Date.valueOf(invoice.getIssueDate()));
            ps.setBigDecimal(2, invoice.getTotalAmount());
            ps.setString(3, invoice.getStatus());
            ps.setLong(4, invoice.getStayId());

            if (invoice.getDiscountId() != null) {
                ps.setLong(5, invoice.getDiscountId());
            } else {
                ps.setNull(5, java.sql.Types.NUMERIC);
            }

            if (invoice.getDiscountAmount() != null) {
                ps.setBigDecimal(6, invoice.getDiscountAmount());
            } else {
                ps.setNull(6, java.sql.Types.NUMERIC);
            }

            if (invoice.getFinalAmount() != null) {
                ps.setBigDecimal(7, invoice.getFinalAmount());
            } else {
                ps.setNull(7, java.sql.Types.NUMERIC);
            }

            ps.setBytes(8, invoice.getInvoicePdf());
            ps.setLong(9, id);

            ps.executeUpdate();

            DatabaseLogger.log(connection, "PUT", "NBP_INVOICE");
        }
    }

    /**
     * Briše fakturu iz sistema.
     */
    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();
            DatabaseLogger.log(connection, "DELETE", "NBP_INVOICE");
        }
    }

    /**
     * Dobavlja fakturu za specifičan boravak gosta.
     */
    public Invoice findByStayId(Long stayId, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(FIND_BY_STAY_ID_QUERY)) {
            ps.setLong(1, stayId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToInvoice(rs);
                }
            }
        }
        return null;
    }

    /**
     * Pomoćna metoda za mapiranje SQL zapisa u {@link Invoice} entitet.
     * Posebno rukuje čitanjem binarnih podataka i konverzijom NULL vrijednosti.
     */
    private Invoice mapResultSetToInvoice(ResultSet rs) throws SQLException {
        Invoice invoice = new Invoice();
        invoice.setId(rs.getLong("ID"));
        invoice.setIssueDate(rs.getDate("ISSUE_DATE").toLocalDate());
        invoice.setTotalAmount(rs.getBigDecimal("TOTAL_AMOUNT"));
        invoice.setStatus(rs.getString("STATUS"));
        invoice.setStayId(rs.getLong("STAY_ID"));
        invoice.setDiscountId(rs.getObject("DISCOUNT_ID") != null ? rs.getLong("DISCOUNT_ID") : null);
        invoice.setDiscountAmount(rs.getBigDecimal("DISCOUNT_AMOUNT"));
        invoice.setFinalAmount(rs.getBigDecimal("FINAL_AMOUNT"));
        invoice.setInvoicePdf(rs.getBytes("INVOICE_PDF"));
        return invoice;
    }
}