package com.example.hotel_management_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class XmlExportDTO {

    private Long guestId;
    private String guestName;

    private Long reservationId;
    private String reservationStatus;

    private Long roomId;
    private String roomNumber;
    private String roomStatus;
}