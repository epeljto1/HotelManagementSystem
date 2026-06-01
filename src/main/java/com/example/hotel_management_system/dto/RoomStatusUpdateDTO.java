package com.example.hotel_management_system.dto;

import com.example.hotel_management_system.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusUpdateDTO {
    private RoomStatus status;
}
