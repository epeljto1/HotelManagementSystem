package com.example.hotel_management_system.exception;

/**
 * Exception thrown when a reservation already exists in the database
 */
public class InvoiceAlreadyExistsException extends RuntimeException {
    public InvoiceAlreadyExistsException(String status) {
        super("Reservation status is invalid for check-out. Current status: " + status);
    }

    public InvoiceAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

