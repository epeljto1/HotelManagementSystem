package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.ReservationDTO;
import com.example.hotel_management_system.enums.ReservationStatus;
import com.example.hotel_management_system.model.Reservation;
import com.example.hotel_management_system.model.Room;
import com.example.hotel_management_system.repository.ReservationRepository;
import com.example.hotel_management_system.repository.RoomRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }
/*
    public ReservationDTO createReservation(ReservationDTO dto) throws SQLException {
        Reservation reservation = mapDTOToEntity(dto);

        try (Connection connection = DbConfig.getConnection()) {
            reservationRepository.save(reservation, connection);

            return mapEntityToDTO(reservation);
        }
    }*/
public ReservationDTO createReservation(ReservationDTO dto) throws SQLException {

    try (Connection connection = DbConfig.getConnection()) {

        java.sql.Date checkIn = new java.sql.Date(dto.getCheckInDate().getTime());
        java.sql.Date checkOut = new java.sql.Date(dto.getCheckOutDate().getTime());


        List<Room> availableRooms = RoomRepository.findAvailableRooms(
                connection,
                checkIn,
                checkOut
        );

        boolean isAvailable = availableRooms.stream()
                .anyMatch(room -> room.getId().equals(dto.getRoomId()));

        if (!isAvailable) {
            throw new RuntimeException("Soba nije dostupna za odabrani period!");
        }

        if (dto.getStatus() == null) {
            dto.setStatus(ReservationStatus.PENDING);
        }

        Reservation reservation = mapDTOToEntity(dto);

        if (reservation.getReservationDate() == null) {
            reservation.setReservationDate(new java.util.Date());
        }
        reservationRepository.save(reservation, connection);

        return mapEntityToDTO(reservation);
    }
}

    public ReservationDTO getReservationById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            return reservationRepository.findById(id, connection)
                    .map(this::mapEntityToDTO)
                    .orElse(null);
        }
    }

    public List<ReservationDTO> getAllReservations() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            return reservationRepository.findAll(connection).stream()
                    .map(this::mapEntityToDTO)
                    .collect(Collectors.toList());
        }
    }

    public ReservationDTO updateReservation(Long id, ReservationDTO dto) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Reservation reservation = mapDTOToEntity(dto);
            reservation.setId(id);

            reservationRepository.update(reservation, connection);
            return mapEntityToDTO(reservation);
        }
    }

    public boolean deleteReservation(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            reservationRepository.delete(id, connection);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private ReservationDTO mapEntityToDTO(Reservation res) {
        return new ReservationDTO(
                res.getId(),
                res.getReservationDate(),
                res.getCheckInDate(),
                res.getCheckOutDate(),
                res.getNumberOfGuests(),
                res.getStatus(),
                res.getTotalPrice(),
                res.getGuestId(),
                res.getRoomId(),
                res.getCreatedBy()
        );
    }

    private Reservation mapDTOToEntity(ReservationDTO dto) {
        return new Reservation(
                dto.getId(),
                dto.getReservationDate(),
                dto.getCheckInDate(),
                dto.getCheckOutDate(),
                dto.getNumberOfGuests(),
                dto.getStatus(),
                dto.getTotalPrice(),
                dto.getGuestId(),
                dto.getRoomId(),
                dto.getCreatedBy()
        );
    }
}