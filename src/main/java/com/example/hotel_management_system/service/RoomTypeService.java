package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.RoomTypeDTO;
import com.example.hotel_management_system.model.RoomType;
import com.example.hotel_management_system.repository.RoomTypeRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servisni sloj zadužen za upravljanje kategorizacijom soba.
 * Pruža funkcionalnosti za definisanje različitih tipova smještaja,
 * njihovih maksimalnih kapaciteta (broj gostiju) i cijena po noćenju.
 * * <p>Ovaj servis je ključan za finansijski aspekt jer postavlja osnovnu cijenu
 * koja se koristi prilikom kreiranja rezervacija i finalnog obračuna računa.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Service
public class RoomTypeService {

    private final RoomTypeRepository roomTypeRepository;

    /**
     * Konstruktor za ubrizgavanje RoomTypeRepository zavisnosti.
     * * @param roomTypeRepository Repozitorij za perzistenciju kategorija soba.
     */
    public RoomTypeService(RoomTypeRepository roomTypeRepository) {
        this.roomTypeRepository = roomTypeRepository;
    }

    /**
     * Kreira novu kategoriju sobe (npr. Jednokrevetna, Apartman).
     * * @param roomTypeDTO Podaci o tipu sobe, kapacitetu i cijeni.
     * @return RoomTypeDTO Podaci o kreiranoj kategoriji.
     * @throws SQLException U slučaju greške pri radu sa bazom podataka.
     */
    public RoomTypeDTO createRoomType(RoomTypeDTO roomTypeDTO) throws SQLException {
        RoomType roomType = mapDTOToEntity(roomTypeDTO);
        try (Connection connection = DbConfig.getConnection()) {
            roomTypeRepository.save(roomType, connection);
        }
        return roomTypeDTO;
    }

    /**
     * Dobavlja podatke o specifičnom tipu sobe na osnovu ID-a.
     * * @param id Jedinstveni identifikator tipa sobe.
     * @return RoomTypeDTO Objekt sa podacima ili null ako ID ne postoji.
     * @throws SQLException U slučaju greške sa SQL upitom.
     */
    public RoomTypeDTO getRoomTypeById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<RoomType> roomType = roomTypeRepository.findById(id, connection);
            return roomType.map(this::mapEntityToDTO).orElse(null);
        }
    }

    /**
     * Vraća listu svih definisanih tipova soba u hotelu.
     * * @return List<RoomTypeDTO> Lista svih kategorija mapirana u DTO objekte.
     * @throws SQLException U slučaju greške pri čitanju iz baze.
     */
    public List<RoomTypeDTO> getAllRoomTypes() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<RoomType> roomTypes = roomTypeRepository.findAll(connection);
            return roomTypes.stream().map(this::mapEntityToDTO).collect(Collectors.toList());
        }
    }

    /**
     * Ažurira parametre postojeće kategorije sobe (npr. promjena cijene po noćenju).
     * * @param id ID kategorije koja se mijenja.
     * @param roomTypeDTO Novi podaci o kategoriji.
     * @return RoomTypeDTO Ažurirani podaci ili null ako kategorija nije pronađena.
     * @throws SQLException U slučaju greške pri komunikaciji sa bazom.
     */
    public RoomTypeDTO updateRoomType(Long id, RoomTypeDTO roomTypeDTO) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<RoomType> existingRoomType = roomTypeRepository.findById(id, connection);
            if (existingRoomType.isPresent()) {
                RoomType roomType = mapDTOToEntity(roomTypeDTO);
                roomType.setId(id);
                roomTypeRepository.update(roomType, connection);
                return roomTypeDTO;
            }
        }
        return null;
    }

    /**
     * Uklanja tip sobe iz sistema.
     * * @param id ID tipa sobe za brisanje.
     * @return boolean True ako je uspješno obrisano, false inače.
     * @throws SQLException U slučaju restrikcija u bazi (npr. ako postoje sobe tog tipa).
     */
    public boolean deleteRoomType(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<RoomType> existingRoomType = roomTypeRepository.findById(id, connection);
            if (existingRoomType.isPresent()) {
                roomTypeRepository.delete(id, connection);
                return true;
            }
        }
        return false;
    }

    /**
     * Interna helper metoda za mapiranje RoomType modela u DTO.
     */
    private RoomTypeDTO mapEntityToDTO(RoomType roomType) {
        return new RoomTypeDTO(
                roomType.getId(),
                roomType.getName(),
                roomType.getDescription(),
                roomType.getCapacity(),
                roomType.getPricePerNight()
        );
    }

    /**
     * Interna helper metoda za mapiranje RoomTypeDTO u model za bazu.
     */
    private RoomType mapDTOToEntity(RoomTypeDTO roomTypeDTO) {
        return new RoomType(
                roomTypeDTO.getId(),
                roomTypeDTO.getName(),
                roomTypeDTO.getDescription(),
                roomTypeDTO.getCapacity(),
                roomTypeDTO.getPricePerNight()
        );
    }
}