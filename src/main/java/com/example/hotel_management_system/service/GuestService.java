package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.GuestDTO;
import com.example.hotel_management_system.model.Guest;
import com.example.hotel_management_system.repository.GuestRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servisni sloj zadužen za upravljanje podacima o gostima hotela.
 * Pruža funkcionalnosti za registraciju novih gostiju, ažuriranje postojećih
 * ličnih podataka, te pretragu i brisanje iz evidencije.
 * * <p>Ovaj servis je ključan za module rezervacija i check-out-a jer povezuje
 * fizička lica sa njihovim boravcima u hotelu.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Service
public class GuestService {
    private final GuestRepository guestRepository;

    /**
     * Konstruktor za ubrizgavanje GuestRepository zavisnosti.
     * * @param guestRepository Repozitorij za perzistenciju podataka o gostima.
     */
    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    /**
     * Registruje novog gosta u bazu podataka.
     * * @param guestDTO Objekt koji sadrži lične podatke, kontakt i broj dokumenta gosta.
     * @return GuestDTO Vraća podatke o kreiranom gostu.
     * @throws SQLException U slučaju greške pri upisu u bazu podataka.
     */
    public GuestDTO createGuest(GuestDTO guestDTO) throws SQLException {
        Guest guest = mapDTOToEntity(guestDTO);
        try (Connection connection = DbConfig.getConnection()) {
            guestRepository.save(guest, connection);
        }
        return guestDTO;
    }

    /**
     * Pronalazi gosta na osnovu njegovog jedinstvenog identifikatora (ID).
     * * @param id ID gosta koji se traži.
     * @return GuestDTO Podaci o gostu ili null ukoliko gost nije pronađen.
     * @throws SQLException U slučaju greške pri izvršavanju SQL upita.
     */
    public GuestDTO getGuestById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Guest> guest = guestRepository.findById(id, connection);
            return guest.map(this::mapEntityToDTO).orElse(null);
        }
    }

    /**
     * Vraća kompletnu listu svih registrovanih gostiju.
     * * @return List<GuestDTO> Lista svih gostiju mapirana u DTO formate.
     * @throws SQLException U slučaju greške pri čitanju iz baze.
     */
    public List<GuestDTO> getAllGuests() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<Guest> guests = guestRepository.findAll(connection);
            return guests.stream().map(this::mapEntityToDTO).collect(Collectors.toList());
        }
    }

    /**
     * Ažurira podatke o postojećem gostu (npr. promjena broja telefona ili adrese).
     * * @param id ID gosta čiji se podaci mijenjaju.
     * @param guestDTO Novi podaci za ažuriranje.
     * @return GuestDTO Ažurirani podaci ili null ako gost sa navedenim ID-om ne postoji.
     * @throws SQLException U slučaju greške pri komunikaciji sa bazom.
     */
    public GuestDTO updateGuest(Long id, GuestDTO guestDTO) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Guest> existingGuest = guestRepository.findById(id, connection);
            if (existingGuest.isPresent()) {
                Guest guest = mapDTOToEntity(guestDTO);
                guest.setId(id);
                guestRepository.update(guest, connection);
                return guestDTO;
            }
        }
        return null;
    }

    /**
     * Briše gosta iz baze podataka na osnovu ID-a.
     * * @param id Jedinstveni identifikator gosta za brisanje.
     * @return boolean True ako je brisanje uspješno, false ako gost nije pronađen.
     * @throws SQLException U slučaju greške pri brisanju zapisa.
     */
    public boolean deleteGuest(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Guest> existingGuest = guestRepository.findById(id, connection);
            if (existingGuest.isPresent()) {
                guestRepository.delete(id, connection);
                return true;
            }
        }
        return false;
    }

    /**
     * Interna metoda za mapiranje entiteta Guest u DTO objekt za prikaz korisniku.
     * * @param guest Entitet baze podataka.
     * @return GuestDTO Objekt spreman za slanje preko API-ja.
     */
    private GuestDTO mapEntityToDTO(Guest guest) {
        return new GuestDTO(
                guest.getId(),
                guest.getFirstName(),
                guest.getLastName(),
                guest.getEmail(),
                guest.getPhoneNumber(),
                guest.getDateOfBirth(),
                guest.getDocumentNumber(),
                guest.getAddressId()
        );
    }

    /**
     * Interna metoda za mapiranje ulaznog DTO-a u entitet baze podataka.
     * * @param guestDTO Podaci primljeni sa klijentske strane.
     * @return Guest Objekt spreman za perzistenciju u bazu.
     */
    private Guest mapDTOToEntity(GuestDTO guestDTO) {
        return new Guest(
                guestDTO.getId(),
                guestDTO.getFirstName(),
                guestDTO.getLastName(),
                guestDTO.getEmail(),
                guestDTO.getPhoneNumber(),
                guestDTO.getDateOfBirth(),
                guestDTO.getDocumentNumber(),
                guestDTO.getAddressId()
        );
    }
}