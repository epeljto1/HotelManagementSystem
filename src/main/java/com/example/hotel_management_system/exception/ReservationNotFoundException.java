package com.example.hotel_management_system.exception;

/**
 * Exception thrown when a reservation is not found
 */
public class ReservationNotFoundException extends RuntimeException {
    public ReservationNotFoundException(Long reservationId) {
        super("Reservation not found with id: " + reservationId);
    }

    public ReservationNotFoundException(String message) {
        super(message);
    }
}

