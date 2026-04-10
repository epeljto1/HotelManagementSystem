package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.dto.CheckOutRequestDTO;
import com.example.hotel_management_system.dto.CheckOutResponseDTO;
import com.example.hotel_management_system.dto.ReservationDTO;
import com.example.hotel_management_system.exception.InvoiceAlreadyExistsException;
import com.example.hotel_management_system.exception.InvalidReservationStatusException;
import com.example.hotel_management_system.exception.ReservationNotFoundException;
import com.example.hotel_management_system.exception.RoomNotFoundException;
import com.example.hotel_management_system.service.CheckOutService;
import com.example.hotel_management_system.service.ReservationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final CheckOutService checkOutService;

    public ReservationController(ReservationService reservationService, CheckOutService checkOutService) {
        this.reservationService = reservationService;
        this.checkOutService = checkOutService;
    }

    /**
     * Process guest check-out and generate invoice
     *
     * @param checkOutRequest Check-out request with reservation ID and optional check-out time
     * @return Check-out response with invoice breakdown and updated statuses
     */
    @PostMapping("/{id}/checkout")
    public ResponseEntity<?> checkOut(@PathVariable Long id, @RequestBody CheckOutRequestDTO checkOutRequest) {
        try {
            // Set the reservation ID from path variable
            checkOutRequest.setReservationId(id);

            // Process check-out
            CheckOutResponseDTO response = checkOutService.processCheckOut(checkOutRequest);

            return ResponseEntity.ok(response);

        } catch (ReservationNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("RESERVATION_NOT_FOUND", e.getMessage()));

        } catch (InvalidReservationStatusException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(createErrorResponse("INVALID_RESERVATION_STATUS", e.getMessage()));

        } catch (RoomNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(createErrorResponse("ROOM_NOT_FOUND", e.getMessage()));

        } catch (InvoiceAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(createErrorResponse("INVOICE_ALREADY_EXISTS", e.getMessage()));

        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("DATABASE_ERROR", "An error occurred during check-out processing"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(createErrorResponse("INTERNAL_ERROR", e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<ReservationDTO> create(@RequestBody ReservationDTO dto) {
        try {
            ReservationDTO created = reservationService.createReservation(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    public ResponseEntity<List<ReservationDTO>> getAll() {
        try {
            List<ReservationDTO> reservations = reservationService.getAllReservations();
            return ResponseEntity.ok(reservations);
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReservationDTO> getById(@PathVariable Long id) {
        try {
            ReservationDTO dto = reservationService.getReservationById(id);
            if (dto != null) {
                return ResponseEntity.ok(dto);
            }
            return ResponseEntity.notFound().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReservationDTO> update(@PathVariable Long id, @RequestBody ReservationDTO dto) {
        try {
            ReservationDTO updated = reservationService.updateReservation(id, dto);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.notFound().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            boolean deleted = reservationService.deleteReservation(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.notFound().build();
        } catch (SQLException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Helper method to create error response DTO
     */
    private Map<String, Object> createErrorResponse(String errorCode, String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("errorCode", errorCode);
        errorResponse.put("message", message);
        errorResponse.put("timestamp", System.currentTimeMillis());
        return errorResponse;
    }
}