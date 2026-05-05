package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object koji definiše kategorizaciju i cjenovnik hotelskih soba.
 * * <p>Ovaj model služi za upravljanje različitim tipovima smještaja. On je ključan
 * za proces rezervacije jer definiše maksimalni kapacitet (koliko osoba može stati)
 * i osnovnu cijenu po kojoj se vrši kasniji finansijski obračun.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeDTO {

    /** Jedinstveni identifikator tipa sobe (npr. ID za "Standard Double"). */
    private Long id;

    /** Naziv kategorije (npr. "Penthouse", "Single Room", "Deluxe Suite"). */
    private String name;

    /** Detaljan opis pogodnosti (npr. "Sadrži mini-bar, balkon i king-size krevet"). */
    private String description;

    /** * Maksimalni broj osoba koji može boraviti u ovom tipu sobe.
     * Koristi se za validaciju prilikom kreiranja rezervacije.
     */
    private Integer capacity;

    /** * Osnovna cijena noćenja za ovaj tip sobe.
     * Ova vrijednost je polazna tačka za kalkulaciju u {@code InvoiceDTO}.
     */
    private Double pricePerNight;
}