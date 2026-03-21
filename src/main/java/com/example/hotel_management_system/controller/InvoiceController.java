package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.dto.InvoiceDTO;
import com.example.hotel_management_system.service.InvoiceService;
import org.springframework.web.bind.annotation.*;

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
}