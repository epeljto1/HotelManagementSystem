package com.example.hotel_management_system.dto;

import com.example.hotel_management_system.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * Data Transfer Object koji predstavlja planirani boravak gosta.
 * * <p>Ovaj DTO je centralni dio sistema za bukiranje. On definiše vremenski okvir
 * zauzeća sobe, broj gostiju i predviđenu cijenu, te služi kao osnova za
 * kasniji prelazak u fazu boravka (Stay).</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservationDTO {

    /** Jedinstveni identifikator rezervacije. */
    private Long id;

    /** Datum kada je rezervacija napravljena u sistemu. */
    private Date reservationDate;

    /** Planirani datum dolaska gosta. */
    private Date checkInDate;

    /** Planirani datum odlaska gosta. */
    private Date checkOutDate;

    /** Broj osoba koje će boraviti u sobi (važno za validaciju kapaciteta). */
    private Integer numberOfGuests;

    /** * Trenutni status rezervacije.
     * Kontroliše životni ciklus (npr. ne može se uraditi Check-in ako status nije CONFIRMED).
     */
    private ReservationStatus status;

    /** * Procjenjena ukupna cijena smještaja.
     * Izračunava se pri kreiranju na osnovu broja noćenja i cijene tipa sobe.
     */
    private Double totalPrice;

    /** ID gosta koji je nosilac rezervacije. */
    private Long guestId;

    /** ID dodijeljene sobe. */
    private Long roomId;

    /** * ID korisnika (zaposlenika) koji je unio rezervaciju u sistem.
     * Omogućava praćenje rada osoblja na recepciji ili u prodaji.
     */
    private Long createdBy;
}