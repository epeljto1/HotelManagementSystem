package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.PaymentDTO;
import com.example.hotel_management_system.model.Payment;
import com.example.hotel_management_system.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public PaymentDTO createPayment(PaymentDTO paymentDTO) throws SQLException {
        Payment payment = mapDTOToEntity(paymentDTO);
        try (Connection connection = DbConfig.getConnection()) {
            paymentRepository.save(payment, connection);
        }
        return paymentDTO;
    }

    public PaymentDTO getPaymentById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Payment> payment = paymentRepository.findById(id, connection);
            return payment.map(this::mapEntityToDTO).orElse(null);
        }
    }

    public List<PaymentDTO> getAllPayments() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<Payment> payments = paymentRepository.findAll(connection);
            return payments.stream().map(this::mapEntityToDTO).collect(Collectors.toList());
        }
    }

    public PaymentDTO updatePayment(Long id, PaymentDTO paymentDTO) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Payment> existingPayment = paymentRepository.findById(id, connection);
            if (existingPayment.isPresent()) {
                Payment payment = mapDTOToEntity(paymentDTO);
                payment.setId(id);
                paymentRepository.update(payment, connection);
                return paymentDTO;
            }
        }
        return null;
    }

    public boolean deletePayment(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            if (paymentRepository.findById(id, connection).isPresent()) {
                paymentRepository.delete(id, connection);
                return true;
            }
        }
        return false;
    }

    private PaymentDTO mapEntityToDTO(Payment payment) {
        return new PaymentDTO(
                payment.getId(),
                payment.getPaymentDate(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getInvoiceId()
        );
    }

    private Payment mapDTOToEntity(PaymentDTO dto) {
        return new Payment(
                dto.getId(),
                dto.getPaymentDate(),
                dto.getAmount(),
                dto.getPaymentMethod(),
                dto.getInvoiceId()
        );
    }
}