package com.example.hotel_management_system.config;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Date;
import java.time.LocalDate;
import java.security.MessageDigest;

public class DataSeeder {

    public static void seedData() {
        //insertAddress();
        //insertHotels();
        //insertGuests();
        insertUsers();
    }

    private static void insertUsers() {
        String sql = """
            INSERT INTO NBP_USER (ID, USER_ID, USERNAME, EMAIL, PASSWORD_HASH, ROLE, CREATED_DATE)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try {
            Connection conn = DbConfig.getConnection();
            
            // Array of user data
            String[][] users = {
                {"user1", "user1@example.com", "password1", "ADMIN"},
                {"user2", "user2@example.com", "password2", "USER"},
                {"user3", "user3@example.com", "password3", "MANAGER"},
                {"user4", "user4@example.com", "password4", "USER"},
                {"user5", "user5@example.com", "password5", "USER"},
                {"user6", "user6@example.com", "password6", "RECEPTIONIST"},
                {"user7", "user7@example.com", "password7", "ADMIN"},
                {"user8", "user8@example.com", "password8", "USER"},
                {"user9", "user9@example.com", "password9", "MANAGER"},
                {"user10", "user10@example.com", "password10", "USER"}
            };

            for (int i = 0; i < users.length; i++) {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, i + 1); // ID
                ps.setInt(2, i + 100); // USER_ID
                ps.setString(3, users[i][0]); // USERNAME
                ps.setString(4, users[i][1]); // EMAIL
                ps.setString(5, hashPassword(users[i][2])); // PASSWORD_HASH (SHA-256)
                ps.setString(6, users[i][3]); // ROLE
                ps.setDate(7, Date.valueOf(LocalDate.now())); // CREATED_DATE
                ps.executeUpdate();
                ps.close();
            }

            System.out.println("10 users inserted into NBP_USER table successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

/*
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
    }*/
}
