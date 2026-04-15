package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for check-out response with invoice details
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckOutResponseDTO {
    // Reservation Info
    private Long reservationId;
    private Long guestId;
    private Long roomId;
    private String roomNumber;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Integer numberOfNights;
    
    // Room Type Info
    private String roomTypeName;
    private BigDecimal pricePerNight;
    
    // Invoice Details
    private Long invoiceId;
    private BigDecimal accommodationCost;
    private BigDecimal additionalServicesCost;
    private BigDecimal subtotal;
    
    // Discount Info
    private Long discountId;
    private String discountName;
    private BigDecimal discountPercentage;
    private BigDecimal discountAmount;
    
    // Final Amount
    private BigDecimal finalAmount;
    
    // Status Updates
    private String invoiceStatus;
    private String roomStatus;
    private String reservationStatus;
}

