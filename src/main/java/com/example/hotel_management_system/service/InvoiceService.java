package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.InvoiceDTO;
import com.example.hotel_management_system.model.Invoice;
import com.example.hotel_management_system.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository repository;

    public InvoiceService(InvoiceRepository repository) {
        this.repository = repository;
    }

    public List<InvoiceDTO> findAll() {
        try (Connection connection = DbConfig.getConnection()) {
            return repository.findAll(connection)
                    .stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching invoices.", e);
        }
    }

    public InvoiceDTO findById(Long id) {
        try (Connection connection = DbConfig.getConnection()) {
            Invoice invoice = repository.findById(id, connection);
            return invoice != null ? toDTO(invoice) : null;
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching invoice by id.", e);
        }
    }

    public void save(InvoiceDTO dto) {
        validateStatus(dto.getStatus());

        try (Connection connection = DbConfig.getConnection()) {
            repository.save(toModel(dto), connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error while saving invoice.", e);
        }
    }

    public void update(Long id, InvoiceDTO dto) {
        validateStatus(dto.getStatus());

        try (Connection connection = DbConfig.getConnection()) {
            repository.update(id, toModel(dto), connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error while updating invoice.", e);
        }
    }

    public void delete(Long id) {
        try (Connection connection = DbConfig.getConnection()) {
            repository.delete(id, connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error while deleting invoice.", e);
        }
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