package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.ExtraServiceDTO;
import com.example.hotel_management_system.model.ExtraService;
import com.example.hotel_management_system.repository.ExtraServiceRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ExtraServiceService {

    private final ExtraServiceRepository extraServiceRepository;

    public ExtraServiceService(ExtraServiceRepository extraServiceRepository) {
        this.extraServiceRepository = extraServiceRepository;
    }

    public ExtraServiceDTO createExtraService(ExtraServiceDTO dto) throws SQLException {
        ExtraService entity = mapDTOToEntity(dto);
        try (Connection connection = DbConfig.getConnection()) {
            extraServiceRepository.save(entity, connection);
        }
        return dto;
    }

    public ExtraServiceDTO getExtraServiceById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<ExtraService> entity = extraServiceRepository.findById(id, connection);
            return entity.map(this::mapEntityToDTO).orElse(null);
        }
    }

    public List<ExtraServiceDTO> getAllExtraServices() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<ExtraService> list = extraServiceRepository.findAll(connection);
            return list.stream()
                    .map(this::mapEntityToDTO)
                    .collect(Collectors.toList());
        }
    }

    public ExtraServiceDTO updateExtraService(Long id, ExtraServiceDTO dto) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<ExtraService> existing = extraServiceRepository.findById(id, connection);
            if (existing.isPresent()) {
                ExtraService entity = mapDTOToEntity(dto);
                entity.setId(id);
                extraServiceRepository.update(entity, connection);
                return dto;
            }
        }
        return null;
    }

    public boolean deleteExtraService(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<ExtraService> existing = extraServiceRepository.findById(id, connection);
            if (existing.isPresent()) {
                extraServiceRepository.delete(id, connection);
                return true;
            }
        }
        return false;
    }

    private ExtraServiceDTO mapEntityToDTO(ExtraService entity) {
        return new ExtraServiceDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getUnitPrice(),
                entity.getAvailable()
        );
    }

    private ExtraService mapDTOToEntity(ExtraServiceDTO dto) {
        return new ExtraService(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getUnitPrice(),
                dto.getAvailable()
        );
    }
}