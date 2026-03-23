package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.DiscountDTO;
import com.example.hotel_management_system.model.Discount;
import com.example.hotel_management_system.repository.DiscountRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DiscountService {
    private final DiscountRepository discountRepository;

    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    public DiscountDTO createDiscount(DiscountDTO discountDTO) throws SQLException {
        Discount discount = mapDTOToEntity(discountDTO);
        try (Connection connection = DbConfig.getConnection()) {
            discountRepository.save(discount, connection);
        }
        return discountDTO;
    }

    public DiscountDTO getDiscountById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Discount> discount = discountRepository.findById(id, connection);
            return discount.map(this::mapEntityToDTO).orElse(null);
        }
    }

    public List<DiscountDTO> getAllDiscounts() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<Discount> discounts = discountRepository.findAll(connection);
            return discounts.stream().map(this::mapEntityToDTO).collect(Collectors.toList());
        }
    }

    public DiscountDTO updateDiscount(Long id, DiscountDTO discountDTO) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Discount> existingDiscount = discountRepository.findById(id, connection);
            if (existingDiscount.isPresent()) {
                Discount discount = mapDTOToEntity(discountDTO);
                discount.setId(id);
                discountRepository.update(discount, connection);
                return discountDTO;
            }
        }
        return null;
    }

    public boolean deleteDiscount(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            if (discountRepository.findById(id, connection).isPresent()) {
                discountRepository.delete(id, connection);
                return true;
            }
        }
        return false;
    }

    private DiscountDTO mapEntityToDTO(Discount discount) {
        return new DiscountDTO(
                discount.getId(),
                discount.getName(),
                discount.getPercentage(),
                discount.getStartDate(),
                discount.getEndDate(),
                discount.getDescription()
        );
    }

    private Discount mapDTOToEntity(DiscountDTO dto) {
        return new Discount(
                dto.getId(),
                dto.getName(),
                dto.getPercentage(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getDescription()
        );
    }
}