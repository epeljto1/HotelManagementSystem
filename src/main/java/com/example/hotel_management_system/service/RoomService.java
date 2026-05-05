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

/**
 * Servisni sloj zadužen za upravljanje fizičkim jedinicama (sobama) unutar hotela.
 * Omogućava manipulaciju podacima o sobama, evidenciju njihovih brojeva i spratnosti,
 * te ključnu funkcionalnost pretrage slobodnih kapaciteta za zadati vremenski period.
 *
 * @author Tvoje Ime
 * @version 1.0
 */
@Service
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Kreira novu sobu u sistemu.
     */
    public RoomDTO createRoom(RoomDTO roomDTO) throws SQLException {
        Room room = mapDTOToEntity(roomDTO);

        try (Connection connection = DbConfig.getConnection()) {
            roomRepository.save(room, connection);

            return mapEntityToDTO(room);
        }
    }

    /**
     * Pronalazi sobu na osnovu njenog ID-a.
     */
    public RoomDTO getRoomById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            return roomRepository.findById(id, connection)
                    .map(this::mapEntityToDTO)
                    .orElse(null);
        }
    }

    /**
     * Dobavlja sve sobe iz baze podataka.
     */
    public List<RoomDTO> getAllRooms() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            // Osiguravamo da findAll(connection) vrati List<Room>
            List<Room> rooms = roomRepository.findAll(connection);

            return rooms.stream()
                    .map(this::mapEntityToDTO)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Ažurira osnovne informacije o sobi.
     */
    public RoomDTO updateRoom(Long id, RoomDTO roomDTO) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Room room = mapDTOToEntity(roomDTO);
            room.setId(id);

            roomRepository.update(room, connection);
            return mapEntityToDTO(room);
        }
    }

    /**
     * Vraća listu slobodnih soba za traženi period.
     */
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

    /**
     * Briše sobu iz sistema.
     */
    public boolean deleteRoom(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            roomRepository.delete(id, connection);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Ažurira sliku sobe u bazi podataka.
     */
    public void updateRoomImage(Long id, byte[] image) throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            Room room = roomRepository.findById(id, conn)
                    .orElseThrow(() -> new RuntimeException("Room not found"));

            room.setImage(image);

            roomRepository.update(room, conn);
        }
    }

    /**
     * Mapira entitet Room u RoomDTO.
     */
    private RoomDTO mapEntityToDTO(Room room) {
        return new RoomDTO(
                room.getId(),
                room.getRoomNumber(),
                room.getFloorNumber(),
                room.getStatus(),
                room.getHotelId(),
                room.getRoomTypeId(),
                room.getImage()
        );
    }

    /**
     * Mapira RoomDTO u entitet Room.
     */
    private Room mapDTOToEntity(RoomDTO roomDTO) {
        return new Room(
                roomDTO.getId(),
                roomDTO.getRoomNumber(),
                roomDTO.getFloorNumber(),
                roomDTO.getStatus(),
                roomDTO.getHotelId(),
                roomDTO.getRoomTypeId(),
                roomDTO.getImage()
        );
    }
}