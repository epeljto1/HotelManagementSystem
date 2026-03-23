package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExtraServiceDTO {
    public Long id;
    public String name;
    public String description;
    public Double unitPrice;
    public String available;
}
