package com.example.hotel_management_system.exception;

/**
 * Exception thrown when a reservation is not in a valid state for check-out
 */
public class InvalidReservationStatusException extends RuntimeException {
    public InvalidReservationStatusException(String status) {
        super("Reservation status is invalid for check-out. Current status: " + status);
    }

    public InvalidReservationStatusException(String message, Throwable cause) {
        super(message, cause);
    }
}

