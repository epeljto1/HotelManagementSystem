package com.example.hotel_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object koji beleži fizičku transakciju uplate.
 * * <p>Ovaj objekat je ključan za finansijsku evidenciju (zatvaranje računa).
 * On povezuje izvršenu uplatu sa fakturom i definiše metodu plaćanja,
 * što je neophodno za dnevne izveštaje blagajne.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Model za evidentiranje izvršene uplate")
public class PaymentDTO {

    /** Jedinstveni identifikator uplate. Generiše ga sistem. */
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Jedinstveni identifikator uplate")
    private Long id;

    /** Tačan trenutak kada je novac primljen. */
    @NotNull(message = "Datum uplate je obavezan")
    @Schema(description = "Datum i vreme izvršenja uplate", example = "2026-04-12T15:00:00")
    private LocalDateTime paymentDate;

    /** Iznos uplate. Validacija osigurava da cifra ne može biti negativna ili nula. */
    @Positive(message = "Iznos mora biti veći od nule")
    @Schema(description = "Iznos novca koji je uplaćen", example = "150.50")
    private Double amount;

    /** * Način na koji je uplata izvršena.
     * Dozvoljene vrednosti su jasno definisane radi lakšeg filtriranja izveštaja.
     */
    @NotBlank(message = "Metoda plaćanja je obavezna")
    @Schema(
            description = "Metoda korišćena za plaćanje",
            allowableValues = {"Cash", "Debit Card", "Credit Card", "Bank transfer"},
            example = "Cash"
    )
    private String paymentMethod;

    /** * Veza sa fakturom.
     * Plaćanje uvek mora biti referencirano na konkretan račun.
     */
    @NotNull(message = "ID fakture je obavezan")
    @Schema(description = "ID povezane fakture", example = "1")
    private Long invoiceId;
}