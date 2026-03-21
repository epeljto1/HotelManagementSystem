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

@Service
public class HotelService {
    private final HotelRepository hotelRepository;

    public HotelService(HotelRepository hotelRepository) {
        this.hotelRepository = hotelRepository;
    }

    public HotelDTO createHotel(HotelDTO hotelDTO) throws SQLException {
        Hotel hotel = mapDTOToEntity(hotelDTO);
        try (Connection connection = DbConfig.getConnection()) {
            hotelRepository.save(hotel, connection);
        }
        return hotelDTO;
    }

    public HotelDTO getHotelById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Hotel> hotel = hotelRepository.findById(id, connection);
            return hotel.map(this::mapEntityToDTO).orElse(null);
        }
    }

    public List<HotelDTO> getAllHotels() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<Hotel> hotels = hotelRepository.findAll(connection);
            return hotels.stream()
                    .map(this::mapEntityToDTO)
                    .collect(Collectors.toList());
        }
    }

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

