package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.dto.RoomTypePriceUpdateDTO;
import com.example.hotel_management_system.dto.RoomTypeDTO;
import com.example.hotel_management_system.service.RoomTypeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/room-types")
public class RoomTypeController {

    private final RoomTypeService roomTypeService;

    public RoomTypeController(RoomTypeService roomTypeService) {
        this.roomTypeService = roomTypeService;
    }

    @PostMapping
    public ResponseEntity<RoomTypeDTO> createRoomType(@RequestBody RoomTypeDTO roomTypeDTO) {
        try {
            RoomTypeDTO created = roomTypeService.createRoomType(roomTypeDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomTypeDTO> getById(@PathVariable Long id) {
        try {
            RoomTypeDTO roomType = roomTypeService.getRoomTypeById(id);
            if (roomType != null) {
                return ResponseEntity.ok(roomType);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<RoomTypeDTO>> getAll() {
        try {
            return ResponseEntity.ok(roomTypeService.getAllRoomTypes());
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomTypeDTO> update(@PathVariable Long id, @RequestBody RoomTypeDTO dto) {
        try {
            RoomTypeDTO updated = roomTypeService.updateRoomType(id, dto);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/price/from-package")
    public ResponseEntity<?> updatePriceUsingPackage(
            @PathVariable Long id,
            @RequestBody RoomTypePriceUpdateDTO dto) {
        try {
            RoomTypeDTO updated = roomTypeService.updateRoomTypePriceUsingPackage(id, dto);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SQLException e) {
            if (isPackageValidationError(e)) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            boolean deleted = roomTypeService.deleteRoomType(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private boolean isPackageValidationError(SQLException e) {
        return e.getErrorCode() >= 20001 && e.getErrorCode() <= 20006;
    }
}
