package com.example.hotel_management_system.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public DatabaseInitializer() throws SQLException {
    }

    public static void initialize() throws SQLException {
        // Dodati sve tabele u ovaj fajl na isti način
        String createAddress = """
            CREATE TABLE NBP_ADDRESS (
                ID NUMBER PRIMARY KEY,
                COUNTRY VARCHAR2(100),
                CITY VARCHAR2(100),
                ZIP_CODE VARCHAR2(20),
                STREET VARCHAR2(255),
                STREET_NUMBER VARCHAR2(20)
            )
        """;

        String createHotel = """
            CREATE TABLE NBP_HOTEL (
                ID NUMBER PRIMARY KEY,
                NAME VARCHAR2(255),
                DESCRIPTION VARCHAR2(500),
                PHONE_NUMBER VARCHAR2(20),
                EMAIL VARCHAR2(255),
                ADDRESS NUMBER,
                CONSTRAINT FK_HOTEL_ADDRESS
                    FOREIGN KEY (ADDRESS)
                    REFERENCES NBP_ADDRESS(ID)
            )
        """;

        String createGuest = """
            CREATE TABLE NBP_GUEST (
                ID NUMBER PRIMARY KEY,
                FIRST_NAME VARCHAR2(255),
                LAST_NAME VARCHAR2(255),
                EMAIL VARCHAR2(255),
                PHONE_NUMBER VARCHAR2(20),
                DATE_OF_BIRTH DATE,
                DOCUMENT_NUMBER VARCHAR2(100),
                ADDRESS_ID NUMBER,
                CONSTRAINT FK_GUEST_ADDRESS
                    FOREIGN KEY (ADDRESS_ID)
                    REFERENCES NBP_ADDRESS(ID)
            )
        """;

        String createPayment = """
        CREATE TABLE NBP_PAYMENT (
            ID NUMBER PRIMARY KEY,
            PAYMENT_DATE TIMESTAMP,
            AMOUNT NUMBER,
            PAYMENT_METHOD VARCHAR2(50),
            INVOICE_ID NUMBER
        )
        """;

        String createPaymentSeq = "CREATE SEQUENCE NBP_PAYMENT_SEQ START WITH 1 INCREMENT BY 1";

        try {
            Connection conn = DbConfig.getConnection();
            Statement stmt = conn.createStatement();
            
            // Create tables only if they don't already exist
            if (!tableExists(conn, "NBP_ADDRESS")) {
                stmt.executeUpdate(createAddress);
            }
            if (!tableExists(conn, "NBP_HOTEL")) {
                stmt.executeUpdate(createHotel);
            }
            if (!tableExists(conn, "NBP_GUEST")) {
                stmt.executeUpdate(createGuest);
            }

            if (!tableExists(conn, "NBP_PAYMENT")) {
                stmt.executeUpdate(createPayment);
                System.out.println("Table NBP_PAYMENT created.");
            }

            if (!sequenceExists(conn, "NBP_PAYMENT_SEQ")) {
                stmt.executeUpdate(createPaymentSeq);
                System.out.println("Sequence NBP_PAYMENT_SEQ created.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }


    }

    private static boolean tableExists(Connection conn, String tableName) {
        String query = "SELECT COUNT(*) FROM user_tables WHERE table_name = ?";
        try (var ps = conn.prepareStatement(query)) {
            ps.setString(1, tableName);
            var rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }

    private static boolean sequenceExists(Connection conn, String seqName) {
        String query = "SELECT COUNT(*) FROM user_sequences WHERE sequence_name = ?";
        try (var ps = conn.prepareStatement(query)) {
            ps.setString(1, seqName.toUpperCase());
            var rs = ps.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            return false;
        }
    }
}
