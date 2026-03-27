package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.ServiceUsageDTO;
import com.example.hotel_management_system.model.ServiceUsage;
import com.example.hotel_management_system.repository.ServiceUsageRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceUsageService {

    private final ServiceUsageRepository repository;

    public ServiceUsageService(ServiceUsageRepository repository) {
        this.repository = repository;
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
            repository.save(toModel(dto), connection);
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