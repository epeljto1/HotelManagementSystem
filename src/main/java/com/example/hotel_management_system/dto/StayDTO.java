package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StayDTO {
    public Long id;
    public LocalDateTime checkInTime;
    public LocalDateTime checkOutTime;
    public Long reservationId;
    public Double actualTotalPrice;
}