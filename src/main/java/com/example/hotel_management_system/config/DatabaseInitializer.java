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

        String createUser = """
            CREATE TABLE NBP_USER (
                ID NUMBER PRIMARY KEY,
                USER_ID NUMBER NOT NULL,
                ROLE_ID NUMBER,
                USERNAME VARCHAR2(255),
                EMAIL VARCHAR2(255),
                PASSWORD_HASH VARCHAR2(255),
                ROLE VARCHAR2(50),
                CREATED_DATE DATE,
                CONSTRAINT FK_NBPT7_USER_USER
                    FOREIGN KEY (USER_ID)
                    REFERENCES NBP.NBP_USER(ID),
                CONSTRAINT FK_NBPT7_USER_ROLE
                    FOREIGN KEY (ROLE_ID)
                    REFERENCES NBP.NBP_ROLE(ID)
            )
        """;

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

        String createReservation = """
            CREATE TABLE NBP_RESERVATION (
                ID NUMBER PRIMARY KEY,
                RESERVATION_DATE TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                CHECK_IN_DATE TIMESTAMP NOT NULL,
                CHECK_OUT_DATE TIMESTAMP NOT NULL,
                NUMBER_OF_GUESTS NUMBER NOT NULL,
                STATUS VARCHAR2(50) DEFAULT 'PENDING',
                TOTAL_PRICE NUMBER(10, 2),
                GUEST_ID NUMBER NOT NULL,
                ROOM_ID NUMBER NOT NULL,
                CREATED_BY NUMBER NOT NULL,
                
                CONSTRAINT CHK_RESERVATION_STATUS
                    CHECK (STATUS IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'COMPLETED')),
                    
                CONSTRAINT FK_RESERVATION_GUEST
                    FOREIGN KEY (GUEST_ID)
                    REFERENCES NBP_GUEST(ID),
                    
                CONSTRAINT FK_RESERVATION_ROOM
                    FOREIGN KEY (ROOM_ID)
                    REFERENCES NBP_ROOM(ID),
                    
                CONSTRAINT FK_RESERVATION_USER
                    FOREIGN KEY (CREATED_BY)
                    REFERENCES NBP_USER(ID)
            )
        """;

        String createLog = """
            CREATE TABLE NBP_LOG (
                ID NUMBER PRIMARY KEY,
                ACTION_NAME VARCHAR2(255) NOT NULL,
                TABLE_NAME VARCHAR2(255) NOT NULL,
                DATE_TIME TIMESTAMP NOT NULL,
                DB_USER VARCHAR2(255)
            )
        """;

        String createPaymentSeq = "CREATE SEQUENCE NBP_PAYMENT_SEQ START WITH 1 INCREMENT BY 1";
        String createDiscountSeq = "CREATE SEQUENCE NBP_DISCOUNT_SEQ START WITH 1 INCREMENT BY 1";
        String createLogSeq = "CREATE SEQUENCE NBP_LOG_SEQ START WITH 1 INCREMENT BY 1";
        String createRoomSeq = "CREATE SEQUENCE NBP_ROOM_SEQ START WITH 1 INCREMENT BY 1";
        String createReservationSeq = "CREATE SEQUENCE NBP_RESERVATION_SEQ START WITH 1 INCREMENT BY 1";
        String createUserSeq = "CREATE SEQUENCE NBP_USER_SEQ START WITH 7 INCREMENT BY 1";

        String createGuestLogTrigger = """
            CREATE OR REPLACE TRIGGER TRG_NBP_GUEST_LOG
            AFTER INSERT OR UPDATE OR DELETE ON NBP_GUEST
            FOR EACH ROW
            BEGIN
                IF INSERTING THEN
                    INSERT INTO NBP_LOG (ID, ACTION_NAME, TABLE_NAME, DATE_TIME, DB_USER)
                    VALUES (NBP_LOG_SEQ.NEXTVAL, 'POST', 'NBP_GUEST', SYSTIMESTAMP, USER);
                ELSIF UPDATING THEN
                    INSERT INTO NBP_LOG (ID, ACTION_NAME, TABLE_NAME, DATE_TIME, DB_USER)
                    VALUES (NBP_LOG_SEQ.NEXTVAL, 'PUT', 'NBP_GUEST', SYSTIMESTAMP, USER);
                ELSIF DELETING THEN
                    INSERT INTO NBP_LOG (ID, ACTION_NAME, TABLE_NAME, DATE_TIME, DB_USER)
                    VALUES (NBP_LOG_SEQ.NEXTVAL, 'DELETE', 'NBP_GUEST', SYSTIMESTAMP, USER);
                END IF;
            END;
        """;

        String createRoomTypeLogTrigger = """
            CREATE OR REPLACE TRIGGER TRG_NBP_ROOM_TYPE_LOG
            AFTER INSERT OR UPDATE OR DELETE ON NBP_ROOM_TYPE
            FOR EACH ROW
            BEGIN
                IF INSERTING THEN
                    INSERT INTO NBP_LOG (ID, ACTION_NAME, TABLE_NAME, DATE_TIME, DB_USER)
                    VALUES (NBP_LOG_SEQ.NEXTVAL, 'POST', 'NBP_ROOM_TYPE', SYSTIMESTAMP, USER);
                ELSIF UPDATING THEN
                    INSERT INTO NBP_LOG (ID, ACTION_NAME, TABLE_NAME, DATE_TIME, DB_USER)
                    VALUES (NBP_LOG_SEQ.NEXTVAL, 'PUT', 'NBP_ROOM_TYPE', SYSTIMESTAMP, USER);
                ELSIF DELETING THEN
                    INSERT INTO NBP_LOG (ID, ACTION_NAME, TABLE_NAME, DATE_TIME, DB_USER)
                    VALUES (NBP_LOG_SEQ.NEXTVAL, 'DELETE', 'NBP_ROOM_TYPE', SYSTIMESTAMP, USER);
                END IF;
            END;
        """;



        String createHotelLogTrigger = """
           CREATE OR REPLACE TRIGGER TRG_NBP_HOTEL_LOG
           AFTER INSERT OR UPDATE OR DELETE ON NBP_HOTEL
           FOR EACH ROW
           BEGIN
              IF INSERTING THEN
                INSERT INTO NBP_LOG (ID, ACTION_NAME, TABLE_NAME, DATE_TIME, DB_USER)
                VALUES (NBP_LOG_SEQ.NEXTVAL, 'POST', 'NBP_HOTEL', SYSTIMESTAMP, USER);
              ELSIF UPDATING THEN
                 INSERT INTO NBP_LOG (ID, ACTION_NAME, TABLE_NAME, DATE_TIME, DB_USER)
                 VALUES (NBP_LOG_SEQ.NEXTVAL, 'PUT', 'NBP_HOTEL', SYSTIMESTAMP, USER);
              ELSIF DELETING THEN
                 INSERT INTO NBP_LOG (ID, ACTION_NAME, TABLE_NAME, DATE_TIME, DB_USER)
                 VALUES (NBP_LOG_SEQ.NEXTVAL, 'DELETE', 'NBP_HOTEL', SYSTIMESTAMP, USER);
              END IF;
           END;
        """;



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

            if (!tableExists(conn, "NBP_USER")) {
                stmt.executeUpdate(createUser);
                System.out.println("Table NBP_USER created.");
            }

            if (!sequenceExists(conn, "NBP_USER_SEQ")) {
                stmt.executeUpdate(createUserSeq);
                System.out.println("Sequence NBP_USER_SEQ created.");
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

            if (!tableExists(conn, "NBP_RESERVATION")) {
                stmt.executeUpdate(createReservation);
                System.out.println("Table NBP_RESERVATION created.");
            }

            if (!sequenceExists(conn, "NBP_RESERVATION_SEQ")) {
                stmt.executeUpdate(createReservationSeq);
                System.out.println("Sequence NBP_RESERVATION_SEQ created.");
            }

            if (!tableExists(conn, "NBP_LOG")) {
                stmt.executeUpdate(createLog);
                System.out.println("Table NBP_LOG created.");
            }

            if (!sequenceExists(conn, "NBP_LOG_SEQ")) {
                stmt.executeUpdate(createLogSeq);
                System.out.println("Sequence NBP_LOG_SEQ created.");
            }

            stmt.execute(createGuestLogTrigger);
            System.out.println("Trigger TRG_NBP_GUEST_LOG created or replaced.");

            stmt.execute(createRoomTypeLogTrigger);
            System.out.println("Trigger TRG_NBP_ROOM_TYPE_LOG created or replaced.");


            stmt.execute(createHotelLogTrigger);
            System.out.println("Trigger TRG_NBP_HOTEL_LOG created or replaced.");

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