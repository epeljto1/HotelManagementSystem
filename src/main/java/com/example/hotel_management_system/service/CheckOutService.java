package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.CheckOutRequestDTO;
import com.example.hotel_management_system.dto.CheckOutResponseDTO;
import com.example.hotel_management_system.dto.ServiceUsageDTO;
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

@Service
public class CheckOutService {

    private final ReservationRepository reservationRepository;
    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final StayRepository stayRepository;
    private final InvoiceRepository invoiceRepository;
    private final ServiceUsageRepository serviceUsageRepository;
    private final DiscountRepository discountRepository;
    private final UserRepository userRepository;
    private final PdfInvoiceService pdfInvoiceService;

    public CheckOutService(
            ReservationRepository reservationRepository,
            RoomRepository roomRepository,
            RoomTypeRepository roomTypeRepository,
            StayRepository stayRepository,
            InvoiceRepository invoiceRepository,
            ServiceUsageRepository serviceUsageRepository,
            DiscountRepository discountRepository,
            UserRepository userRepository,
            PdfInvoiceService pdfInvoiceService) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.stayRepository = stayRepository;
        this.invoiceRepository = invoiceRepository;
        this.serviceUsageRepository = serviceUsageRepository;
        this.discountRepository = discountRepository;
        this.userRepository = userRepository;
        this.pdfInvoiceService = pdfInvoiceService;
    }

    public CheckOutResponseDTO processCheckOut(CheckOutRequestDTO request) throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Dohvatanje rezervacije
                Reservation reservation = reservationRepository.findById(request.getReservationId(), conn)
                        .orElseThrow(() -> new ReservationNotFoundException(request.getReservationId()));

                if (reservation.getStatus() != ReservationStatus.CONFIRMED) {
                    throw new InvalidReservationStatusException("Samo potvrđene rezervacije se mogu odjaviti.");
                }

                Room room = roomRepository.findById(reservation.getRoomId(), conn)
                        .orElseThrow(() -> new RoomNotFoundException(reservation.getRoomId()));

                RoomType roomType = roomTypeRepository.findById(room.getRoomTypeId(), conn)
                        .orElseThrow(() -> new RuntimeException("Tip sobe nije pronadjen."));

                LocalDateTime checkOutTime = request.getActualCheckOutTime() != null ? request.getActualCheckOutTime() : LocalDateTime.now();

                // 2. Stay
                Stay stay = stayRepository.findById(reservation.getId(), conn).orElse(null);
                if (stay == null) {
                    stay = new Stay();
                    stay.setId(reservation.getId());
                    stay.setReservationId(reservation.getId());
                    stay.setCheckInTime(convertToLocalDateTime(reservation.getCheckInDate()));
                    stay.setCheckOutTime(checkOutTime);
                    stay.setActualTotalPrice(0.0);
                    stayRepository.save(stay, conn);
                }

                // 3. Kalkulacija troskova
                BigDecimal accommodationCost = calculateAccommodationCost(reservation.getCheckInDate(), checkOutTime, roomType.getPricePerNight());

                // Detaljna obrada usluga
                List<ServiceUsage> usages = serviceUsageRepository.findByStayId(stay.getId(), conn);
                BigDecimal additionalServicesCost = BigDecimal.ZERO;
                List<ServiceUsageDTO> serviceDetails = new java.util.ArrayList<>(); // Lista za DTO

                if (usages != null) {
                    for (ServiceUsage u : usages) {
                        ServiceUsageDTO dto = new ServiceUsageDTO();
                        dto.setId(u.getId());
                        dto.setServiceId(u.getServiceId());
                        dto.setQuantity(u.getQuantity());
                        dto.setTotalPrice(u.getTotalPrice());
                        dto.setUsageDate(u.getUsageDate() != null ? u.getUsageDate() : LocalDate.now());

                        String serviceName = serviceUsageRepository.findServiceNameById(u.getServiceId(), conn);
                        dto.setServiceName(serviceName);

                        serviceDetails.add(dto);

                        BigDecimal price = u.getTotalPrice() != null ? u.getTotalPrice() : BigDecimal.ZERO;
                        additionalServicesCost = additionalServicesCost.add(price);
                    }
                }

                BigDecimal subtotal = accommodationCost.add(additionalServicesCost);

                // Popust
                Optional<Discount> activeDiscount = discountRepository.findActiveDiscountByDate(java.sql.Date.valueOf(LocalDate.now()), conn);
                BigDecimal discountAmount = BigDecimal.ZERO;
                Long dId = null;
                if (activeDiscount.isPresent()) {
                    dId = activeDiscount.get().getId();
                    discountAmount = subtotal.multiply(BigDecimal.valueOf(activeDiscount.get().getPercentage()))
                            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                }
                BigDecimal finalAmount = subtotal.subtract(discountAmount);

                // 4. Kreiranje Invoice-a
                if (invoiceRepository.findByStayId(stay.getId(), conn) != null) {
                    throw new InvoiceAlreadyExistsException("Racun vec postoji.");
                }

                Invoice invoice = new Invoice();
                invoice.setIssueDate(LocalDate.now());
                invoice.setTotalAmount(subtotal);
                invoice.setStatus("PAID");
                invoice.setStayId(stay.getId());
                invoice.setDiscountId(dId);
                invoice.setDiscountAmount(discountAmount);
                invoice.setFinalAmount(finalAmount);

                CheckOutResponseDTO responseDTO = buildCheckOutResponse(reservation, room, roomType, invoice, accommodationCost, additionalServicesCost, discountAmount, finalAmount, activeDiscount.orElse(null), checkOutTime);

                responseDTO.setServiceDetails(serviceDetails);

                String guestName = userRepository.findById(reservation.getGuestId(), conn).map(User::getUsername).orElse("Gost #" + reservation.getGuestId());
                responseDTO.setGuestFullName(guestName);
                responseDTO.setInvoiceId(reservation.getId());

                // Generisanje PDF-a
                byte[] pdfBytes = pdfInvoiceService.generateInvoicePdfBytes(responseDTO);
                invoice.setInvoicePdf(pdfBytes);

                // Spasavanje u bazu
                invoiceRepository.save(invoice, conn);

                // 5. Update statusa
                room.setStatus(RoomStatus.AVAILABLE);
                roomRepository.update(room, conn);
                reservation.setStatus(ReservationStatus.COMPLETED);
                reservationRepository.update(reservation, conn);

                conn.commit();
                return responseDTO;

            } catch (Exception e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public CheckOutResponseDTO getCheckOutDetailsByReservationId(Long reservationId) throws SQLException {
        try (Connection conn = DbConfig.getConnection()) {
            Reservation res = reservationRepository.findById(reservationId, conn)
                    .orElseThrow(() -> new ReservationNotFoundException(reservationId));

            String guestIdentifier = userRepository.findById(res.getGuestId(), conn)
                    .map(User::getUsername).orElse("Gost #" + res.getGuestId());

            Room room = roomRepository.findById(res.getRoomId(), conn)
                    .orElseThrow(() -> new RoomNotFoundException(res.getRoomId()));

            RoomType type = roomTypeRepository.findById(room.getRoomTypeId(), conn)
                    .orElseThrow(() -> new RuntimeException("Tip sobe nije pronađen"));

            Invoice inv = invoiceRepository.findByStayId(reservationId, conn);
            if (inv == null) throw new RuntimeException("Račun nije pronađen.");

            Stay stay = stayRepository.findById(reservationId, conn).orElse(null);
            Discount discount = (inv.getDiscountId() != null) ?
                    discountRepository.findById(inv.getDiscountId(), conn).orElse(null) : null;

            CheckOutResponseDTO response = buildCheckOutResponse(
                    res, room, type, inv, inv.getTotalAmount(), BigDecimal.ZERO,
                    inv.getDiscountAmount(), inv.getFinalAmount(), discount,
                    stay != null ? stay.getCheckOutTime() : LocalDateTime.now()
            );

            response.setGuestFullName(guestIdentifier);
            return response;
        }
    }

    private BigDecimal calculateAccommodationCost(java.util.Date checkInDate, LocalDateTime checkOutDateTime, Double pricePerNight) {
        LocalDateTime checkInDateTime = convertToLocalDateTime(checkInDate);
        int nights = calculateNumberOfNights(checkInDateTime, checkOutDateTime);
        if (nights < 1) nights = 1;
        return BigDecimal.valueOf(nights).multiply(BigDecimal.valueOf(pricePerNight)).setScale(2, RoundingMode.HALF_UP);
    }

    private int calculateNumberOfNights(LocalDateTime checkIn, LocalDateTime checkOut) {
        return (int) java.time.temporal.ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
    }

    private LocalDateTime convertToLocalDateTime(java.util.Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    private CheckOutResponseDTO buildCheckOutResponse(
            Reservation reservation, Room room, RoomType roomType, Invoice invoice,
            BigDecimal accommodationCost, BigDecimal additionalServicesCost,
            BigDecimal discountAmount, BigDecimal finalAmount, Discount discount, LocalDateTime checkOutTime) {

        LocalDateTime checkInTime = convertToLocalDateTime(reservation.getCheckInDate());
        int numberOfNights = calculateNumberOfNights(checkInTime, checkOutTime);
        if (numberOfNights < 1) numberOfNights = 1;

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