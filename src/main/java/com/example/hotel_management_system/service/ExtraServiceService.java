package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.ExtraServiceDTO;
import com.example.hotel_management_system.model.ExtraService;
import com.example.hotel_management_system.repository.ExtraServiceRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servisni sloj zadužen za upravljanje katalogom dodatnih hotelskih usluga.
 * Omogućava administraciju usluga koje gosti mogu konzumirati tokom boravka,
 * kao što su doručak, bazen, room service i slično.
 * * <p>Ovaj servis služi kao osnova za obračun troškova u CheckOut procesu
 * jer sadrži definicije jediničnih cijena usluga.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Service
public class ExtraServiceService {

    private final ExtraServiceRepository extraServiceRepository;

    /**
     * Konstruktor za Dependency Injection repozitorija dodatnih usluga.
     * * @param extraServiceRepository Repozitorij za rad sa NBP_SERVICE tabelom.
     */
    public ExtraServiceService(ExtraServiceRepository extraServiceRepository) {
        this.extraServiceRepository = extraServiceRepository;
    }

    /**
     * Kreira novu dodatnu uslugu u sistemu.
     * * @param dto Podaci o usluzi (naziv, opis, cijena, dostupnost).
     * @return ExtraServiceDTO Vraća proslijeđeni DTO objekt nakon spašavanja.
     * @throws SQLException U slučaju greške pri radu sa bazom podataka.
     */
    public ExtraServiceDTO createExtraService(ExtraServiceDTO dto) throws SQLException {
        ExtraService entity = mapDTOToEntity(dto);
        try (Connection connection = DbConfig.getConnection()) {
            extraServiceRepository.save(entity, connection);
        }
        return dto;
    }

    /**
     * Pronalazi specifičnu uslugu na osnovu njenog ID-a.
     * * @param id Jedinstveni identifikator usluge.
     * @return ExtraServiceDTO DTO objekt usluge ili null ako usluga ne postoji.
     * @throws SQLException U slučaju greške sa SQL upitom.
     */
    public ExtraServiceDTO getExtraServiceById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<ExtraService> entity = extraServiceRepository.findById(id, connection);
            return entity.map(this::mapEntityToDTO).orElse(null);
        }
    }

    /**
     * Dobavlja listu svih dostupnih i nedostupnih dodatnih usluga iz baze.
     * * @return List<ExtraServiceDTO> Lista svih usluga mapirana u DTO objekte.
     * @throws SQLException U slučaju greške sa SQL upitom.
     */
    public List<ExtraServiceDTO> getAllExtraServices() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<ExtraService> list = extraServiceRepository.findAll(connection);
            return list.stream()
                    .map(this::mapEntityToDTO)
                    .collect(Collectors.toList());
        }
    }

    /**
     * Ažurira podatke o postojećoj usluzi (npr. promjena cijene ili naziva).
     * * @param id ID usluge koju je potrebno ažurirati.
     * @param dto Novi podaci za uslugu.
     * @return ExtraServiceDTO Ažurirani podaci ili null ako usluga nije pronađena.
     * @throws SQLException U slučaju greške pri radu sa bazom podataka.
     */
    public ExtraServiceDTO updateExtraService(Long id, ExtraServiceDTO dto) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<ExtraService> existing = extraServiceRepository.findById(id, connection);
            if (existing.isPresent()) {
                ExtraService entity = mapDTOToEntity(dto);
                entity.setId(id);
                extraServiceRepository.update(entity, connection);
                return dto;
            }
        }
        return null;
    }

    /**
     * Uklanja dodatnu uslugu iz kataloga na osnovu ID-a.
     * * @param id ID usluge za brisanje.
     * @return boolean True ako je brisanje izvršeno, false ako usluga ne postoji.
     * @throws SQLException U slučaju greške pri radu sa bazom podataka.
     */
    public boolean deleteExtraService(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<ExtraService> existing = extraServiceRepository.findById(id, connection);
            if (existing.isPresent()) {
                extraServiceRepository.delete(id, connection);
                return true;
            }
        }
        return false;
    }

    /**
     * Pomoćna metoda za pretvaranje entiteta iz baze u DTO objekt za prikaz.
     * * @param entity Model dobijen iz repozitorija.
     * @return ExtraServiceDTO Objekt spreman za slanje prema klijentu.
     */
    private ExtraServiceDTO mapEntityToDTO(ExtraService entity) {
        return new ExtraServiceDTO(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getUnitPrice(),
                entity.getAvailable()
        );
    }

    /**
     * Pomoćna metoda za pretvaranje DTO objekta u entitet za bazu podataka.
     * * @param dto Podaci primljeni putem API-ja.
     * @return ExtraService Model spreman za perzistenciju.
     */
    private ExtraService mapDTOToEntity(ExtraServiceDTO dto) {
        return new ExtraService(
                dto.getId(),
                dto.getName(),
                dto.getDescription(),
                dto.getUnitPrice(),
                dto.getAvailable()
        );
    }
}