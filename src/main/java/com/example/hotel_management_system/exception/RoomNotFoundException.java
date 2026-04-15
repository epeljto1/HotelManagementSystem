package com.example.hotel_management_system.exception;

/**
 * Exception thrown when a room is not found
 */
public class RoomNotFoundException extends RuntimeException {
    public RoomNotFoundException(Long roomId) {
        super("Room not found with id: " + roomId);
    }

    public RoomNotFoundException(String message) {
        super(message);
    }
}

