package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.DiscountDTO;
import com.example.hotel_management_system.model.Discount;
import com.example.hotel_management_system.repository.DiscountRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servisni sloj zadužen za upravljanje definicijama popusta u sistemu.
 * Omogućava CRUD operacije nad popustima i logiku za pronalaženje
 * aktivnih promocija na određeni datum koje se primjenjuju tokom check-out procesa.
 * * @author Tvoje Ime
 * @version 1.0
 */
@Service
public class DiscountService {
    private final DiscountRepository discountRepository;

    /**
     * Konstruktor za ubrizgavanje zavisnosti DiscountRepository-ja.
     */
    public DiscountService(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    /**
     * Kreira novi popust u sistemu.
     * * @param discountDTO Podaci o popustu (naziv, procenat, trajanje).
     * @return DiscountDTO Podaci o kreiranom popustu.
     * @throws SQLException U slučaju greške pri komunikaciji sa bazom podataka.
     */
    public DiscountDTO createDiscount(DiscountDTO discountDTO) throws SQLException {
        Discount discount = mapDTOToEntity(discountDTO);
        try (Connection connection = DbConfig.getConnection()) {
            discountRepository.save(discount, connection);
        }
        return discountDTO;
    }

    /**
     * Pronalazi popust na osnovu njegovog primarnog ključa.
     * * @param id Jedinstveni identifikator popusta.
     * @return DiscountDTO Objekt popusta ili null ako nije pronađen.
     * @throws SQLException U slučaju baze podataka.
     */
    public DiscountDTO getDiscountById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Discount> discount = discountRepository.findById(id, connection);
            return discount.map(this::mapEntityToDTO).orElse(null);
        }
    }

    /**
     * Vraća listu svih definisanih popusta u bazi podataka.
     * * @return List<DiscountDTO> Lista svih popusta mapiranih u DTO format.
     * @throws SQLException U slučaju baze podataka.
     */
    public List<DiscountDTO> getAllDiscounts() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<Discount> discounts = discountRepository.findAll(connection);
            return discounts.stream().map(this::mapEntityToDTO).collect(Collectors.toList());
        }
    }

    /**
     * Ažurira postojeći popust u bazi podataka.
     * * @param id ID popusta koji se mijenja.
     * @param discountDTO Novi podaci o popustu.
     * @return DiscountDTO Ažurirani podaci ili null ako popust sa tim ID-om ne postoji.
     * @throws SQLException U slučaju baze podataka.
     */
    public DiscountDTO updateDiscount(Long id, DiscountDTO discountDTO) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Discount> existingDiscount = discountRepository.findById(id, connection);
            if (existingDiscount.isPresent()) {
                Discount discount = mapDTOToEntity(discountDTO);
                discount.setId(id);
                discountRepository.update(discount, connection);
                return discountDTO;
            }
        }
        return null;
    }

    /**
     * Briše popust iz sistema na osnovu ID-a.
     * * @param id ID popusta za brisanje.
     * @return boolean True ako je brisanje uspješno, false ako popust nije pronađen.
     * @throws SQLException U slučaju baze podataka.
     */
    public boolean deleteDiscount(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            if (discountRepository.findById(id, connection).isPresent()) {
                discountRepository.delete(id, connection);
                return true;
            }
        }
        return false;
    }

    /**
     * Pronalazi popust koji je aktivan na proslijeđeni datum.
     * Koristi se unutar CheckOutService-a kako bi se utvrdilo da li gost ostvaruje pravo na popust.
     * * @param date Datum za koji se provjerava aktivnost popusta (obično današnji datum).
     * @return DiscountDTO Aktivni popust ili null ako na taj datum nema aktivnih promocija.
     * @throws SQLException U slučaju baze podataka.
     */
    public DiscountDTO getActiveDiscountForDate(LocalDate date) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Discount> discount = discountRepository.findActiveDiscountByDate(java.sql.Date.valueOf(date), connection);
            return discount.map(this::mapEntityToDTO).orElse(null);
        }
    }

    /**
     * Pomoćna privatna metoda za mapiranje entiteta Discount u DTO.
     */
    private DiscountDTO mapEntityToDTO(Discount discount) {
        return new DiscountDTO(
                discount.getId(),
                discount.getName(),
                discount.getPercentage(),
                discount.getStartDate(),
                discount.getEndDate(),
                discount.getDescription()
        );
    }

    /**
     * Pomoćna privatna metoda za mapiranje DTO-a u entitet Discount.
     */
    private Discount mapDTOToEntity(DiscountDTO dto) {
        return new Discount(
                dto.getId(),
                dto.getName(),
                dto.getPercentage(),
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getDescription()
        );
    }
}