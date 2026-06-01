package com.example.hotel_management_system.dto;

import com.example.hotel_management_system.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomPackageCreateDTO {
    private String roomNumber;
    private Integer floorNumber;
    private RoomStatus status;
    private Long hotelId;
    private Long roomTypeId;
}
