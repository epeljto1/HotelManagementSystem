package com.example.hotel_management_system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO koji služi za definisanje marketinških kampanja i popusta u sistemu.
 * * <p>Sadrži ugrađenu validaciju poslovnih pravila (npr. opseg procenta)
 * i metapodatke za automatsko generisanje Swagger dokumentacije.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Model za kreiranje i prikaz informacija o popustu")
public class DiscountDTO {

    /** * Jedinstveni identifikator popusta.
     * Postavljen na READ_ONLY jer ga generiše baza, ne unosi ga korisnik.
     */
    @Schema(accessMode = Schema.AccessMode.READ_ONLY, description = "Jedinstveni identifikator popusta")
    private Long id;

    /** * Naziv popusta. Obavezan polje radi lakše pretrage u administraciji. */
    @NotBlank(message = "Naziv popusta je obavezan")
    @Schema(description = "Naziv popusta (npr. First Minute, Last Minute)", example = "Sezonski popust 2026")
    private String name;

    /** * Procenat umanjenja cijene. Validiran na opseg od 0 do 100. */
    @NotNull(message = "Procenat je obavezan")
    @Min(value = 0, message = "Procenat ne može biti manji od 0")
    @Max(value = 100, message = "Procenat ne može biti veći od 100")
    @Schema(description = "Procenat popusta (0.0 do 100.0)", example = "15.0")
    private Double percentage;

    /** * Datum početka važenja popusta. */
    @NotNull(message = "Početni datum je obavezan")
    @Schema(description = "Datum kada popust postaje aktivan", example = "2026-06-01")
    private LocalDate startDate;

    /** * Datum isteka važenja popusta. */
    @NotNull(message = "Krajnji datum je obavezan")
    @Schema(description = "Datum kada popust prestaje da važi", example = "2026-08-31")
    private LocalDate endDate;

    /** * Opcioni opis kampanje ili uslova korištenja. */
    @Schema(description = "Dodatni detalji o popustu", example = "Dostupno za sve rezervacije napravljene tokom ljetne sezone.")
    private String description;
}