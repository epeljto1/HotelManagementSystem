package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object koji definiše osnovne informacije o hotelskom objektu.
 * * <p>Ovaj DTO se koristi za konfiguraciju hotela unutar sistema, prikazivanje
 * kontakt informacija na zaglavljima faktura i upravljanje osnovnim postavkama
 * identiteta objekta.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelDTO {

    /** Jedinstveni identifikator hotela. */
    private Long id;

    /** Naziv hotela (npr. "Grand Hotel Sarajevo"). */
    private String name;

    /** Kratak opis hotela, vizija ili promotivni tekst. */
    private String description;

    /** Zvanični kontakt telefon recepcije ili rezervacija. */
    private String phoneNumber;

    /** Zvanična email adresa hotela za korespodenciju. */
    private String email;

    /** * Strani ključ ka entitetu adrese.
     * Povezuje hotel sa fizičkom lokacijom (grad, država, ulica).
     */
    private Long addressId;
}