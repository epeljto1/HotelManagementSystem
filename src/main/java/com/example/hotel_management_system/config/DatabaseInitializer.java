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

        String createServiceUsage = """
    CREATE TABLE NBP_SERVICE_USAGE (
        ID NUMBER PRIMARY KEY,
        STAY_ID NUMBER,
        SERVICE_ID NUMBER,
        QUANTITY NUMBER,
        USAGE_DATE DATE,
        TOTAL_PRICE NUMBER
    )
""";

        String createInvoice = """
    CREATE TABLE NBP_INVOICE (
        ID NUMBER PRIMARY KEY,
        ISSUE_DATE DATE,
        TOTAL_AMOUNT NUMBER,
        STATUS VARCHAR2(50),
        STAY_ID NUMBER
    )
""";
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
            if (!tableExists(conn, "NBP_SERVICE_USAGE")) {
                stmt.executeUpdate(createServiceUsage);
            }

            if (!tableExists(conn, "NBP_INVOICE")) {
                stmt.executeUpdate(createInvoice);
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
}
