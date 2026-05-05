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
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servisni sloj zadužen za upravljanje rezervacijama soba.
 * Centralna logika ovog servisa uključuje provjeru dostupnosti soba za specifične periode,
 * kreiranje novih rezervacija, te ažuriranje statusa postojećih.
 * * <p>Ovaj servis osigurava da jedna soba ne može biti rezervisana od strane više gostiju
 * u preklapajućim terminima.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Service
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    /**
     * Konstruktor za Dependency Injection.
     * * @param reservationRepository Repozitorij za rad sa rezervacijama.
     * @param roomRepository Repozitorij za rad sa podacima o sobama.
     */
    public ReservationService(ReservationRepository reservationRepository, RoomRepository roomRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    /**
     * Kreira novu rezervaciju uz prethodnu validaciju dostupnosti sobe.
     * * <p>Proces validacije:</p>
     * 1. Konvertuje ulazne datume u SQL format.<br>
     * 2. Poziva {@link RoomRepository#findAvailableRooms} da dobije listu slobodnih soba za taj period.<br>
     * 3. Provjerava da li se tražena soba nalazi na listi dostupnih.<br>
     * 4. Postavlja inicijalni status {@link ReservationStatus#PENDING} ako status nije proslijeđen.
     * * @param dto Podaci o željenoj rezervaciji (ID sobe, datumi, broj gostiju).
     * @return ReservationDTO Podaci o uspješno kreiranoj rezervaciji.
     * @throws SQLException U slučaju greške sa bazom podataka.
     * @throws RuntimeException Ako soba nije dostupna za odabrani period.
     */
    public ReservationDTO createReservation(ReservationDTO dto) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {

            java.sql.Date checkIn = new java.sql.Date(dto.getCheckInDate().getTime());
            java.sql.Date checkOut = new java.sql.Date(dto.getCheckOutDate().getTime());

            // ISPRAVLJENO: Koristimo 'roomRepository' (malo r), ne klasu direktno
            List<Room> availableRooms = roomRepository.findAvailableRooms(
                    connection,
                    checkIn,
                    checkOut
            );

            // Provjera dostupnosti pomoću streama
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

    /**
     * Pronalazi rezervaciju na osnovu ID-a.
     * * @param id Identifikator rezervacije.
     * @return ReservationDTO ili null ako nije pronađena.
     * @throws SQLException SQL greška.
     */
    public ReservationDTO getReservationById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            return reservationRepository.findById(id, connection)
                    .map(this::mapEntityToDTO)
                    .orElse(null);
        }
    }

    /**
     * Vraća listu svih rezervacija u sistemu.
     * * @return List<ReservationDTO>
     * @throws SQLException SQL greška.
     */
    public List<ReservationDTO> getAllReservations() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            return reservationRepository.findAll(connection).stream()
                    .map(this::mapEntityToDTO)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Ažurira postojeću rezervaciju.
     * * @param id ID rezervacije.
     * @param dto Novi podaci za ažuriranje.
     * @return ReservationDTO Ažurirani podaci.
     * @throws SQLException SQL greška.
     */
    public ReservationDTO updateReservation(Long id, ReservationDTO dto) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Reservation reservation = mapDTOToEntity(dto);
            reservation.setId(id);

            reservationRepository.update(reservation, connection);
            return mapEntityToDTO(reservation);
        }
    }

    /**
     * Briše rezervaciju iz evidencije.
     * * @param id ID rezervacije za brisanje.
     * @return boolean True ako je brisanje uspješno.
     * @throws SQLException SQL greška.
     */
    public boolean deleteReservation(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            reservationRepository.delete(id, connection);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Pomoćna metoda za mapiranje entiteta Reservation u DTO.
     */
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

    /**
     * Pomoćna metoda za mapiranje DTO-a u entitet Reservation.
     */
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