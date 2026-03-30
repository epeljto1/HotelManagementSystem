package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.PaymentDTO;
import com.example.hotel_management_system.model.Invoice;
import com.example.hotel_management_system.model.Payment;
import com.example.hotel_management_system.repository.InvoiceRepository;
import com.example.hotel_management_system.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public PaymentDTO createPayment(PaymentDTO paymentDTO) throws SQLException {
        Payment payment = mapDTOToEntity(paymentDTO);

        try (Connection connection = DbConfig.getConnection()) {

            // 1. Snimi payment
            paymentRepository.save(payment, connection);

            // 2. Uzmi invoice
            Invoice invoice = invoiceRepository.findById(payment.getInvoiceId(), connection);

            if (invoice != null) {

                // 3. Ukupno uplaćeno
                double totalPaid = paymentRepository.getTotalPaidForInvoice(payment.getInvoiceId(), connection);

                // 4. Ukupan iznos računa
                double totalAmount = invoice.getTotalAmount().doubleValue();

                // 5. Odredi status
                String status;
                if (totalPaid >= totalAmount) {
                    status = "Paid";
                } else if (totalPaid > 0) {
                    status = "Partially paid";
                } else {
                    status = "Unpaid";
                }

                // 6. Postavi novi status
                invoice.setStatus(status);

                // 7. Update invoice
                invoiceRepository.update(invoice.getId(), invoice, connection);
            }
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