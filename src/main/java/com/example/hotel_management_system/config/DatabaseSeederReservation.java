package com.example.hotel_management_system.config;

import java.sql.*;
import java.time.LocalDateTime;

public class DatabaseSeederReservation {

    public static void seedReservations() {
        String checkSql = "SELECT COUNT(*) FROM NBP_RESERVATION";
        String insertSql = """
            INSERT INTO NBP_RESERVATION (ID, RESERVATION_DATE, CHECK_IN_DATE, CHECK_OUT_DATE, 
                                       NUMBER_OF_GUESTS, STATUS, TOTAL_PRICE, GUEST_ID, ROOM_ID, CREATED_BY)
            VALUES (NBP_RESERVATION_SEQ.NEXTVAL, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DbConfig.getConnection()) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkSql)) {
                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Tabela NBP_RESERVATION već ima podatke.");
                    return;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                Object[][] reservations = {
                        {Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().plusDays(1)), Timestamp.valueOf(LocalDateTime.now().plusDays(4)), 2, "CONFIRMED", 350.0, 1L, 23L, 1L},
                        {Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().plusDays(2)), Timestamp.valueOf(LocalDateTime.now().plusDays(5)), 1, "PENDING", 120.0, 2L, 24L, 3L},
                        {Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().plusDays(10)), Timestamp.valueOf(LocalDateTime.now().plusDays(15)), 3, "CONFIRMED", 600.0, 3L, 25L, 2L},
                        {Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().minusDays(5)), Timestamp.valueOf(LocalDateTime.now().minusDays(1)), 2, "COMPLETED", 200.0, 1L, 26L, 4L},
                        {Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().plusDays(7)), Timestamp.valueOf(LocalDateTime.now().plusDays(9)), 2, "CONFIRMED", 280.0, 2L, 27L, 3L},
                        {Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().plusDays(3)), Timestamp.valueOf(LocalDateTime.now().plusDays(6)), 1, "CANCELLED", 150.0, 3L, 28L, 1L},
                        {Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().plusDays(12)), Timestamp.valueOf(LocalDateTime.now().plusDays(14)), 4, "PENDING", 800.0, 1L, 29L, 2L},
                        {Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().minusDays(10)), Timestamp.valueOf(LocalDateTime.now().minusDays(7)), 2, "COMPLETED", 320.0, 2L, 30L, 3L},
                        {Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().plusDays(20)), Timestamp.valueOf(LocalDateTime.now().plusDays(25)), 1, "CONFIRMED", 500.0, 3L, 31L, 1L},
                        {Timestamp.valueOf(LocalDateTime.now()), Timestamp.valueOf(LocalDateTime.now().plusDays(1)), Timestamp.valueOf(LocalDateTime.now().plusDays(2)), 2, "PENDING", 100.0, 1L, 32L, 4L}
                };

                for (Object[] res : reservations) {
                    ps.setTimestamp(1, (Timestamp) res[0]); // RESERVATION_DATE
                    ps.setTimestamp(2, (Timestamp) res[1]); // CHECK_IN_DATE
                    ps.setTimestamp(3, (Timestamp) res[2]); // CHECK_OUT_DATE
                    ps.setInt(4, (Integer) res[3]);         // NUMBER_OF_GUESTS
                    ps.setString(5, (String) res[4]);      // STATUS
                    ps.setDouble(6, (Double) res[5]);       // TOTAL_PRICE
                    ps.setLong(7, (Long) res[6]);           // GUEST_ID (1, 2, 3)
                    ps.setLong(8, (Long) res[7]);           // ROOM_ID (23, 24, 25, 26)
                    ps.setLong(9, (Long) res[8]);           // CREATED_BY (1, 3, 2, 4)
                    ps.addBatch();
                }

                ps.executeBatch();
                conn.commit();
                System.out.println("Inicijalne rezervacije uspješno unesene.");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Greška u seederu rezervacija: " + e.getMessage());
            e.printStackTrace();
        }
    }
}