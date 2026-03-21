package com.example.hotel_management_system.service;

import com.example.hotel_management_system.dto.ServiceUsageDTO;
import com.example.hotel_management_system.model.ServiceUsage;
import com.example.hotel_management_system.repository.ServiceUsageRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceUsageService {

    private final ServiceUsageRepository repository;

    public ServiceUsageService(ServiceUsageRepository repository) {
        this.repository = repository;
    }

    public List<ServiceUsageDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public ServiceUsageDTO findById(Long id) {
        return toDTO(repository.findById(id));
    }

    public void save(ServiceUsageDTO dto) {
        repository.save(toModel(dto));
    }

    public void update(Long id, ServiceUsageDTO dto) {
        repository.update(id, toModel(dto));
    }

    public void delete(Long id) {
        repository.delete(id);
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