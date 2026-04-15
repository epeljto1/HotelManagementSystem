package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.CheckOutRequestDTO;
import com.example.hotel_management_system.dto.CheckOutResponseDTO;
import com.example.hotel_management_system.enums.ReservationStatus;
import com.example.hotel_management_system.enums.RoomStatus;
import com.example.hotel_management_system.exception.InvoiceAlreadyExistsException;
import com.example.hotel_management_system.exception.InvalidReservationStatusException;
import com.example.hotel_management_system.exception.ReservationNotFoundException;
import com.example.hotel_management_system.exception.RoomNotFoundException;
import com.example.hotel_management_system.model.*;
import com.example.hotel_management_system.repository.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling guest check-out and invoice generation
 */
@Service
public class CheckOutService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final StayRepository stayRepository;
    private final InvoiceRepository invoiceRepository;
    private final ServiceUsageRepository serviceUsageRepository;
    private final DiscountRepository discountRepository;

    public CheckOutService(
            ReservationRepository reservationRepository,
            RoomRepository roomRepository,
            RoomTypeRepository roomTypeRepository,
            StayRepository stayRepository,
            InvoiceRepository invoiceRepository,
            ServiceUsageRepository serviceUsageRepository,
            DiscountRepository discountRepository) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.stayRepository = stayRepository;
        this.invoiceRepository = invoiceRepository;
        this.serviceUsageRepository = serviceUsageRepository;
        this.discountRepository = discountRepository;
    }

    /**
     * Process guest check-out with invoice generation
     * 
     * This is a transactional operation that:
     * 1. Validates the reservation exists and is in CONFIRMED state
     * 2. Creates/updates the stay record with actual check-out time
     * 3. Calculates invoice with:
     *    - Accommodation cost = nights × price per night
     *    - Additional services cost
     *    - Applies active discount if available
     * 4. Updates room status to AVAILABLE
     * 5. Updates reservation status to COMPLETED
     * 
     * @param request Check-out request details
     * @return Check-out response with invoice breakdown
     * @throws ReservationNotFoundException if reservation doesn't exist
     * @throws InvalidReservationStatusException if reservation is not CONFIRMED
     * @throws RoomNotFoundException if room doesn't exist
     * @throws InvoiceAlreadyExistsException if invoice already exists for the stay
     */
    public CheckOutResponseDTO processCheckOut(CheckOutRequestDTO request) throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            // Start transaction
            conn.setAutoCommit(false);
            try {
                // Step 1: Validate and fetch reservation
                Reservation reservation = reservationRepository.findById(request.getReservationId(), conn)
                    .orElseThrow(() -> new ReservationNotFoundException(request.getReservationId()));

                // Verify reservation is in CONFIRMED status (checked in)
                if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
                    throw new InvalidReservationStatusException(
                        "Only CONFIRMED reservations can be checked out. Current status: " + reservation.getStatus()
                    );
                }

                // Step 2: Fetch room and validate
                Room room = roomRepository.findById(reservation.getRoomId(), conn)
                    .orElseThrow(() -> new RoomNotFoundException(reservation.getRoomId()));

                // Step 3: Fetch room type for pricing
                RoomType roomType = roomTypeRepository.findById(room.getRoomTypeId(), conn)
                    .orElseThrow(() -> new RuntimeException("Room type not found with id: " + room.getRoomTypeId()));

                // Step 4: Determine actual check-out time
                LocalDateTime checkOutTime = request.getActualCheckOutTime() != null 
                    ? request.getActualCheckOutTime() 
                    : LocalDateTime.now();

                // Step 5: Get or create Stay record
                Stay stay = stayRepository.findById(reservation.getId(), conn).orElse(null);
                if (stay == null) {
                    // Create new stay record
                    stay = new Stay();
                    stay.setId(reservation.getId());
                    stay.setReservationId(reservation.getId());
                    stay.setCheckInTime(convertToLocalDateTime(reservation.getCheckInDate()));
                    stay.setCheckOutTime(checkOutTime);
                    stay.setActualTotalPrice(0.0);
                    stayRepository.save(stay, conn);
                } else {
                    // Update existing stay with actual check-out time
                    stay.setCheckOutTime(checkOutTime);
                    stayRepository.update(stay, conn);
                }

                // Step 6: Calculate accommodation cost
                BigDecimal accommodationCost = calculateAccommodationCost(
                    reservation.getCheckInDate(),
                    checkOutTime,
                    roomType.getPricePerNight()
                );

                // Step 7: Calculate additional services cost
                List<ServiceUsage> serviceUsages = serviceUsageRepository.findByStayId(stay.getId(), conn);
                BigDecimal additionalServicesCost = serviceUsages.stream()
                    .map(ServiceUsage::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

                // Step 8: Calculate subtotal
                BigDecimal subtotal = accommodationCost.add(additionalServicesCost);

                // Step 9: Check for and apply active discount
                Optional<Discount> activeDiscount = discountRepository.findActiveDiscountByDate(
                    java.sql.Date.valueOf(LocalDate.now()),
                    conn
                );

                BigDecimal discountAmount = BigDecimal.ZERO;
                BigDecimal finalAmount = subtotal;
                Long discountId = null;

                if (activeDiscount.isPresent()) {
                    Discount discount = activeDiscount.get();
                    discountId = discount.getId();
                    
                    // Calculate discount amount
                    discountAmount = subtotal
                        .multiply(BigDecimal.valueOf(discount.getPercentage()))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

                    // Apply discount
                    finalAmount = subtotal.subtract(discountAmount);
                }

                // Ensure final amount is never negative
                if (finalAmount.compareTo(BigDecimal.ZERO) < 0) {
                    finalAmount = BigDecimal.ZERO;
                }

                // Step 10: Check if invoice already exists
                Invoice existingInvoice = invoiceRepository.findByStayId(stay.getId(), conn);
                if (existingInvoice != null) {
                    throw new InvoiceAlreadyExistsException(
                        "An invoice already exists for reservation id: " + reservation.getId()
                    );
                }

                // Step 11: Create invoice
                Invoice invoice = new Invoice();
                invoice.setIssueDate(LocalDate.now());
                invoice.setTotalAmount(subtotal);
                invoice.setStatus("UNPAID");
                invoice.setStayId(stay.getId());
                invoice.setDiscountId(discountId);
                invoice.setDiscountAmount(discountAmount);
                invoice.setFinalAmount(finalAmount);

                invoiceRepository.save(invoice, conn);

                // Step 12: Update room status to AVAILABLE
                room.setStatus(RoomStatus.AVAILABLE);
                roomRepository.update(room, conn);

                // Step 13: Update reservation status to COMPLETED
                reservation.setStatus(ReservationStatus.COMPLETED);
                reservationRepository.update(reservation, conn);

                // Commit transaction
                conn.commit();

                // Step 14: Build response
                return buildCheckOutResponse(
                    reservation,
                    room,
                    roomType,
                    invoice,
                    accommodationCost,
                    additionalServicesCost,
                    discountAmount,
                    finalAmount,
                    activeDiscount.orElse(null),
                    checkOutTime
                );

            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Calculate accommodation cost based on check-in/check-out dates and price per night
     */
    private BigDecimal calculateAccommodationCost(
            java.util.Date checkInDate,
            LocalDateTime checkOutDateTime,
            Double pricePerNight) {
        
        // Convert checkInDate to LocalDateTime (use start of day)
        LocalDateTime checkInDateTime = checkInDate.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();

        // Calculate number of nights
        int nights = calculateNumberOfNights(checkInDateTime, checkOutDateTime);
        if (nights < 1) {
            nights = 1; // Minimum 1 night
        }

        return BigDecimal.valueOf(nights)
            .multiply(BigDecimal.valueOf(pricePerNight))
            .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Calculate number of nights between check-in and check-out
     */
    private int calculateNumberOfNights(LocalDateTime checkIn, LocalDateTime checkOut) {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(
            checkIn.toLocalDate(),
            checkOut.toLocalDate()
        );
    }

    /**
     * Convert java.util.Date to LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(java.util.Date date) {
        return date.toInstant()
            .atZone(ZoneId.systemDefault())
            .toLocalDateTime();
    }

    /**
     * Build the check-out response DTO with all details
     */
    private CheckOutResponseDTO buildCheckOutResponse(
            Reservation reservation,
            Room room,
            RoomType roomType,
            Invoice invoice,
            BigDecimal accommodationCost,
            BigDecimal additionalServicesCost,
            BigDecimal discountAmount,
            BigDecimal finalAmount,
            Discount discount,
            LocalDateTime checkOutTime) {

        LocalDateTime checkInTime = convertToLocalDateTime(reservation.getCheckInDate());
        int numberOfNights = calculateNumberOfNights(checkInTime, checkOutTime);
        if (numberOfNights < 1) {
            numberOfNights = 1;
        }

        return CheckOutResponseDTO.builder()
            .reservationId(reservation.getId())
            .guestId(reservation.getGuestId())
            .roomId(room.getId())
            .roomNumber(room.getRoomNumber())
            .checkInTime(checkInTime)
            .checkOutTime(checkOutTime)
            .numberOfNights(numberOfNights)
            .roomTypeName(roomType.getName())
            .pricePerNight(BigDecimal.valueOf(roomType.getPricePerNight()))
            .invoiceId(invoice.getId())
            .accommodationCost(accommodationCost)
            .additionalServicesCost(additionalServicesCost)
            .subtotal(accommodationCost.add(additionalServicesCost))
            .discountId(discount != null ? discount.getId() : null)
            .discountName(discount != null ? discount.getName() : null)
            .discountPercentage(discount != null ? BigDecimal.valueOf(discount.getPercentage()) : null)
            .discountAmount(discountAmount)
            .finalAmount(finalAmount)
            .invoiceStatus(invoice.getStatus())
            .roomStatus(RoomStatus.AVAILABLE.name())
            .reservationStatus(ReservationStatus.COMPLETED.name())
            .build();
    }
}

