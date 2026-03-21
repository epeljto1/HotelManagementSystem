package com.example.hotel_management_system.service;

import com.example.hotel_management_system.dto.InvoiceDTO;
import com.example.hotel_management_system.model.Invoice;
import com.example.hotel_management_system.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository repository;

    public InvoiceService(InvoiceRepository repository) {
        this.repository = repository;
    }

    public List<InvoiceDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public InvoiceDTO findById(Long id) {
        return toDTO(repository.findById(id));
    }

    public void save(InvoiceDTO dto) {
        validateStatus(dto.getStatus());
        repository.save(toModel(dto));
    }

    public void update(Long id, InvoiceDTO dto) {
        validateStatus(dto.getStatus());
        repository.update(id, toModel(dto));
    }

    public void delete(Long id) {
        repository.delete(id);
    }

    private void validateStatus(String status) {
        if (status == null ||
                (!status.equals("Unpaid")
                        && !status.equals("Partially paid")
                        && !status.equals("Paid"))) {
            throw new RuntimeException("Invalid status! Allowed values are: Unpaid, Partially paid, Paid");
        }
    }

    private InvoiceDTO toDTO(Invoice invoice) {
        return new InvoiceDTO(
                invoice.getId(),
                invoice.getIssueDate(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                invoice.getStayId()
        );
    }

    private Invoice toModel(InvoiceDTO dto) {
        return new Invoice(
                dto.getId(),
                dto.getIssueDate(),
                dto.getTotalAmount(),
                dto.getStatus(),
                dto.getStayId()
        );
    }
}