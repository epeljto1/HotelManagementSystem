package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.dto.RoomPackageCreateDTO;
import com.example.hotel_management_system.dto.RoomDTO;
import com.example.hotel_management_system.dto.RoomStatusUpdateDTO;
import com.example.hotel_management_system.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile; // VAŽNO: Dodaj ovaj import

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }


    // post metoda koja omogucava dodavanje slike vec postojecim sobama u tabeli sobe
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadRoomImage(
            @PathVariable Long id,
            @RequestPart("file") MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body("Fajl nije odabran ili je prazan!");
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body("Dozvoljeni su samo slikovni fajlovi!");
            }

            byte[] imageBytes = file.getBytes();
            roomService.updateRoomImage(id, imageBytes);

            return ResponseEntity.ok("Slika uspješno spasena za sobu ID: " + id);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška pri čitanju fajla.");
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Greška u bazi: " + e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<RoomDTO> createRoom(@RequestBody RoomDTO roomDTO) {
        try {
            RoomDTO createdRoom = roomService.createRoom(roomDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/from-package")
    public ResponseEntity<?> createRoomUsingPackage(@RequestBody RoomPackageCreateDTO roomDTO) {
        try {
            RoomDTO createdRoom = roomService.createRoomUsingPackage(roomDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
        } catch (SQLException e) {
            if (isPackageValidationError(e)) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/status/from-package")
    public ResponseEntity<?> changeRoomStatusUsingPackage(
            @PathVariable Long id,
            @RequestBody RoomStatusUpdateDTO roomDTO) {
        try {
            RoomDTO updatedRoom = roomService.changeRoomStatusUsingPackage(id, roomDTO);
            if (updatedRoom != null) {
                return ResponseEntity.ok(updatedRoom);
            }

            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SQLException e) {
            if (isPackageValidationError(e)) {
                return ResponseEntity.badRequest().body(e.getMessage());
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


    // post metoda za unos sobe + unos slike
    @PostMapping(value = "/with-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RoomDTO> createRoomWithImage(
            @ModelAttribute RoomDTO roomDTO,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file != null && !file.isEmpty()) {
                roomDTO.setImage(file.getBytes());
            }

            RoomDTO createdRoom = roomService.createRoom(roomDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
        } catch (IOException | SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> getRoomById(@PathVariable Long id) {
        try {
            RoomDTO room = roomService.getRoomById(id);
            if (room != null) {
                return ResponseEntity.ok(room);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<RoomDTO>> getAllRooms() {
        try {
            List<RoomDTO> rooms = roomService.getAllRooms();
            return ResponseEntity.ok(rooms);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoomDTO> updateRoom(@PathVariable Long id, @RequestBody RoomDTO roomDTO) {
        try {
            RoomDTO updatedRoom = roomService.updateRoom(id, roomDTO);
            if (updatedRoom != null) {
                return ResponseEntity.ok(updatedRoom);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        try {
            boolean deleted = roomService.deleteRoom(id);
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
