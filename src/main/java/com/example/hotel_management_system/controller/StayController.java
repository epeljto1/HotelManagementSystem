package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.dto.StayDTO;
import com.example.hotel_management_system.service.StayService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/stays")
public class StayController {

    private final StayService stayService;

    public StayController(StayService stayService) {
        this.stayService = stayService;
    }

    @PostMapping
    public ResponseEntity<StayDTO> createStay(@RequestBody StayDTO stayDTO) {
        try {
            StayDTO createdStay = stayService.createStay(stayDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdStay);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<StayDTO> getStayById(@PathVariable Long id) {
        try {
            StayDTO stay = stayService.getStayById(id);
            if (stay != null) {
                return ResponseEntity.ok(stay);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<StayDTO>> getAllStays() {
        try {
            List<StayDTO> stays = stayService.getAllStays();
            return ResponseEntity.ok(stays);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<StayDTO> updateStay(@PathVariable Long id, @RequestBody StayDTO stayDTO) {
        try {
            StayDTO updatedStay = stayService.updateStay(id, stayDTO);
            if (updatedStay != null) {
                return ResponseEntity.ok(updatedStay);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStay(@PathVariable Long id) {
        try {
            boolean deleted = stayService.deleteStay(id);
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