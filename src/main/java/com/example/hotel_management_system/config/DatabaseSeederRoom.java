package com.example.hotel_management_system.config;

import java.sql.*;

public class DatabaseSeederRoom {

    public static void seedRooms() {
        insertRoomsIfNotExists();
    }

    private static void insertRoomsIfNotExists() {
        String checkSql = "SELECT COUNT(*) FROM NBP_ROOM";
        String insertSql = """
            INSERT INTO NBP_ROOM (ID, ROOM_NUMBER, FLOOR_NUMBER, STATUS, HOTEL_ID, ROOM_TYPE_ID)
            VALUES (NBP_ROOM_SEQ.NEXTVAL, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = DbConfig.getConnection()) {
            conn.setAutoCommit(false);

            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(checkSql)) {

                if (rs.next() && rs.getInt(1) > 0) {
                    System.out.println("Tabela NBP_ROOM već ima podatke.");
                    return;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                Object[][] rooms = {
                        {"101", 1, "AVAILABLE", 1, 1},
                        {"102", 1, "AVAILABLE", 1, 2},
                        {"201", 2, "OCCUPIED", 1, 1},
                        {"202", 2, "AVAILABLE", 1, 3},
                        {"301", 3, "RESERVED", 1, 2},
                        {"302", 3, "AVAILABLE", 1, 1},
                        {"401", 4, "OUT_OF_SERVICE", 1, 2},
                        {"402", 4, "AVAILABLE", 1, 3},
                        {"501", 5, "AVAILABLE", 1, 1},
                        {"505", 5, "AVAILABLE", 1, 2}
                };

                for (Object[] room : rooms) {
                    ps.setString(1, (String) room[0]); // ROOM_NUMBER
                    ps.setInt(2, (Integer) room[1]);    // FLOOR_NUMBER
                    ps.setString(3, (String) room[2]); // STATUS
                    ps.setLong(4, Long.valueOf(room[3].toString())); // HOTEL_ID
                    ps.setLong(5, Long.valueOf(room[4].toString())); // ROOM_TYPE_ID

                    ps.addBatch();
                }

                ps.executeBatch();
                conn.commit();
                System.out.println("Inicijalnih 10 soba uspješno uneseno u NBP_ROOM.");

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }

        } catch (Exception e) {
            System.err.println("Greška prilikom seeding-a soba: " + e.getMessage());
            e.printStackTrace();
        }
    }
}