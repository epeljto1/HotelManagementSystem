package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotelDTO {
    private Long id;
    private String name;
    private String description;
    private String phoneNumber;
    private String email;
    private Long addressId;
}

