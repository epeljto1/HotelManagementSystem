package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object koji služi za povezivanje odabranog popusta sa fakturom.
 * * <p>Ovaj DTO se koristi u momentu kada recepcioner ili sistem odluče da
 * primijene određenu olakšicu (npr. popust za vjerne goste, sezonski popust
 * ili promo kod) na već generisanu fakturu.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DiscountApplyDTO {

    /** * ID fakture na koju se popust primjenjuje.
     * Sistem će na osnovu ovog ID-a ažurirati {@code FINAL_AMOUNT} u bazi.
     */
    private Long invoiceId;

    /** * ID popusta koji se koristi.
     * Referencira tabelu sa definicijama popusta gdje se nalaze procenti ili fiksni iznosi.
     */
    private Long discountId;
}