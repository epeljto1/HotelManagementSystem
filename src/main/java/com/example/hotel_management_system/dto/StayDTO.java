package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object koji predstavlja realizovani boravak gosta.
 * * <p>Za razliku od rezervacije, {@code StayDTO} barata sa stvarnim vremenima
 * (Check-in i Check-out) i finalnom cijenom koja uključuje sve troškove
 * nastale tokom boravka.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StayDTO {

    /** Jedinstveni identifikator boravka. */
    public Long id;

    /** * Tačno vrijeme prijave gosta.
     * Bilježi se u momentu kada gost preuzme ključeve sobe.
     */
    public LocalDateTime checkInTime;

    /** * Tačno vrijeme odjave gosta.
     * Može biti {@code null} dok je gost još uvijek u hotelu.
     */
    public LocalDateTime checkOutTime;

    /** * Poveznica sa originalnom rezervacijom.
     * Omogućava uvid u planirane naspram ostvarenih datuma.
     */
    public Long reservationId;

    /** * Finalna ukupna cijena boravka.
     * Sumira cijenu smještaja i sve konzumirane dodatne usluge.
     */
    public Double actualTotalPrice;
}