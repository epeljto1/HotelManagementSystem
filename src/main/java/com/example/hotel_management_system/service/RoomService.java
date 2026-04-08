package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.RoomDTO;
import com.example.hotel_management_system.model.Room;
import com.example.hotel_management_system.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public RoomDTO createRoom(RoomDTO roomDTO) throws SQLException {
        Room room = mapDTOToEntity(roomDTO);

        try (Connection connection = DbConfig.getConnection()) {
            roomRepository.save(room, connection);

            return mapEntityToDTO(room);
        }
    }

    public RoomDTO getRoomById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            return roomRepository.findById(id, connection)
                    .map(this::mapEntityToDTO)
                    .orElse(null);
        }
    }

    public List<RoomDTO> getAllRooms() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            return roomRepository.findAll(connection).stream()
                    .map(this::mapEntityToDTO)
                    .collect(Collectors.toList());
        }
    }

    public RoomDTO updateRoom(Long id, RoomDTO roomDTO) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Room room = mapDTOToEntity(roomDTO);
            room.setId(id);

            roomRepository.update(room, connection);
            return mapEntityToDTO(room);
        }
    }
    public List<RoomDTO> getAvailableRooms(LocalDate from, LocalDate to) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {

            return roomRepository.findAvailableRooms(
                            connection,
                            Date.valueOf(from),
                            Date.valueOf(to)
                    ).stream()
                    .map(this::mapEntityToDTO)
                    .toList();
        }
    }

    public boolean deleteRoom(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            roomRepository.delete(id, connection);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private RoomDTO mapEntityToDTO(Room room) {
        return new RoomDTO(
                room.getId(),
                room.getRoomNumber(),
                room.getFloorNumber(),
                room.getStatus(),
                room.getHotelId(),
                room.getRoomTypeId()
        );
    }

    private Room mapDTOToEntity(RoomDTO roomDTO) {
        return new Room(
                roomDTO.getId(),
                roomDTO.getRoomNumber(),
                roomDTO.getFloorNumber(),
                roomDTO.getStatus(),
                roomDTO.getHotelId(),
                roomDTO.getRoomTypeId()
        );
    }
}