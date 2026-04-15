package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for check-out request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckOutRequestDTO {
    // The ID of the reservation to check out
    private Long reservationId;
    
    // The actual check-out timestamp (optional - if not provided, current time is used)
    private LocalDateTime actualCheckOutTime;
    
    // The ID of the user performing the check-out (receptionist)
    private Long performedByUserId;
}

