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

@Service
public class GuestService {
    private final GuestRepository guestRepository;

    public GuestService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    public GuestDTO createGuest(GuestDTO guestDTO) throws SQLException {
        Guest guest = mapDTOToEntity(guestDTO);
        try (Connection connection = DbConfig.getConnection()) {
            guestRepository.save(guest, connection);
        }
        return guestDTO;
    }

    public GuestDTO getGuestById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Guest> guest = guestRepository.findById(id, connection);
            return guest.map(this::mapEntityToDTO).orElse(null);
        }
    }

    public List<GuestDTO> getAllGuests() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<Guest> guests = guestRepository.findAll(connection);
            return guests.stream().map(this::mapEntityToDTO).collect(Collectors.toList());
        }
    }

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
