package com.example.hotel_management_system.dto;

import com.example.hotel_management_system.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object koji predstavlja specifičnu smeštajnu jedinicu (sobu).
 * * <p>Ovaj objekat nosi podatke o fizičkoj lokaciji sobe unutar hotela, njenom
 * trenutnom stanju operativnosti i vizuelni prikaz. Povezuje konkretnu sobu
 * sa njenim tipom (karakteristikama) i matičnim hotelom.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomDTO {

    /** Jedinstveni identifikator sobe u bazi podataka. */
    private Long id;

    /** * Oznaka sobe (npr. "101", "A-12").
     * Koristi se String jer neki hoteli koriste slova u oznakama.
     */
    private String roomNumber;

    /** Sprat na kojem se soba nalazi. Korisno za orijentaciju gostiju i osoblja. */
    private Integer floorNumber;

    /** * Trenutni operativni status sobe.
     * Referencira {@link RoomStatus} (npr. AVAILABLE, OCCUPIED, CLEANING, MAINTENANCE).
     */
    private RoomStatus status;

    /** ID hotela kojem soba pripada. */
    private Long hotelId;

    /** * ID kategorije/tipa sobe.
     * Određuje karakteristike poput "Double Room", "Suite", itd.
     */
    private Long roomTypeId;

    /** * Binarni podatak (niz bajtova) koji predstavlja fotografiju sobe.
     * Omogućava prikaz vizuelnog identiteta sobe direktno u aplikaciji.
     */
    private byte[] image;
}