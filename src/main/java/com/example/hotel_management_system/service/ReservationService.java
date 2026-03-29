package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.ReservationDTO;
import com.example.hotel_management_system.model.Reservation;
import com.example.hotel_management_system.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public ReservationDTO createReservation(ReservationDTO dto) throws SQLException {
        Reservation reservation = mapDTOToEntity(dto);

        try (Connection connection = DbConfig.getConnection()) {
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