package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.HotelDTO;
import com.example.hotel_management_system.model.Hotel;
import com.example.hotel_management_system.repository.HotelRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servisni sloj zadužen za upravljanje osnovnim informacijama o hotelu.
 * Pruža administrativne funkcionalnosti za definisanje profila hotela,
 * uključujući naziv, kontakt podatke i lokaciju (povezanu preko adrese).
 * * <p>Ovaj servis služi kao centralna tačka za konfiguraciju brendiranja sistema
 * i osnovnih kontakt informacija koje se mogu pojaviti na računima i izveštajima.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Service
public class HotelService {
    private final HotelRepository hotelRepository;

    /**
     * Konstruktor za Dependency Injection HotelRepository-ja.
     * * @param hotelRepository Repozitorij za perzistenciju podataka o hotelima.
     */
    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    /**
     * Kreira novi zapis o hotelu u bazi podataka.
     * * @param hotelDTO Podaci o hotelu (naziv, kontakt, adresa).
     * @return HotelDTO Prosleđeni podaci nakon uspešnog čuvanja.
     * @throws SQLException U slučaju greške u komunikaciji sa Oracle bazom podataka.
     */
    public HotelDTO createHotel(HotelDTO hotelDTO) throws SQLException {
        Hotel hotel = mapDTOToEntity(hotelDTO);
        try (Connection connection = DbConfig.getConnection()) {
            hotelRepository.save(hotel, connection);
        }
        return hotelDTO;
    }

    /**
     * Pronalazi hotel na osnovu njegovog ID-a.
     * * @param id Jedinstveni identifikator hotela.
     * @return HotelDTO Podaci o hotelu ili null ako hotel sa tim ID-om ne postoji.
     * @throws SQLException U slučaju greške prilikom izvršavanja SQL upita.
     */
    public HotelDTO getHotelById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Hotel> hotel = hotelRepository.findById(id, connection);
            return hotel.map(this::mapEntityToDTO).orElse(null);
        }
    }

    /**
     * Dobavlja listu svih registrovanih hotela u sistemu.
     * * @return List<HotelDTO> Lista svih hotela mapirana u DTO objekte.
     * @throws SQLException U slučaju greške pri čitanju podataka iz baze.
     */
    public List<HotelDTO> getAllHotels() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<Hotel> hotels = hotelRepository.findAll(connection);
            return hotels.stream()
                    .map(this::mapEntityToDTO)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Ažurira postojeće informacije o hotelu.
     * * @param id ID hotela koji se ažurira.
     * @param hotelDTO Novi podaci o hotelu.
     * @return HotelDTO Ažurirani podaci ili null ako hotel nije pronađen u bazi.
     * @throws SQLException U slučaju greške prilikom ažuriranja podataka.
     */
    public HotelDTO updateHotel(Long id, HotelDTO hotelDTO) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Hotel> existingHotel = hotelRepository.findById(id, connection);
            if (existingHotel.isPresent()) {
                Hotel hotel = mapDTOToEntity(hotelDTO);
                hotel.setId(id);
                hotelRepository.update(hotel, connection);
                return hotelDTO;
            }
        }
        return null;
    }

    /**
     * Briše zapis o hotelu iz sistema na osnovu ID-a.
     * * @param id ID hotela za brisanje.
     * @return boolean True ako je brisanje uspešno, false ako hotel nije pronađen.
     * @throws SQLException U slučaju greške prilikom brisanja zapisa.
     */
    public boolean deleteHotel(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Hotel> hotel = hotelRepository.findById(id, connection);
            if (hotel.isPresent()) {
                hotelRepository.delete(id, connection);
                return true;
            }
        }
        return false;
    }

    /**
     * Pretvara entitet Hotel u DTO objekt za prenos podataka.
     * * @param hotel Entitet iz baze.
     * @return HotelDTO Objekt pogodan za prikaz na UI-u.
     */
    private HotelDTO mapEntityToDTO(Hotel hotel) {
        return new HotelDTO(
                hotel.getId(),
                hotel.getName(),
                hotel.getDescription(),
                hotel.getPhoneNumber(),
                hotel.getEmail(),
                hotel.getAddressId()
        );
    }

    /**
     * Pretvara DTO objekt u entitet Hotel pogodan za rad sa repozitorijumom.
     * * @param hotelDTO Podaci primljeni preko API-ja.
     * @return Hotel Entitet spreman za bazu podataka.
     */
    private Hotel mapDTOToEntity(HotelDTO hotelDTO) {
        return new Hotel(
                hotelDTO.getId(),
                hotelDTO.getName(),
                hotelDTO.getDescription(),
                hotelDTO.getPhoneNumber(),
                hotelDTO.getEmail(),
                hotelDTO.getAddressId()
        );
    }
}