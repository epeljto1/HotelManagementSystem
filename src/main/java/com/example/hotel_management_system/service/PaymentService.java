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

/**
 * Servisni sloj zadužen za procesiranje uplata gostiju.
 * Glavna odgovornost ovog servisa je evidentiranje transakcija i automatsko
 * ažuriranje statusa povezane fakture na osnovu ukupnog uplaćenog iznosa.
 * * <p>Podržane metode plaćanja su fiksne i validiraju se prilikom svakog unosa.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Service
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    /** Lista dozvoljenih metoda plaćanja definisana poslovnim pravilima hotela. */
    private static final List<String> ALLOWED_METHODS = List.of(
            "Cash", "Debit Card", "Credit Card", "Bank transfer"
    );

    /**
     * Konstruktor za Dependency Injection.
     */
    public PaymentService(PaymentRepository paymentRepository, InvoiceRepository invoiceRepository) {
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    /**
     * Kreira novu uplatu i vrši rekalkulaciju statusa fakture.
     * Logika provjerava sumu svih dosadašnjih uplata za određeni račun:
     * <ul>
     * <li>Ako je suma >= iznosu računa, status postaje "Paid".</li>
     * <li>Ako je suma > 0, status postaje "Partially paid".</li>
     * <li>U suprotnom ostaje "Unpaid".</li>
     * </ul>
     * * @param paymentDTO Podaci o uplati (iznos, metoda, ID fakture).
     * @return PaymentDTO Vraća podatke o izvršenoj uplati.
     * @throws SQLException U slučaju greške pri radu sa bazom podataka.
     * @throws IllegalArgumentException Ako metoda plaćanja nije na listi dozvoljenih.
     */
    public PaymentDTO createPayment(PaymentDTO paymentDTO) throws SQLException {
        if (paymentDTO.getPaymentMethod() == null ||
                !ALLOWED_METHODS.contains(paymentDTO.getPaymentMethod())) {
            throw new IllegalArgumentException("Invalid payment method. Allowed methods are: " + ALLOWED_METHODS);
        }

        Payment payment = mapDTOToEntity(paymentDTO);

        try (Connection connection = DbConfig.getConnection()) {

            // 1. Perzistencija uplate
            paymentRepository.save(payment, connection);

            // 2. Dobavljanje povezane fakture radi ažuriranja statusa
            Invoice invoice = invoiceRepository.findById(payment.getInvoiceId(), connection);

            if (invoice != null) {
                // 3. Agregacija svih uplata za datu fakturu
                double totalPaid = paymentRepository.getTotalPaidForInvoice(payment.getInvoiceId(), connection);

                // 4. Provjera ukupnog duga
                double totalAmount = invoice.getTotalAmount().doubleValue();

                // 5. Logika za određivanje novog statusa fakture
                String status;
                if (totalPaid >= totalAmount) {
                    status = "Paid";
                } else if (totalPaid > 0) {
                    status = "Partially paid";
                } else {
                    status = "Unpaid";
                }

                // 6. Ažuriranje statusa entiteta
                invoice.setStatus(status);

                // 7. Spašavanje promjena statusa u bazu podataka
                invoiceRepository.update(invoice.getId(), invoice, connection);
            }
        }

        return paymentDTO;
    }

    /**
     * Pronalazi specifičnu uplatu na osnovu njenog ID-a.
     * * @param id ID uplate.
     * @return PaymentDTO Podaci o uplati ili null ako nije pronađena.
     * @throws SQLException SQL greška.
     */
    public PaymentDTO getPaymentById(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            Optional<Payment> payment = paymentRepository.findById(id, connection);
            return payment.map(this::mapEntityToDTO).orElse(null);
        }
    }

    /**
     * Vraća listu svih evidentiranih transakcija u sistemu.
     * * @return List<PaymentDTO> Lista svih uplata.
     * @throws SQLException SQL greška.
     */
    public List<PaymentDTO> getAllPayments() throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            List<Payment> payments = paymentRepository.findAll(connection);
            return payments.stream().map(this::mapEntityToDTO).collect(Collectors.toList());
        }
    }

    /**
     * Ažurira podatke o postojećoj uplati.
     * * @param id ID uplate koju mijenjamo.
     * @param paymentDTO Novi podaci.
     * @return PaymentDTO Ažurirani objekt.
     * @throws SQLException SQL greška.
     */
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

    /**
     * Briše zapis o uplati iz baze podataka.
     * * @param id ID uplate za brisanje.
     * @return boolean True ako je obrisano, false inače.
     * @throws SQLException SQL greška.
     */
    public boolean deletePayment(Long id) throws SQLException {
        try (Connection connection = DbConfig.getConnection()) {
            if (paymentRepository.findById(id, connection).isPresent()) {
                paymentRepository.delete(id, connection);
                return true;
            }
        }
        return false;
    }

    /**
     * Mapira entitet u DTO za potrebe API odgovora.
     */
    private PaymentDTO mapEntityToDTO(Payment payment) {
        return new PaymentDTO(
                payment.getId(),
                payment.getPaymentDate(),
                payment.getAmount(),
                payment.getPaymentMethod(),
                payment.getInvoiceId()
        );
    }

    /**
     * Mapira DTO u entitet spreman za bazu podataka.
     */
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