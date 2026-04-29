package com.example.hotel_management_system.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class ExceptionClassesTest {

    @Test
    void reservationNotFoundExceptionBuildsExpectedMessages() {
        assertEquals("Reservation not found with id: 7", new ReservationNotFoundException(7L).getMessage());
        assertEquals("custom", new ReservationNotFoundException("custom").getMessage());
    }

    @Test
    void roomNotFoundExceptionBuildsExpectedMessages() {
        assertEquals("Room not found with id: 9", new RoomNotFoundException(9L).getMessage());
        assertEquals("custom room", new RoomNotFoundException("custom room").getMessage());
    }

    @Test
    void invalidReservationStatusExceptionBuildsExpectedMessages() {
        RuntimeException cause = new RuntimeException("cause");

        assertEquals("Reservation status is invalid for check-out. Current status: PENDING",
                new InvalidReservationStatusException("PENDING").getMessage());
        InvalidReservationStatusException exception = new InvalidReservationStatusException("custom", cause);
        assertEquals("custom", exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void invoiceAlreadyExistsExceptionBuildsExpectedMessages() {
        RuntimeException cause = new RuntimeException("cause");

        assertEquals("Reservation status is invalid for check-out. Current status: DUPLICATE",
                new InvoiceAlreadyExistsException("DUPLICATE").getMessage());
        InvoiceAlreadyExistsException exception = new InvoiceAlreadyExistsException("custom", cause);
        assertEquals("custom", exception.getMessage());
        assertSame(cause, exception.getCause());
    }
}
