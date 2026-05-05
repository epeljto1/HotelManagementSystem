package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object za definisanje dodatnih hotelskih usluga.
 * * <p>Ovaj DTO se koristi za upravljanje katalogom usluga koje gost može
 * konzumirati tokom boravka, a koje nisu uključene u osnovnu cijenu noćenja.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtraServiceDTO {

    /** Jedinstveni identifikator usluge u bazi podataka. */
    public Long id;

    /** Naziv usluge (npr. "Spa centar", "Doručak u sobi", "Parking"). */
    public String name;

    /** Detaljan opis usluge i šta ona obuhvata. */
    public String description;

    /** Cijena po jedinici mjere (npr. cijena po satu, po osobi ili po narudžbi). */
    public Double unitPrice;

    /** * Trenutna dostupnost usluge.
     * Može se koristiti kao status (npr. "AVAILABLE", "NOT_AVAILABLE", "MAINTENANCE").
     */
    public String available;
}