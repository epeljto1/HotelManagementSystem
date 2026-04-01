package com.example.hotel_management_system.repository;

import com.example.hotel_management_system.model.Discount;
import org.springframework.stereotype.Repository;
import com.example.hotel_management_system.util.DatabaseLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class DiscountRepository {

    private final String INSERT_QUERY = """
            INSERT INTO NBP_DISCOUNT (ID, NAME, PERCENTAGE, START_DATE, END_DATE, DESCRIPTION)
            VALUES (NBP_DISCOUNT_SEQ.NEXTVAL, ?, ?, ?, ?, ?)
            """;

    private final String SELECT_ALL_QUERY = "SELECT * FROM NBP_DISCOUNT ORDER BY ID";

    private final String SELECT_BY_ID_QUERY = "SELECT * FROM NBP_DISCOUNT WHERE ID = ?";

    private final String UPDATE_QUERY = """
            UPDATE NBP_DISCOUNT
            SET NAME = ?, PERCENTAGE = ?, START_DATE = ?, END_DATE = ?, DESCRIPTION = ?
            WHERE ID = ?
            """;

    private final String DELETE_QUERY = "DELETE FROM NBP_DISCOUNT WHERE ID = ?";

    private final String FIND_ACTIVE_DISCOUNT_BY_DATE_QUERY = """
        SELECT *
        FROM NBP_DISCOUNT
        WHERE ? BETWEEN START_DATE AND END_DATE
        ORDER BY PERCENTAGE DESC, ID ASC
        FETCH FIRST 1 ROWS ONLY
        """;
    public void save(Discount discount, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(INSERT_QUERY)) {
            ps.setString(1, discount.getName());
            ps.setDouble(2, discount.getPercentage());
            ps.setDate(3, Date.valueOf(discount.getStartDate()));
            ps.setDate(4, Date.valueOf(discount.getEndDate()));
            ps.setString(5, discount.getDescription());
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "POST", "NBP_DISCOUNT");
        }
    }

    public List<Discount> findAll(Connection connection) throws SQLException {
        List<Discount> discounts = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(SELECT_ALL_QUERY);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                discounts.add(mapResultSetToDiscount(rs));
            }
        }
        return discounts;
    }

    public Optional<Discount> findById(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(SELECT_BY_ID_QUERY)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDiscount(rs));
                }
            }
        }
        return Optional.empty();
    }

    public void update(Discount discount, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY)) {
            ps.setString(1, discount.getName());
            ps.setDouble(2, discount.getPercentage());
            ps.setDate(3, Date.valueOf(discount.getStartDate()));
            ps.setDate(4, Date.valueOf(discount.getEndDate()));
            ps.setString(5, discount.getDescription());
            ps.setLong(6, discount.getId());
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "PUT", "NBP_DISCOUNT");
        }
    }

    public void delete(Long id, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(DELETE_QUERY)) {
            ps.setLong(1, id);
            ps.executeUpdate();

            //Logovanje akcije
            DatabaseLogger.log(connection, "DELETE", "NBP_DISCOUNT");
        }
    }

    private Discount mapResultSetToDiscount(ResultSet rs) throws SQLException {
        return new Discount(
                rs.getLong("ID"),
                rs.getString("NAME"),
                rs.getDouble("PERCENTAGE"),
                rs.getDate("START_DATE").toLocalDate(),
                rs.getDate("END_DATE").toLocalDate(),
                rs.getString("DESCRIPTION")
        );
    }
    public Optional<Discount> findActiveDiscountByDate(Date date, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(FIND_ACTIVE_DISCOUNT_BY_DATE_QUERY)) {
            ps.setDate(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToDiscount(rs));
                }
            }
        }
        return Optional.empty();
    }
}