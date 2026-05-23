package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.PdfIzvjestaji;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;

@Repository
public class PdfIzvjestajiRepository {

    public static final long DEFAULT_RECORD_ID = 1L;

    private static final String SELECT_BY_ID = """
            SELECT ID, DATUM_GENERISANJA, PDF_REZERVACIJE, PDF_FAKTURE, PDF_USLUGE, PDF_LOYALTY
            FROM PDF_IZVJESTAJI
            WHERE ID = ?
            """;

    private static final String EXISTS_QUERY = """
            SELECT COUNT(*) FROM PDF_IZVJESTAJI WHERE ID = ?
            """;

    private static final String INSERT_QUERY = """
            INSERT INTO PDF_IZVJESTAJI (
                ID, DATUM_GENERISANJA, PDF_REZERVACIJE, PDF_FAKTURE, PDF_USLUGE, PDF_LOYALTY
            ) VALUES (?, ?, ?, ?, ?, ?)
            """;

    private static final String UPDATE_QUERY = """
            UPDATE PDF_IZVJESTAJI
            SET DATUM_GENERISANJA = ?,
                PDF_REZERVACIJE = ?,
                PDF_FAKTURE = ?,
                PDF_USLUGE = ?,
                PDF_LOYALTY = ?
            WHERE ID = ?
            """;

    public boolean exists(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(EXISTS_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    public PdfIzvjestaji findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        }
        return null;
    }

    public void save(PdfIzvjestaji entity, Connection connection) throws SQLException {
        if (exists(entity.getId(), connection)) {
            update(entity, connection);
        } else {
            insert(entity, connection);
        }
    }

    private void insert(PdfIzvjestaji entity, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            bindAllColumns(ps, entity);
            ps.executeUpdate();
            DatabaseLogger.log(connection, "POST", "PDF_IZVJESTAJI");
        }
    }

    private void update(PdfIzvjestaji entity, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setTimestamp(1, Timestamp.valueOf(entity.getDatumGenerisanja()));
            setBlob(ps, 2, entity.getPdfRezervacije());
            setBlob(ps, 3, entity.getPdfFakture());
            setBlob(ps, 4, entity.getPdfUsluge());
            setBlob(ps, 5, entity.getPdfLoyalty());
            ps.setLong(6, entity.getId());
            ps.executeUpdate();
            DatabaseLogger.log(connection, "PUT", "PDF_IZVJESTAJI");
        }
    }

    private void bindAllColumns(PreparedStatement ps, PdfIzvjestaji entity) throws SQLException {
        ps.setLong(1, entity.getId());
        ps.setTimestamp(2, Timestamp.valueOf(entity.getDatumGenerisanja()));
        setBlob(ps, 3, entity.getPdfRezervacije());
        setBlob(ps, 4, entity.getPdfFakture());
        setBlob(ps, 5, entity.getPdfUsluge());
        setBlob(ps, 6, entity.getPdfLoyalty());
    }

    private void setBlob(PreparedStatement ps, int index, byte[] data) throws SQLException {
        if (data == null || data.length == 0) {
            ps.setNull(index, Types.BLOB);
        } else {
            ps.setBytes(index, data);
        }
    }

    private PdfIzvjestaji mapResultSet(ResultSet rs) throws SQLException {
        PdfIzvjestaji entity = new PdfIzvjestaji();
        entity.setId(rs.getLong("ID"));
        Timestamp ts = rs.getTimestamp("DATUM_GENERISANJA");
        entity.setDatumGenerisanja(ts != null ? ts.toLocalDateTime() : null);
        entity.setPdfRezervacije(rs.getBytes("PDF_REZERVACIJE"));
        entity.setPdfFakture(rs.getBytes("PDF_FAKTURE"));
        entity.setPdfUsluge(rs.getBytes("PDF_USLUGE"));
        entity.setPdfLoyalty(rs.getBytes("PDF_LOYALTY"));
        return entity;
    }
}
