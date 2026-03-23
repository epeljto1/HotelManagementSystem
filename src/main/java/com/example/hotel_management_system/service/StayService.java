package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.StayDTO;
import com.example.hotel_management_system.model.Stay;
import com.example.hotel_management_system.repository.StayRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StayService {

    private final StayRepository stayRepository;

    public StayService(StayRepository stayRepository) {
        this.stayRepository = stayRepository;
    }

    public StayDTO createStay(StayDTO stayDTO) throws SQLException {
        Stay stay = mapDTOToEntity(stayDTO);
        try (Connection connection = DbConfig.getConnection()) {
            stayRepository.save(stay, connection);
        }
        return stayDTO;
    }

    public StayDTO getStayById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Stay> stay = stayRepository.findById(id, connection);
            return stay.map(this::mapEntityToDTO).orElse(null);
        }
    }

    public List<StayDTO> getAllStays() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<Stay> stays = stayRepository.findAll(connection);
            return stays.stream()
                    .map(this::mapEntityToDTO)
                    .collect(Collectors.toList());
        }
    }

    public StayDTO updateStay(Long id, StayDTO stayDTO) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Stay> existing = stayRepository.findById(id, connection);
            if (existing.isPresent()) {
                Stay stay = mapDTOToEntity(stayDTO);
                stay.setId(id);
                stayRepository.update(stay, connection);
                return stayDTO;
            }
        }
        return null;
    }

    public boolean deleteStay(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Stay> stay = stayRepository.findById(id, connection);
            if (stay.isPresent()) {
                stayRepository.delete(id, connection);
                return true;
            }
        }
        return false;
    }

    private StayDTO mapEntityToDTO(Stay stay) {
        return new StayDTO(
                stay.getId(),
                stay.getCheckInTime(),
                stay.getCheckOutTime(),
                stay.getReservationId(),
                stay.getActualTotalPrice()
        );
    }

    private Stay mapDTOToEntity(StayDTO dto) {
        return new Stay(
                dto.getId(),
                dto.getCheckInTime(),
                dto.getCheckOutTime(),
                dto.getReservationId(),
                dto.getActualTotalPrice()
        );
    }
}
