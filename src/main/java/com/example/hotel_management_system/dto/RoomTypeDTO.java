package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeDTO {
    private Long id;
    private String name;
    private String description;
    private Integer capacity;
    private Double pricePerNight;
}