package com.example.hotel_management_system.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.InvoiceDTO;
import com.example.hotel_management_system.model.Invoice;
import com.example.hotel_management_system.repository.InvoiceRepository;
import org.springframework.stereotype.Service;
import com.example.hotel_management_system.model.Discount;
import com.example.hotel_management_system.repository.DiscountRepository;
import java.util.Optional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servisni sloj zadužen za upravljanje fakturama (računima) unutar sistema.
 * Ova klasa implementira ključnu poslovnu logiku za obračun finalnog iznosa,
 * automatsku i manuelnu primjenu popusta, te validaciju statusa plaćanja računa.
 * * <p>Sve finansijske operacije koriste {@link BigDecimal} sa {@link RoundingMode#HALF_UP}
 * kako bi se osigurala preciznost u dvije decimale.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Service
public class InvoiceService {

    private final InvoiceRepository repository;
    private final DiscountRepository discountRepository;

    /**
     * Konstruktor za Dependency Injection.
     * * @param repository Repozitorij za perzistenciju računa.
     * @param discountRepository Repozitorij za dobavljanje podataka o popustima.
     */
    public InvoiceService(InvoiceRepository repository, DiscountRepository discountRepository) {
        this.repository = repository;
        this.discountRepository = discountRepository;
    }

    /**
     * Dobavlja sve fakture iz baze podataka.
     * * @return List<InvoiceDTO> Lista svih faktura mapiranih u DTO format.
     * @throws RuntimeException Omotač za SQLException pri fetch-ovanju podataka.
     */
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

    /**
     * Pronalazi fakturu na osnovu njenog ID-a.
     * * @param id Jedinstveni identifikator fakture.
     * @return InvoiceDTO Objekt fakture ili null ako nije pronađen.
     */
    public InvoiceDTO findById(Long id) {
        try (Connection connection = DbConfig.getConnection()) {
            Invoice invoice = repository.findById(id, connection);
            return invoice != null ? toDTO(invoice) : null;
        } catch (SQLException e) {
            throw new RuntimeException("Error while fetching invoice by id.", e);
        }
    }

    /**
     * Spašava novu fakturu uz automatsku provjeru i primjenu aktivnog popusta.
     * Metoda provjerava postoji li važeći popust za datum izdavanja računa
     * i shodno tome izračunava finalni iznos.
     * * @param dto Podaci o fakturi primljeni preko API-ja.
     * @throws RuntimeException Ako status nije validan ili dođe do SQL greške.
     */
    public void save(InvoiceDTO dto) {
        validateStatus(dto.getStatus());

        try (Connection connection = DbConfig.getConnection()) {
            Invoice invoice = toModel(dto);

            // Automatsko traženje aktivnog popusta za datum izdavanja
            Optional<Discount> activeDiscount =
                    discountRepository.findActiveDiscountByDate(java.sql.Date.valueOf(invoice.getIssueDate()), connection);

            if (activeDiscount.isPresent()) {
                Discount discount = activeDiscount.get();

                // Formula: discountAmount = total * (percentage / 100)
                BigDecimal discountAmount = invoice.getTotalAmount()
                        .multiply(BigDecimal.valueOf(discount.getPercentage()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                BigDecimal finalAmount = invoice.getTotalAmount().subtract(discountAmount);

                invoice.setDiscountId(discount.getId());
                invoice.setDiscountAmount(discountAmount);
                invoice.setFinalAmount(finalAmount);
            } else {
                invoice.setDiscountId(null);
                invoice.setDiscountAmount(BigDecimal.ZERO);
                invoice.setFinalAmount(invoice.getTotalAmount());
            }

            repository.save(invoice, connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error while saving invoice.", e);
        }
    }

    /**
     * Ažurira podatke o postojećoj fakturi.
     * * @param id ID fakture koja se mijenja.
     * @param dto Novi podaci o fakturi.
     */
    public void update(Long id, InvoiceDTO dto) {
        validateStatus(dto.getStatus());

        try (Connection connection = DbConfig.getConnection()) {
            repository.update(id, toModel(dto), connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error while updating invoice.", e);
        }
    }

    /**
     * Briše fakturu iz baze podataka.
     * * @param id ID fakture za brisanje.
     */
    public void delete(Long id) {
        try (Connection connection = DbConfig.getConnection()) {
            repository.delete(id, connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error while deleting invoice.", e);
        }
    }

    /**
     * Interna validacija dozvoljenih statusa za fakturu.
     * Dozvoljene vrijednosti su: "Unpaid", "Partially paid", "Paid".
     * * @param status Tekstualni status fakture.
     */
    private void validateStatus(String status) {
        if (status == null ||
                (!status.equals("Unpaid")
                        && !status.equals("Partially paid")
                        && !status.equals("Paid"))) {
            throw new RuntimeException("Invalid status! Allowed values are: Unpaid, Partially paid, Paid");
        }
    }

    /**
     * Omogućava manuelnu primjenu specifičnog popusta na već postojeću fakturu.
     * Ponovo izračunava popust i finalni iznos te ažurira zapis u bazi.
     * * @param invoiceId ID fakture.
     * @param discountId ID popusta koji se primjenjuje.
     */
    public void applyDiscountManually(Long invoiceId, Long discountId) {
        try (Connection connection = DbConfig.getConnection()) {
            Invoice invoice = repository.findById(invoiceId, connection);
            if (invoice == null) {
                throw new RuntimeException("Invoice not found.");
            }

            Optional<Discount> optionalDiscount = discountRepository.findById(discountId, connection);
            if (optionalDiscount.isEmpty()) {
                throw new RuntimeException("Discount not found.");
            }

            Discount discount = optionalDiscount.get();

            BigDecimal discountAmount = invoice.getTotalAmount()
                    .multiply(BigDecimal.valueOf(discount.getPercentage()))
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            BigDecimal finalAmount = invoice.getTotalAmount().subtract(discountAmount);

            invoice.setDiscountId(discount.getId());
            invoice.setDiscountAmount(discountAmount);
            invoice.setFinalAmount(finalAmount);

            repository.update(invoiceId, invoice, connection);
        } catch (SQLException e) {
            throw new RuntimeException("Error while applying discount.", e);
        }
    }

    /**
     * Konvertuje model {@link Invoice} u {@link InvoiceDTO}.
     */
    private InvoiceDTO toDTO(Invoice invoice) {
        return new InvoiceDTO(
                invoice.getId(),
                invoice.getIssueDate(),
                invoice.getTotalAmount(),
                invoice.getStatus(),
                invoice.getStayId(),
                invoice.getDiscountId(),
                invoice.getDiscountAmount(),
                invoice.getFinalAmount()
        );
    }

    /**
     * Konvertuje {@link InvoiceDTO} u model {@link Invoice}.
     * Napomena: PDF polje se ne mapira ovdje jer se generiše kroz poseban proces.
     */
    private Invoice toModel(InvoiceDTO dto) {
        Invoice invoice = new Invoice();
        invoice.setId(dto.getId());
        invoice.setIssueDate(dto.getIssueDate());
        invoice.setTotalAmount(dto.getTotalAmount());
        invoice.setStatus(dto.getStatus());
        invoice.setStayId(dto.getStayId());
        invoice.setDiscountId(dto.getDiscountId());
        invoice.setDiscountAmount(dto.getDiscountAmount());
        invoice.setFinalAmount(dto.getFinalAmount());
        return invoice;
    }
}