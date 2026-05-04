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
import java.math.RoundingMode;
import java.util.Optional;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InvoiceService {

    private final InvoiceRepository repository;
    private final DiscountRepository discountRepository;

    public InvoiceService(InvoiceRepository repository, DiscountRepository discountRepository) {
        this.repository = repository;
        this.discountRepository = discountRepository;
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
            Invoice invoice = toModel(dto);

            Optional<Discount> activeDiscount =
                    discountRepository.findActiveDiscountByDate(java.sql.Date.valueOf(invoice.getIssueDate()), connection);

            if (activeDiscount.isPresent()) {
                Discount discount = activeDiscount.get();

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
        // invoicePdf ostaje null jer se generiše naknadno
        return invoice;
    }

}