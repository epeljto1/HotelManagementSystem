package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.RoomTypeDTO;
import com.example.hotel_management_system.model.RoomType;
import com.example.hotel_management_system.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    public RoomTypeService(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    public RoomTypeDTO createRoomType(RoomTypeDTO roomTypeDTO) throws SQLException {
        RoomType roomType = mapDTOToEntity(roomTypeDTO);
        try (Connection connection = DbConfig.getConnection()) {
            roomTypeRepository.save(roomType, connection);
        }
        return roomTypeDTO;
    }

    public RoomTypeDTO getRoomTypeById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<RoomType> roomType = roomTypeRepository.findById(id, connection);
            return roomType.map(this::mapEntityToDTO).orElse(null);
        }
    }

    public List<RoomTypeDTO> getAllRoomTypes() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<RoomType> roomTypes = roomTypeRepository.findAll(connection);
            return roomTypes.stream().map(this::mapEntityToDTO).collect(Collectors.toList());
        }
    }

    public RoomTypeDTO updateRoomType(Long id, RoomTypeDTO roomTypeDTO) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<RoomType> existingRoomType = roomTypeRepository.findById(id, connection);
            if (existingRoomType.isPresent()) {
                RoomType roomType = mapDTOToEntity(roomTypeDTO);
                roomType.setId(id);
                roomTypeRepository.update(roomType, connection);
                return roomTypeDTO;
            }
        }
        return null;
    }

    public boolean deleteRoomType(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<RoomType> existingRoomType = roomTypeRepository.findById(id, connection);
            if (existingRoomType.isPresent()) {
                roomTypeRepository.delete(id, connection);
                return true;
            }
        }
        return false;
    }

    private RoomTypeDTO mapEntityToDTO(RoomType roomType) {
        return new RoomTypeDTO(
                roomType.getId(),
                roomType.getName(),
                roomType.getDescription(),
                roomType.getCapacity(),
                roomType.getPricePerNight()
        );
    }

    private RoomType mapDTOToEntity(RoomTypeDTO roomTypeDTO) {
        return new RoomType(
                roomTypeDTO.getId(),
                roomTypeDTO.getName(),
                roomTypeDTO.getDescription(),
                roomTypeDTO.getCapacity(),
                roomTypeDTO.getPricePerNight()
        );
    }
}