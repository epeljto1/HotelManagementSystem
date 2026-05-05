package com.example.hotel_management_system.dto;

import lombok.Data;

/**
 * Data Transfer Object za registraciju novog korisnika (zaposlenika) u sistem.
 * * <p>Ovaj objekt prikuplja sve neophodne informacije za kreiranje korisničkog računa,
 * uključujući lozinku koja će biti obrađena (hash-ovana) prije spremanja u bazu.
 * Objedinjuje podatke za Authentication (User) i Personalne (Profile) tabele.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
public class UserRegistrationDTO {

    /** Interni ID (uglavnom se koristi kod izmjena postojećeg profila). */
    private Long userId;

    /** Željeno korisničko ime za prijavu. */
    private String username;

    /** Službeni email zaposlenika. */
    private String email;

    /** * Lozinka u plain-text formatu.
     * NAPOMENA: Ovaj podatak se koristi samo u tranzitu i nikada se ne vraća u odgovorima API-ja.
     */
    private String password;

    /** Naziv uloge (npr. "RECEPTIONIST"). */
    private String role;

    /** Numerički ID uloge iz baze podataka (npr. 1 za ADMIN). */
    private Long roleId;

    /** Stvarno ime zaposlenika. */
    private String firstName;

    /** Prezime zaposlenika. */
    private String lastName;
}