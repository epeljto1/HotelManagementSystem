package com.example.hotel_management_system.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import com.example.hotel_management_system.dto.InvoiceDTO;
import com.example.hotel_management_system.service.InvoiceService;
import org.springframework.web.bind.annotation.*;
import com.example.hotel_management_system.dto.DiscountApplyDTO;
import java.util.List;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    @GetMapping
    public List<InvoiceDTO> findAll() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public InvoiceDTO findById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PostMapping
    public void save(@RequestBody InvoiceDTO dto) {
        service.save(dto);
    }

    @PutMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody InvoiceDTO dto) {
        service.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
    @PostMapping("/apply-discount")
    public ResponseEntity<?> applyDiscount(@RequestBody DiscountApplyDTO dto) {
        try {
            service.applyDiscountManually(dto.getInvoiceId(), dto.getDiscountId());
            return ResponseEntity.ok("Discount applied successfully.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}