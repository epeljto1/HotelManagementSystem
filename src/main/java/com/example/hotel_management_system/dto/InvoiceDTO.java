package com.example.hotel_management_system.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object koji predstavlja finansijski račun (fakturu) za boravak gosta.
 * * <p>Ovaj DTO služi za prikazivanje i prenos podataka o naplati. On sumira sve troškove
 * boravka, primjenjuje popuste i definiše konačni iznos koji gost treba platiti.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
public class InvoiceDTO {

    /** Jedinstveni identifikator fakture. */
    private Long id;

    /** Datum izdavanja računa (obično datum Check-out operacije). */
    private LocalDate issueDate;

    /** * Ukupni akumulirani iznos prije popusta.
     * Uključuje cijenu smještaja i sve dodatne usluge.
     */
    private BigDecimal totalAmount;

    /** Trenutni status računa (npr. 'PENDING', 'PAID', 'CANCELLED'). */
    private String status;

    /** Referenca na konkretan boravak na koji se račun odnosi. */
    private Long stayId;

    /** ID primijenjenog popusta (ako postoji). */
    private Long discountId;

    /** Izračunata vrijednost popusta u novčanim jedinicama. */
    private BigDecimal discountAmount;

    /** * Konačni iznos za uplatu.
     * Dobija se formulom: {@code totalAmount - discountAmount}.
     */
    private BigDecimal finalAmount;

    /** Standardni default konstruktor neophodan za JSON serijalizaciju. */
    public InvoiceDTO() {
    }

    /** Konstruktor za kreiranje inicijalne fakture bez popusta. */
    public InvoiceDTO(Long id, LocalDate issueDate, BigDecimal totalAmount, String status, Long stayId) {
        this.id = id;
        this.issueDate = issueDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.stayId = stayId;
    }

    /** Sveobuhvatni konstruktor za fakture sa obračunatim popustima. */
    public InvoiceDTO(Long id, LocalDate issueDate, BigDecimal totalAmount, String status, Long stayId,
                      Long discountId, BigDecimal discountAmount, BigDecimal finalAmount) {
        this.id = id;
        this.issueDate = issueDate;
        this.totalAmount = totalAmount;
        this.status = status;
        this.stayId = stayId;
        this.discountId = discountId;
        this.discountAmount = discountAmount;
        this.finalAmount = finalAmount;
    }

    // --- Getters & Setters ---
    // (Ostavljamo ih eksplicitnim radi precizne kontrole nad finansijskim poljima)

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getIssueDate() { return issueDate; }
    public void setIssueDate(LocalDate issueDate) { this.issueDate = issueDate; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getStayId() { return stayId; }
    public void setStayId(Long stayId) { this.stayId = stayId; }

    public Long getDiscountId() { return discountId; }
    public void setDiscountId(Long discountId) { this.discountId = discountId; }

    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }

    public BigDecimal getFinalAmount() { return finalAmount; }
    public void setFinalAmount(BigDecimal finalAmount) { this.finalAmount = finalAmount; }
}