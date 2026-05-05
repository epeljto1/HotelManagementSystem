package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Data Transfer Object koji nosi lične podatke o gostu hotela.
 * * <p>Ovaj DTO se koristi prilikom registracije novih gostiju u sistem,
 * kao i za prikaz podataka na listama gostiju. Sadrži osjetljive informacije
 * poput broja dokumenta, što je neophodno za zakonsku prijavu boravka.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestDTO {

    /** Jedinstveni identifikator gosta u sistemu. */
    private Long id;

    /** Ime gosta. */
    private String firstName;

    /** Prezime gosta. */
    private String lastName;

    /** Email adresa za slanje potvrda rezervacija i faktura. */
    private String email;

    /** Kontakt telefon gosta. */
    private String phoneNumber;

    /** Datum rođenja (bitno zbog zakonskih regulativa i provjere punoljetstva). */
    private Date dateOfBirth;

    /** * Broj identifikacionog dokumenta (pasoš ili lična karta).
     * Ključno polje za policijsku prijavu turista.
     */
    private String documentNumber;

    /** * Strani ključ ka entitetu adrese.
     * Omogućava uvid u to odakle gost dolazi bez pretrpavanja Guest tabele.
     */
    private Long addressId;
}