package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.dto.ServiceUsageDTO;
import com.example.hotel_management_system.service.ServiceUsageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/service-usage")
public class ServiceUsageController {

    private final ServiceUsageService service;

    public ServiceUsageController(ServiceUsageService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServiceUsageDTO> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public ServiceUsageDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public void save(@RequestBody ServiceUsageDTO dto) {
        service.save(dto);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody ServiceUsageDTO dto) {
        service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}