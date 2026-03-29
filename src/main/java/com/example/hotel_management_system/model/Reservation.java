package com.example.hotel_management_system.model;

import com.example.hotel_management_system.enums.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reservation {
    private Long id;
    private Date reservationDate;
    private Date checkInDate;
    private Date checkOutDate;
    private Integer numberOfGuests;
    private ReservationStatus status;
    private Double totalPrice;
    private Long guestId;
    private Long roomId;
    private Long createdBy;
}
