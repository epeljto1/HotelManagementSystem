package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object za zahtjev odjave gosta (Check-out).
 * * <p>Ovaj DTO nosi podatke neophodne za zatvaranje boravka, obračun finalnih troškova
 * i oslobađanje resursa (sobe). Za razliku od prijave, ovdje je ključno pratiti
 * ko je od zaposlenih izvršio odjavu radi revizije (audit trail).</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutRequestDTO {

    /** * ID rezervacije koja se odjavljuje.
     * Sistem će preko ovog ID-a pronaći povezani {@code Stay} zapis.
     */
    private Long reservationId;

    /** * Stvarno vrijeme odjave.
     * Ukoliko se ne proslijedi, sistem će automatski koristiti trenutno sistemsko vrijeme.
     * Korisno za naknadno evidentiranje odjava koje su se desile ranije.
     */
    private LocalDateTime actualCheckOutTime;

    /** * ID korisnika (recepcionera/administratora) koji vrši proces odjave.
     * Ključno za praćenje odgovornosti unutar hotelskog osoblja.
     */
    private Long performedByUserId;
}