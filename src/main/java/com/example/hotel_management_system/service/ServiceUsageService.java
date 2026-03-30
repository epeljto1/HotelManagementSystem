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

@Service
public class ServiceUsageService {

    private final ServiceUsageRepository repository;
    private final ExtraServiceRepository extraServiceRepository;
    private final StayRepository stayRepository;

    public ServiceUsageService(ServiceUsageRepository repository,
                               ExtraServiceRepository extraServiceRepository,
                               StayRepository stayRepository) {
        this.repository = repository;
        this.extraServiceRepository = extraServiceRepository;
        this.stayRepository = stayRepository;
    }

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

    public ServiceUsageDTO findById(Long id) {
        try (Connection connection = DbConfig.getConnection()) {
            ServiceUsage serviceUsage = repository.findById(id, connection);
            return serviceUsage != null ? toDTO(serviceUsage) : null;
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching service usage by id.", e);
        }
    }

    public void save(ServiceUsageDTO dto) {
        try (Connection connection = DbConfig.getConnection()) {

            Optional<ExtraService> extraServiceOpt = extraServiceRepository.findById(dto.getServiceId(), connection);
            if (extraServiceOpt.isEmpty()) {
                throw new RuntimeException("Service not found with id: " + dto.getServiceId());
            }

            ExtraService extraService = extraServiceOpt.get();

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

            repository.save(serviceUsage, connection);

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

    public void update(Long id, ServiceUsageDTO dto) {
        try (Connection connection = DbConfig.getConnection()) {
            repository.update(id, toModel(dto), connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error while updating service usage.", e);
        }
    }

    public void delete(Long id) {
        try (Connection connection = DbConfig.getConnection()) {
            repository.delete(id, connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error while deleting service usage.", e);
        }
    }

    private ServiceUsageDTO toDTO(ServiceUsage serviceUsage) {
        return new ServiceUsageDTO(
                serviceUsage.getId(),
                serviceUsage.getStayId(),
                serviceUsage.getServiceId(),
                serviceUsage.getQuantity(),
                serviceUsage.getUsageDate(),
                serviceUsage.getTotalPrice()
        );
    }

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