package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

/**
 * Data Transfer Object za prikaz informacija o korisnicima sistema (osoblju).
 * * <p>Ovaj DTO se koristi za administrativne preglede korisnika i profilisanje
 * trenutno prijavljenog korisnika. Dizajniran je tako da ne eksponira osjetljive
 * podatke poput hash-a lozinke prema klijentskom dijelu aplikacije.</p>
 * * @author Tvoje Ime
 * @version 1.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {

    /** Interni ID korisnika u aplikativnoj šemi. */
    private Long id;

    /** Korisničko ime koje se koristi za prijavu na sistem. */
    private String username;

    /** Službena email adresa zaposlenika. */
    private String email;

    /** * Uloga korisnika u sistemu (npr. 'ROLE_ADMIN', 'ROLE_RECEPTIONIST').
     * Određuje nivo pristupa različitim modulima aplikacije.
     */
    private String role;

    /** Datum kada je korisnički nalog kreiran. */
    private LocalDate createdDate;
}