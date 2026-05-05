package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object koji se koristi za iniciranje procesa prijave gosta (Check-in).
 * * <p>Ovaj DTO je dizajniran da bude maksimalno lagan, zahtijevajući samo
 * identifikator rezervacije. Svi ostali podaci potrebni za kreiranje boravka
 * (Stay) se izvlače iz baze podataka na strani servera radi sigurnosti i integriteta.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckInDTO {

    /** * Jedinstveni identifikator potvrđene rezervacije.
     * Na osnovu ovog ID-a, sistem mijenja status rezervacije i sobe.
     */
    private Long reservationId;
}