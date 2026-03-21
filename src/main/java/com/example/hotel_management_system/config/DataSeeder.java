package com.example.hotel_management_system.config;

import java.sql.Connection;
import java.sql.PreparedStatement;

public class DataSeeder {

    public static void seedData() {
        insertAddress();
        //insertHotels();
        //insertGuests();
    }

    private static void insertAddress() {
        String sql = """
            INSERT INTO NBP_ADDRESS (ID, COUNTRY, CITY, ZIP_CODE, STREET, STREET_NUMBER)
            VALUES (?, ?, ?, ?, ?, ?)
        """;

        try{
            Connection conn = DbConfig.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setInt(1,1);
            ps.setString(2, "Bosnia and Herzegovina");
            ps.setString(3, "Sarajevo");
            ps.setString(4, "71000");
            ps.setString(5, "Ferhadija");
            ps.setString(6, "12");
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
