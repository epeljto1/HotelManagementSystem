package com.example.hotel_management_system.model;

import com.example.hotel_management_system.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Room {

    private Long id;
    private String roomNumber;
    private Integer floorNumber;
    private RoomStatus status;
    private Long hotelId;
    private Long roomTypeId;

    private byte[] image;
}
