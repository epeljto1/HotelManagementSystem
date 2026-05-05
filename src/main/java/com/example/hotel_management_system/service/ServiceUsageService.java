package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.ServiceUsageDTO;
import com.example.hotel_management_system.model.ExtraService;
import com.example.hotel_management_system.model.ServiceUsage;
import com.example.hotel_management_system.model.Stay;
import com.example.hotel_management_system.repository.ExtraServiceRepository;
import com.example.hotel_management_system.repository.ServiceUsageRepository;
import com.example.hotel_management_system.repository.StayRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servisni sloj zadužen za evidenciju korištenja dodatnih usluga tokom boravka gosta.
 * Klasa upravlja poslovnom logikom izračunavanja cijene usluge na osnovu količine
 * i jedinične cijene, te automatski ažurira kumulativni trošak boravka (Stay).
 * * <p>Ovaj servis direktno utiče na finalni obračun koji se prikazuje na fakturi.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Service
public class ServiceUsageService {

    private final ServiceUsageRepository repository;
    private final ExtraServiceRepository extraServiceRepository;
    private final StayRepository stayRepository;

    /**
     * Konstruktor za ubrizgavanje zavisnosti repozitorija potrebnih za rad sa uslugama i boravcima.
     */
    public ServiceUsageService(ServiceUsageRepository repository,
                               ExtraServiceRepository extraServiceRepository,
                               StayRepository stayRepository) {
        this.repository = repository;
        this.extraServiceRepository = extraServiceRepository;
        this.stayRepository = stayRepository;
    }

    /**
     * Dobavlja listu svih zabilježenih korištenja usluga u sistemu.
     * * @return List<ServiceUsageDTO> Lista mapiranih DTO objekata sa nazivima usluga.
     * @throws RuntimeException Omotač za SQLException.
     */
    public List<ServiceUsageDTO> findAll() {
        try (Connection connection = DbConfig.getConnection()) {
            return repository.findAll(connection)
                    .stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching service usages.", e);
        }
    }

    /**
     * Pronalazi konkretan zapis o korištenju usluge putem ID-a.
     * * @param id Identifikator zapisa.
     * @return ServiceUsageDTO ili null ako zapis ne postoji.
     */
    public ServiceUsageDTO findById(Long id) {
        try (Connection connection = DbConfig.getConnection()) {
            ServiceUsage serviceUsage = repository.findById(id, connection);
            return serviceUsage != null ? toDTO(serviceUsage) : null;
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching service usage by id.", e);
        }
    }

    /**
     * Bilježi novo korištenje usluge i ažurira ukupnu cijenu boravka.
     * * <p>Proces uključuje:</p>
     * 1. Provjeru postojanja usluge u katalogu.<br>
     * 2. Izračunavanje ukupne cijene (količina * jedinična cijena).<br>
     * 3. Spašavanje zapisa o korištenju.<br>
     * 4. Inkrementalno ažuriranje kolone {@code actual_total_price} u tabeli Stay.
     * * @param dto Podaci o korištenju (ID boravka, ID usluge, količina, datum).
     * @throws RuntimeException Ako usluga nije pronađena ili dođe do SQL greške.
     */
    public void save(ServiceUsageDTO dto) {
        try (Connection connection = DbConfig.getConnection()) {

            // 1. Dobavljanje cijene iz kataloga usluga
            Optional<ExtraService> extraServiceOpt = extraServiceRepository.findById(dto.getServiceId(), connection);
            if (extraServiceOpt.isEmpty()) {
                throw new RuntimeException("Service not found with id: " + dto.getServiceId());
            }

            ExtraService extraService = extraServiceOpt.get();

            // 2. Izračun ukupne cijene za ovu stavku
            BigDecimal totalPrice = BigDecimal.valueOf(extraService.getUnitPrice())
                    .multiply(BigDecimal.valueOf(dto.getQuantity()));

            ServiceUsage serviceUsage = new ServiceUsage(
                    dto.getId(),
                    dto.getStayId(),
                    dto.getServiceId(),
                    dto.getQuantity(),
                    dto.getUsageDate(),
                    totalPrice
            );

            // 3. Perzistencija zapisa
            repository.save(serviceUsage, connection);

            // 4. Ažuriranje ukupnog duga na boravku (Stay)
            Optional<Stay> stayOpt = stayRepository.findById(dto.getStayId(), connection);
            if (stayOpt.isPresent()) {
                Stay stay = stayOpt.get();

                double currentTotal = stay.getActualTotalPrice() != null ? stay.getActualTotalPrice() : 0.0;
                stay.setActualTotalPrice(currentTotal + totalPrice.doubleValue());

                stayRepository.update(stay, connection);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error while saving service usage.", e);
        }
    }

    /**
     * Ažurira postojeći zapis o korištenju usluge.
     */
    public void update(Long id, ServiceUsageDTO dto) {
        try (Connection connection = DbConfig.getConnection()) {
            repository.update(id, toModel(dto), connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error while updating service usage.", e);
        }
    }

    /**
     * Briše zapis o korištenju usluge iz baze podataka.
     */
    public void delete(Long id) {
        try (Connection connection = DbConfig.getConnection()) {
            repository.delete(id, connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error while deleting service usage.", e);
        }
    }

    /**
     * Konvertuje entitet u DTO i obogaćuje ga nazivom usluge.
     * Vrši dodatni upit u bazu kako bi se korisniku prikazao naziv (npr. "Doručak")
     * umjesto samo ID broja.
     */
    private ServiceUsageDTO toDTO(ServiceUsage serviceUsage) {
        String name = "Unknown Service";
        try (Connection connection = DbConfig.getConnection()) {
            Optional<ExtraService> service = extraServiceRepository.findById(serviceUsage.getServiceId(), connection);
            if (service.isPresent()) {
                name = service.get().getName();
            }
        } catch (SQLException e) {
            // Logovanje ili fallback
        }

        return new ServiceUsageDTO(
                serviceUsage.getId(),
                serviceUsage.getStayId(),
                name,
                serviceUsage.getQuantity(),
                serviceUsage.getUsageDate(),
                serviceUsage.getTotalPrice()
        );
    }

    /**
     * Konvertuje DTO u model za internu obradu.
     */
    private ServiceUsage toModel(ServiceUsageDTO dto) {
        return new ServiceUsage(
                dto.getId(),
                dto.getStayId(),
                dto.getServiceId(),
                dto.getQuantity(),
                dto.getUsageDate(),
                dto.getTotalPrice()
        );
    }
}