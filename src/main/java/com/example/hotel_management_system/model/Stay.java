package com.example.hotel_management_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stay {
    public Long id;
    public LocalDateTime checkInTime;
    public LocalDateTime checkOutTime;
    public Long reservationId;
    public Double actualTotalPrice;
}

