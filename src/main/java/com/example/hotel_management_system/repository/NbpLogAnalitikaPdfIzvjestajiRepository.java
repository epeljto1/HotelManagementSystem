package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.NbpLogAnalitikaPdfIzvjestaj;
import com.example.hotel_management_system.util.DatabaseLogger;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;

@Repository
public class NbpLogAnalitikaPdfIzvjestajiRepository {

    public static final long DEFAULT_RECORD_ID = 1L;

    private static final String TABLE_NAME = "NBP_LOG_ANALITIKA_PDF_IZVJESTAJI";

    private static final String SELECT_BY_ID = """
            SELECT ID, DATUM_GENERISANJA, PDF_IZVJESTAJ
            FROM NBP_LOG_ANALITIKA_PDF_IZVJESTAJI
            WHERE ID = ?
            """;

    private static final String EXISTS_QUERY = """
            SELECT COUNT(*) FROM NBP_LOG_ANALITIKA_PDF_IZVJESTAJI WHERE ID = ?
            """;

    private static final String INSERT_QUERY = """
            INSERT INTO NBP_LOG_ANALITIKA_PDF_IZVJESTAJI (
                ID, DATUM_GENERISANJA, PDF_IZVJESTAJ
            ) VALUES (?, ?, ?)
            """;

    private static final String UPDATE_QUERY = """
            UPDATE NBP_LOG_ANALITIKA_PDF_IZVJESTAJI
            SET DATUM_GENERISANJA = ?,
                PDF_IZVJESTAJ = ?
            WHERE ID = ?
            """;

    public NbpLogAnalitikaPdfIzvjestaj findById(Long id, Connection connection) throws SQLException {
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

    public void save(NbpLogAnalitikaPdfIzvjestaj entity, Connection connection) throws SQLException {
        if (exists(entity.getId(), connection)) {
            update(entity, connection);
        } else {
            insert(entity, connection);
        }
    }

    private boolean exists(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(EXISTS_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    private void insert(NbpLogAnalitikaPdfIzvjestaj entity, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setLong(1, entity.getId());
            ps.setTimestamp(2, Timestamp.valueOf(entity.getDatumGenerisanja()));
            setBlob(ps, 3, entity.getPdfIzvjestaj());
            ps.executeUpdate();
            DatabaseLogger.log(connection, "POST", TABLE_NAME);
        }
    }

    private void update(NbpLogAnalitikaPdfIzvjestaj entity, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setTimestamp(1, Timestamp.valueOf(entity.getDatumGenerisanja()));
            setBlob(ps, 2, entity.getPdfIzvjestaj());
            ps.setLong(3, entity.getId());
            ps.executeUpdate();
            DatabaseLogger.log(connection, "PUT", TABLE_NAME);
        }
    }

    private void setBlob(PreparedStatement ps, int index, byte[] data) throws SQLException {
        if (data == null || data.length == 0) {
            ps.setNull(index, Types.BLOB);
        } else {
            ps.setBytes(index, data);
        }
    }

    private NbpLogAnalitikaPdfIzvjestaj mapResultSet(ResultSet rs) throws SQLException {
        NbpLogAnalitikaPdfIzvjestaj entity = new NbpLogAnalitikaPdfIzvjestaj();
        entity.setId(rs.getLong("ID"));
        Timestamp ts = rs.getTimestamp("DATUM_GENERISANJA");
        entity.setDatumGenerisanja(ts != null ? ts.toLocalDateTime() : null);
        entity.setPdfIzvjestaj(rs.getBytes("PDF_IZVJESTAJ"));
        return entity;
    }
}
