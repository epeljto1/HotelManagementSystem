package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.dto.ExtraServiceDTO;
import com.example.hotel_management_system.service.ExtraServiceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/extra-services")
public class ExtraServiceController {

    private final ExtraServiceService extraServiceService;

    public ExtraServiceController(ExtraServiceService extraServiceService) {
        this.extraServiceService = extraServiceService;
    }

    @PostMapping
    public ResponseEntity<ExtraServiceDTO> createExtraService(@RequestBody ExtraServiceDTO extraServiceDTO) {
        try {
            ExtraServiceDTO created = extraServiceService.createExtraService(extraServiceDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExtraServiceDTO> getExtraServiceById(@PathVariable Long id) {
        try {
            ExtraServiceDTO result = extraServiceService.getExtraServiceById(id);
            if (result != null) {
                return ResponseEntity.ok(result);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ExtraServiceDTO>> getAllExtraServices() {
        try {
            List<ExtraServiceDTO> list = extraServiceService.getAllExtraServices();
            return ResponseEntity.ok(list);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExtraServiceDTO> updateExtraService(
            @PathVariable Long id,
            @RequestBody ExtraServiceDTO extraServiceDTO
    ) {
        try {
            ExtraServiceDTO updated = extraServiceService.updateExtraService(id, extraServiceDTO);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExtraService(@PathVariable Long id) {
        try {
            boolean deleted = extraServiceService.deleteExtraService(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}