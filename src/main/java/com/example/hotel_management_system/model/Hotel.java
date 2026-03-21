package com.example.hotel_management_system.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hotel {
    private Long id;
    private String name;
    private String description;
    private String phoneNumber;
    private String email;
    private Long addressId;
}

