package com.example.hotel_management_system.config;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseInitializer {

    public DatabaseInitializer() throws SQLException {
    }

    public static void initialize() throws SQLException {
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

        String createPayment = """
            CREATE TABLE NBP_PAYMENT (
                ID NUMBER PRIMARY KEY,
                PAYMENT_DATE TIMESTAMP,
                AMOUNT NUMBER,
                PAYMENT_METHOD VARCHAR2(50),
                INVOICE_ID NUMBER
            )
        """;
        String createRoomType = """
           CREATE TABLE NBP_ROOM_TYPE (
             ID NUMBER PRIMARY KEY,
             NAME VARCHAR2(100),
             DESCRIPTION VARCHAR2(500),
             CAPACITY NUMBER,
             PRICE_PER_NIGHT NUMBER
           )
        """;

        String createPaymentSeq = "CREATE SEQUENCE NBP_PAYMENT_SEQ START WITH 1 INCREMENT BY 1";

        String createDiscount = """
            CREATE TABLE NBP_DISCOUNT (
                ID NUMBER PRIMARY KEY,
                NAME VARCHAR2(100),
                PERCENTAGE NUMBER,
                START_DATE DATE,
                END_DATE DATE,
                DESCRIPTION VARCHAR2(500)
            )
        """;

        String createDiscountSeq = "CREATE SEQUENCE NBP_DISCOUNT_SEQ START WITH 1 INCREMENT BY 1";

        String createRoom = """
                    CREATE TABLE NBP_ROOM (
                        ID NUMBER PRIMARY KEY,
                        ROOM_NUMBER VARCHAR2(20),
                        FLOOR_NUMBER NUMBER,
                        STATUS VARCHAR2(50) DEFAULT 'AVAILABLE',
                        HOTEL_ID NUMBER NOT NULL,
                        ROOM_TYPE_ID NUMBER NOT NULL,
                        CONSTRAINT CHK_ROOM_STATUS
                            CHECK (STATUS IN ('AVAILABLE', 'OCCUPIED', 'RESERVED', 'OUT_OF_SERVICE')),
                        CONSTRAINT FK_ROOM_HOTEL
                            FOREIGN KEY (HOTEL_ID)
                            REFERENCES NBP_HOTEL(ID),
                        CONSTRAINT FK_ROOM_ROOM_TYPE
                            FOREIGN KEY (ROOM_TYPE_ID)
                            REFERENCES NBP_ROOM_TYPE(ID)
                    )
                """;

        String createRoomSeq = "CREATE SEQUENCE NBP_ROOM_SEQ START WITH 3 INCREMENT BY 1";


        try {
            Connection conn = DbConfig.getConnection();
            Statement stmt = conn.createStatement();

            if (!tableExists(conn, "NBP_ADDRESS")) stmt.executeUpdate(createAddress);
            if (!tableExists(conn, "NBP_HOTEL")) stmt.executeUpdate(createHotel);
            if (!tableExists(conn, "NBP_GUEST")) stmt.executeUpdate(createGuest);
            if (!tableExists(conn, "NBP_SERVICE_USAGE")) stmt.executeUpdate(createServiceUsage);
            if (!tableExists(conn, "NBP_INVOICE")) stmt.executeUpdate(createInvoice);
            if (!tableExists(conn, "NBP_ROOM_TYPE")) stmt.executeUpdate(createRoomType);

            if (!tableExists(conn, "NBP_PAYMENT")) {
                stmt.executeUpdate(createPayment);
                System.out.println("Table NBP_PAYMENT created.");
            }

            if (!sequenceExists(conn, "NBP_PAYMENT_SEQ")) {
                stmt.executeUpdate(createPaymentSeq);
                System.out.println("Sequence NBP_PAYMENT_SEQ created.");
            }
            if (!tableExists(conn, "NBP_DISCOUNT")) {
                stmt.executeUpdate(createDiscount);
                System.out.println("Table NBP_DISCOUNT created.");
            }

            if (!sequenceExists(conn, "NBP_DISCOUNT_SEQ")) {
                stmt.executeUpdate(createDiscountSeq);
                System.out.println("Sequence NBP_DISCOUNT_SEQ created.");
            }

            if (!tableExists(conn, "NBP_ROOM")) {
                stmt.executeUpdate(createRoom);
                System.out.println("Table NBP_ROOM created.");
            }

            if (!sequenceExists(conn, "NBP_ROOM_SEQ")) {
                stmt.executeUpdate(createRoomSeq);
                System.out.println("Sequence NBP_ROOM_SEQ created.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static boolean tableExists(Connection conn, String tableName) {
        String query = "SELECT COUNT(*) FROM user_tables WHERE table_name = ?";
        try (var ps = conn.prepareStatement(query)) {
            ps.setString(1, tableName.toUpperCase());
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