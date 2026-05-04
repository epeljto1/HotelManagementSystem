package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.dto.CheckOutRequestDTO;
import com.example.hotel_management_system.dto.CheckOutResponseDTO;
import com.example.hotel_management_system.dto.ReservationDTO;
import com.example.hotel_management_system.exception.InvoiceAlreadyExistsException;
import com.example.hotel_management_system.exception.InvalidReservationStatusException;
import com.example.hotel_management_system.exception.ReservationNotFoundException;
import com.example.hotel_management_system.exception.RoomNotFoundException;
import com.example.hotel_management_system.repository.InvoiceRepository;
import com.example.hotel_management_system.service.CheckOutService;
import com.example.hotel_management_system.service.PdfInvoiceService;
import com.example.hotel_management_system.service.ReservationService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final CheckOutService checkOutService;
    private final PdfInvoiceService pdfInvoiceService;
    private final InvoiceRepository invoiceRepository;

    public ReservationController(ReservationService reservationService,
                                 CheckOutService checkOutService,
                                 PdfInvoiceService pdfInvoiceService,
                                 InvoiceRepository invoiceRepository) {
        this.reservationService = reservationService;
        this.checkOutService = checkOutService;
        this.pdfInvoiceService = pdfInvoiceService;
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Process guest check-out and generate invoice
     */
    @PostMapping("/{id}/checkout")
    public ResponseEntity<?> checkOut(@PathVariable Long id, @RequestBody CheckOutRequestDTO checkOutRequest) {
        try {
            checkOutRequest.setReservationId(id);
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("DATABASE_ERROR", "Greška u bazi podataka."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("INTERNAL_ERROR", e.getMessage()));
        }
    }

    /**
     * Generates and downloads PDF invoice for a specific reservation
     */
    @GetMapping("/{id}/invoice/pdf")
    public ResponseEntity<?> downloadInvoicePdf(@PathVariable Long id) {
        try (java.sql.Connection conn = com.example.hotel_management_system.config.DbConfig.getConnection()) {
            com.example.hotel_management_system.model.Invoice invoice =
                    invoiceRepository.findByStayId(id, conn);

            if (invoice == null || invoice.getInvoicePdf() == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(createErrorResponse("PDF_NOT_FOUND", "Faktura ili PDF dokument nisu pronađeni za ovu rezervaciju."));
            }

            byte[] pdfContent = invoice.getInvoicePdf();
            ByteArrayInputStream bis = new ByteArrayInputStream(pdfContent);

            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Disposition", "inline; filename=racun_" + id + ".pdf");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .contentType(MediaType.APPLICATION_PDF)
                    .contentLength(pdfContent.length)
                    .body(new InputStreamResource(bis));

        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("DATABASE_ERROR", "Greška pri pristupu bazi podataka."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("PDF_ERROR", "Greška: " + e.getMessage()));
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody ReservationDTO dto) {
        try {
            ReservationDTO created = reservationService.createReservation(dto);
            return new ResponseEntity<>(created, HttpStatus.CREATED);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("DATABASE_ERROR", "Greška pri kreiranju rezervacije."));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll() {
        try {
            List<ReservationDTO> reservations = reservationService.getAllReservations();
            return ResponseEntity.ok(reservations);
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("DATABASE_ERROR", "Greška pri dohvatanju rezervacija."));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        try {
            ReservationDTO dto = reservationService.getReservationById(id);
            if (dto != null) {
                return ResponseEntity.ok(dto);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("NOT_FOUND", "Rezervacija nije pronađena."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("DATABASE_ERROR", "Greška u bazi podataka."));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody ReservationDTO dto) {
        try {
            ReservationDTO updated = reservationService.updateReservation(id, dto);
            if (updated != null) {
                return ResponseEntity.ok(updated);
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("NOT_FOUND", "Rezervacija za ažuriranje nije pronađena."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("DATABASE_ERROR", "Greška pri ažuriranju."));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        try {
            boolean deleted = reservationService.deleteReservation(id);
            if (deleted) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(createErrorResponse("NOT_FOUND", "Rezervacija za brisanje nije pronađena."));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("DATABASE_ERROR", "Greška pri brisanju."));
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