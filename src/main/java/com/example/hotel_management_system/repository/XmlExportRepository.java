package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.dto.XmlExportDTO;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class XmlExportRepository {

    private final JdbcTemplate jdbcTemplate;

    public XmlExportRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<XmlExportDTO> getExportData() {

        String sql = """
                SELECT 
                    g.ID AS guest_id,
                    g.FIRST_NAME || ' ' || g.LAST_NAME AS guest_name,
                    r.ID AS reservation_id,
                    r.STATUS AS reservation_status,
                    rm.ID AS room_id,
                    rm.ROOM_NUMBER AS room_number,
                    rm.STATUS AS room_status
                FROM NBP_GUEST g
                JOIN NBP_RESERVATION r ON r.GUEST_ID = g.ID
                JOIN NBP_ROOM rm ON rm.ID = r.ROOM_ID
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new XmlExportDTO(
                        rs.getLong("guest_id"),
                        rs.getString("guest_name"),
                        rs.getLong("reservation_id"),
                        rs.getString("reservation_status"),
                        rs.getLong("room_id"),
                        rs.getString("room_number"),
                        rs.getString("room_status")
                )
        );
    }
}