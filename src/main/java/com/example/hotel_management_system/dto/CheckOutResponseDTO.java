package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Sveobuhvatni DTO koji predstavlja finalni račun i izvještaj nakon odjave.
 * * <p>Ovaj objekt služi kao podloga za generisanje digitalne fakture gosti.
 * Agregira podatke o smještaju, konzumiranim uslugama, primijenjenim popustima
 * i finalnim statusima svih povezanih entiteta.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckOutResponseDTO {

    // --- Informacije o Rezervaciji ---
    private Long reservationId;
    private Long guestId;
    private String guestFullName;
    private Long roomId;
    private String roomNumber;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Integer numberOfNights; // Izračunato na osnovu check-in/out vremena

    // --- Detalji o Tipu Sobe ---
    private String roomTypeName;
    private BigDecimal pricePerNight;

    // --- Finansijski Obračun (Faktura) ---
    private Long invoiceId;
    private BigDecimal accommodationCost;      // (numberOfNights * pricePerNight)
    private BigDecimal additionalServicesCost; // Suma svih ServiceUsage stavki
    private BigDecimal subtotal;               // accommodation + additionalServices

    // --- Informacije o Popustu ---
    private Long discountId;
    private String discountName;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;        // Iznos koji se oduzima od subtotal-a

    // --- Finalni Iznos za Plaćanje ---
    private BigDecimal finalAmount;           // subtotal - discountAmount

    // --- Statusni Podaci (Potvrda operacije) ---
    private String invoiceStatus;     // npr. "ISSUED" ili "PAID"
    private String roomStatus;        // npr. "CLEANING_REQUIRED" ili "AVAILABLE"
    private String reservationStatus; // npr. "COMPLETED"

    /** Detaljan prikaz svake pojedinačne dodatne usluge (mini-bar, spa, itd.) */
    private List<ServiceUsageDTO> serviceDetails;
}