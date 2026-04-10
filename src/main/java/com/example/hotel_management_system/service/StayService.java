package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.CheckInDTO;
import com.example.hotel_management_system.dto.StayDTO;
import com.example.hotel_management_system.enums.ReservationStatus;
import com.example.hotel_management_system.enums.RoomStatus;
import com.example.hotel_management_system.model.Reservation;
import com.example.hotel_management_system.model.Room;
import com.example.hotel_management_system.model.Stay;
import com.example.hotel_management_system.repository.ReservationRepository;
import com.example.hotel_management_system.repository.RoomRepository;
import com.example.hotel_management_system.repository.StayRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StayService {

    private final StayRepository stayRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public StayService(StayRepository stayRepository,
                       ReservationRepository reservationRepository,
                       RoomRepository roomRepository) {
        this.stayRepository = stayRepository;
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
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

    public void checkIn(CheckInDTO checkInDTO) throws SQLException {
        Connection connection = null;

        try {
            connection = DbConfig.getConnection();
            connection.setAutoCommit(false);

            Optional<Reservation> reservationOptional =
                    reservationRepository.findById(checkInDTO.getReservationId(), connection);

            if (reservationOptional.isEmpty()) {
                throw new RuntimeException("Reservation not found.");
            }

            Reservation reservation = reservationOptional.get();

            if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                throw new RuntimeException("Cancelled reservation cannot be checked in.");
            }

            if (reservation.getStatus() == ReservationStatus.COMPLETED) {
                throw new RuntimeException("Completed reservation cannot be checked in.");
            }

            Optional<Stay> existingStay =
                    stayRepository.findByReservationId(checkInDTO.getReservationId(), connection);

            if (existingStay.isPresent()) {
                throw new RuntimeException("Stay already exists for this reservation.");
            }

            Optional<Room> roomOptional = roomRepository.findById(reservation.getRoomId(), connection);

            if (roomOptional.isEmpty()) {
                throw new RuntimeException("Room not found.");
            }

            Room room = roomOptional.get();

            if (room.getStatus() == RoomStatus.OCCUPIED) {
                throw new RuntimeException("Room is already occupied.");
            }

            if (room.getStatus() == RoomStatus.OUT_OF_SERVICE) {
                throw new RuntimeException("Room is out of service.");
            }

            Stay stay = new Stay();
            stay.setId(stayRepository.getNextId(connection));
            stay.setCheckInTime(LocalDateTime.now());
            stay.setCheckOutTime(null);
            stay.setReservationId(reservation.getId());
            stay.setActualTotalPrice(0.0);

            stayRepository.save(stay, connection);

            if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
                reservationRepository.updateStatus(reservation.getId(), ReservationStatus.CONFIRMED, connection);
            }

            roomRepository.updateStatus(reservation.getRoomId(), RoomStatus.OCCUPIED, connection);

            connection.commit();

        } catch (Exception e) {
            if (connection != null) {
                connection.rollback();
            }
            throw new SQLException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
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