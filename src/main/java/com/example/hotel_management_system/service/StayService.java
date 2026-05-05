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

/**
 * Servisni sloj zadužen za upravljanje aktivnim boravcima gostiju u hotelu.
 * Glavna odgovornost ovog servisa je proces prijave (Check-In), praćenje trajanja
 * boravka i koordinacija između statusa soba i rezervacija.
 *
 * <p>Ova klasa osigurava integritet podataka koristeći manuelno upravljanje
 * SQL transakcijama kako bi se spriječili konflikti u bazi podataka.</p>
 *
 * @author Tvoje Ime
 * @version 1.0
 */
@Service
public class StayService {

    private final StayRepository stayRepository;
    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;

    /**
     * Konstruktor za Dependency Injection svih potrebnih repozitorija.
     */
    public StayService(StayRepository stayRepository,
                       ReservationRepository reservationRepository,
                       RoomRepository roomRepository) {
        this.stayRepository = stayRepository;
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
    }

    /**
     * Kreira novi zapis o boravku u bazi.
     * @param stayDTO Podaci o boravku.
     * @return StayDTO Kreirani objekt.
     * @throws SQLException SQL greška.
     */
    public StayDTO createStay(StayDTO stayDTO) throws SQLException {
        Stay stay = mapDTOToEntity(stayDTO);
        try (Connection connection = DbConfig.getConnection()) {
            stayRepository.save(stay, connection);
        }
        return stayDTO;
    }

    /**
     * Pronalazi boravak na osnovu ID-a.
     */
    public StayDTO getStayById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Stay> stay = stayRepository.findById(id, connection);
            return stay.map(this::mapEntityToDTO).orElse(null);
        }
    }

    /**
     * Vraća listu svih boravaka (aktivnih i završenih).
     */
    public List<StayDTO> getAllStays() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<Stay> stays = stayRepository.findAll(connection);
            return stays.stream()
                    .map(this::mapEntityToDTO)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Ažurira podatke o postojećem boravku.
     */
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

    /**
     * Briše zapis o boravku.
     */
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

    /**
     * Izvršava kompleksan Check-In proces.
     * <p>Proces je atomičan (koristi transakciju) i sastoji se od sljedećih validacija:</p>
     * <ul>
     *  <li>Provjera postojanja rezervacije i njenog statusa (ne smije biti otkazana ili završena).</li>
     *  <li>Provjera da li gost već ima kreiran boravak za tu rezervaciju.</li>
     *  <li>Provjera dostupnosti sobe (ne smije biti zauzeta ili van funkcije).</li>
     * </ul>
     * <p>Nakon validacije, metoda vrši tri akcije u bazi:</p>
     * 1. Kreira novi {@link Stay} objekt.<br>
     * 2. Postavlja status rezervacije na {@code CONFIRMED}.<br>
     * 3. Postavlja status sobe na {@code OCCUPIED}.
     *
     * @param checkInDTO Objekt koji sadrži ID rezervacije.
     * @throws SQLException U slučaju bilo kakve greške, vrši se <b>rollback</b> transakcije.
     */
    public void checkIn(CheckInDTO checkInDTO) throws SQLException {
        Connection connection = null;

        try {
            connection = DbConfig.getConnection();
            connection.setAutoCommit(false); // Početak transakcije

            // 1. Provjera rezervacije
            Optional<Reservation> reservationOptional =
                    reservationRepository.findById(checkInDTO.getReservationId(), connection);

            if (reservationOptional.isEmpty()) {
                throw new RuntimeException("Reservation not found.");
            }

            Reservation reservation = reservationOptional.get();

            // Validacija statusa rezervacije
            if (reservation.getStatus() == ReservationStatus.CANCELLED) {
                throw new RuntimeException("Cancelled reservation cannot be checked in.");
            }
            if (reservation.getStatus() == ReservationStatus.COMPLETED) {
                throw new RuntimeException("Completed reservation cannot be checked in.");
            }

            // 2. Provjera duplog Check-ina
            Optional<Stay> existingStay =
                    stayRepository.findByReservationId(checkInDTO.getReservationId(), connection);
            if (existingStay.isPresent()) {
                throw new RuntimeException("Stay already exists for this reservation.");
            }

            // 3. Provjera stanja sobe
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

            // 4. Kreiranje boravka
            Stay stay = new Stay();
            stay.setId(stayRepository.getNextId(connection));
            stay.setCheckInTime(LocalDateTime.now());
            stay.setCheckOutTime(null);
            stay.setReservationId(reservation.getId());
            stay.setActualTotalPrice(0.0);

            stayRepository.save(stay, connection);

            // 5. Ažuriranje statusa rezervacije u CONFIRMED
            if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
                reservationRepository.updateStatus(reservation.getId(), ReservationStatus.CONFIRMED, connection);
            }

            // 6. Ažuriranje sobe u OCCUPIED
            roomRepository.updateStatus(reservation.getRoomId(), RoomStatus.OCCUPIED, connection);

            connection.commit(); // Potvrda transakcije

        } catch (Exception e) {
            if (connection != null) {
                connection.rollback(); // Poništavanje transakcije u slučaju greške
            }
            throw new SQLException(e.getMessage(), e);
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Mapira entitet Stay u DTO objekt.
     */
    private StayDTO mapEntityToDTO(Stay stay) {
        return new StayDTO(
                stay.getId(),
                stay.getCheckInTime(),
                stay.getCheckOutTime(),
                stay.getReservationId(),
                stay.getActualTotalPrice()
        );
    }

    /**
     * Mapira DTO objekt u model entitet.
     */
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