package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.dto.ViewReportData;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ViewReportRepository {

    private static final int MAX_ROWS = 500;

    private static final String REZERVACIJE_QUERY = """
            SELECT * FROM V_NBP_REZERVACIJSKI_PREGLED WHERE ROWNUM <= ?
            """;

    private static final String FAKTURE_QUERY = """
            SELECT * FROM V_NBP_FAKTURA_DETALJI WHERE ROWNUM <= ?
            """;

    private static final String USLUGE_QUERY = """
            SELECT * FROM V_NBP_USLUGA_PREGLED WHERE ROWNUM <= ?
            """;

    private static final String LOYALTY_QUERY = """
            SELECT * FROM V_GUEST_LOYALTY_PROFILE WHERE ROWNUM <= ?
            """;

    public ViewReportData fetchRezervacijskiPregled(Connection connection) throws SQLException {
        return fetchFromView(connection, REZERVACIJE_QUERY);
    }

    public ViewReportData fetchFakturaDetalji(Connection connection) throws SQLException {
        return fetchFromView(connection, FAKTURE_QUERY);
    }

    public ViewReportData fetchUslugaPregled(Connection connection) throws SQLException {
        return fetchFromView(connection, USLUGE_QUERY);
    }

    public ViewReportData fetchGuestLoyaltyProfile(Connection connection) throws SQLException {
        return fetchFromView(connection, LOYALTY_QUERY);
    }

    private ViewReportData fetchFromView(Connection connection, String sql) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, MAX_ROWS);
            try (ResultSet rs = ps.executeQuery()) {
                return mapResultSet(rs);
            }
        }
    }

    private ViewReportData mapResultSet(ResultSet rs) throws SQLException {
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        List<String> headers = new ArrayList<>(columnCount);
        for (int i = 1; i <= columnCount; i++) {
            headers.add(meta.getColumnLabel(i));
        }

        List<List<String>> rows = new ArrayList<>();
        while (rs.next()) {
            List<String> row = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                Object value = rs.getObject(i);
                row.add(value == null ? "" : value.toString());
            }
            rows.add(row);
        }

        return new ViewReportData(headers, rows);
    }
}
