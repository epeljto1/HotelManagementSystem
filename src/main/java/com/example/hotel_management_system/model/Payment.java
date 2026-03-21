package com.example.hotel_management_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    private Long id;
    private LocalDateTime paymentDate;
    private Double amount;
    private String paymentMethod;
    private Long invoiceId;
}
