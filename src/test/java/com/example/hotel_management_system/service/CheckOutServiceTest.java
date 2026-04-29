package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.CheckOutRequestDTO;
import com.example.hotel_management_system.dto.CheckOutResponseDTO;
import com.example.hotel_management_system.enums.ReservationStatus;
import com.example.hotel_management_system.enums.RoomStatus;
import com.example.hotel_management_system.model.Discount;
import com.example.hotel_management_system.model.Invoice;
import com.example.hotel_management_system.model.Reservation;
import com.example.hotel_management_system.model.Room;
import com.example.hotel_management_system.model.RoomType;
import com.example.hotel_management_system.model.ServiceUsage;
import com.example.hotel_management_system.model.Stay;
import com.example.hotel_management_system.repository.DiscountRepository;
import com.example.hotel_management_system.repository.InvoiceRepository;
import com.example.hotel_management_system.repository.ReservationRepository;
import com.example.hotel_management_system.repository.RoomRepository;
import com.example.hotel_management_system.repository.RoomTypeRepository;
import com.example.hotel_management_system.repository.ServiceUsageRepository;
import com.example.hotel_management_system.repository.StayRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckOutServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private RoomTypeRepository roomTypeRepository;

    @Mock
    private StayRepository stayRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private ServiceUsageRepository serviceUsageRepository;

    @Mock
    private DiscountRepository discountRepository;

    @Mock
    private Connection connection;

    @InjectMocks
    private CheckOutService checkOutService;

    @Test
    void processCheckOutCreatesStayInvoiceAndAppliesDiscount() throws Exception {
        Reservation reservation = new Reservation(1L,
                asDate(LocalDateTime.of(2026, 4, 18, 12, 0)),
                asDate(LocalDateTime.of(2026, 4, 20, 15, 0)),
                asDate(LocalDateTime.of(2026, 4, 22, 11, 0)),
                2, ReservationStatus.CONFIRMED, 0.0, 30L, 10L, 5L);
        Room room = new Room(10L, "210", 2, RoomStatus.OCCUPIED, 1L, 4L);
        RoomType roomType = new RoomType(4L, "Deluxe", "Sea view", 2, 120.0);
        Discount discount = new Discount(7L, "Spring Sale", 10.0,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), "Seasonal discount");
        CheckOutRequestDTO request = new CheckOutRequestDTO(1L,
                LocalDateTime.of(2026, 4, 22, 10, 0), 99L);
        ServiceUsage minibar = new ServiceUsage(1L, 1L, 1L, 1,
                LocalDate.of(2026, 4, 21), new BigDecimal("20.00"));
        ServiceUsage parking = new ServiceUsage(2L, 1L, 2L, 2,
                LocalDate.of(2026, 4, 22), new BigDecimal("30.00"));

        when(reservationRepository.findById(1L, connection)).thenReturn(Optional.of(reservation));
        when(roomRepository.findById(10L, connection)).thenReturn(Optional.of(room));
        when(roomTypeRepository.findById(4L, connection)).thenReturn(Optional.of(roomType));
        when(stayRepository.findById(1L, connection)).thenReturn(Optional.empty());
        when(serviceUsageRepository.findByStayId(1L, connection)).thenReturn(List.of(minibar, parking));
        when(discountRepository.findActiveDiscountByDate(any(java.sql.Date.class), eq(connection)))
                .thenReturn(Optional.of(discount));
        when(invoiceRepository.findByStayId(1L, connection)).thenReturn(null);
        doAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(80L);
            return null;
        }).when(invoiceRepository).save(any(Invoice.class), eq(connection));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            CheckOutResponseDTO response = checkOutService.processCheckOut(request);

            assertEquals(1L, response.getReservationId());
            assertEquals("210", response.getRoomNumber());
            assertEquals(2, response.getNumberOfNights());
            assertBigDecimalEquals("240.00", response.getAccommodationCost());
            assertBigDecimalEquals("50.00", response.getAdditionalServicesCost());
            assertBigDecimalEquals("290.00", response.getSubtotal());
            assertBigDecimalEquals("29.00", response.getDiscountAmount());
            assertBigDecimalEquals("261.00", response.getFinalAmount());
            assertEquals(80L, response.getInvoiceId());
            assertEquals(RoomStatus.AVAILABLE.name(), response.getRoomStatus());
            assertEquals(ReservationStatus.COMPLETED.name(), response.getReservationStatus());
        }

        ArgumentCaptor<Stay> stayCaptor = ArgumentCaptor.forClass(Stay.class);
        ArgumentCaptor<Room> roomCaptor = ArgumentCaptor.forClass(Room.class);
        ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);

        verify(stayRepository).save(stayCaptor.capture(), eq(connection));
        verify(roomRepository).update(roomCaptor.capture(), eq(connection));
        verify(reservationRepository).update(reservationCaptor.capture(), eq(connection));
        verify(connection).commit();

        assertEquals(1L, stayCaptor.getValue().getReservationId());
        assertEquals(LocalDateTime.of(2026, 4, 22, 10, 0), stayCaptor.getValue().getCheckOutTime());
        assertEquals(RoomStatus.AVAILABLE, roomCaptor.getValue().getStatus());
        assertEquals(ReservationStatus.COMPLETED, reservationCaptor.getValue().getStatus());
    }

    @Test
    void processCheckOutUsesMinimumOneNightForSameDayDeparture() throws Exception {
        Reservation reservation = new Reservation(2L,
                asDate(LocalDateTime.of(2026, 4, 18, 12, 0)),
                asDate(LocalDateTime.of(2026, 4, 20, 9, 0)),
                asDate(LocalDateTime.of(2026, 4, 21, 11, 0)),
                1, ReservationStatus.CONFIRMED, 0.0, 31L, 11L, 5L);
        Room room = new Room(11L, "305", 3, RoomStatus.OCCUPIED, 1L, 5L);
        RoomType roomType = new RoomType(5L, "Standard", "Single room", 1, 150.0);
        Stay existingStay = new Stay(2L,
                LocalDateTime.of(2026, 4, 20, 9, 0),
                null,
                2L,
                0.0);
        CheckOutRequestDTO request = new CheckOutRequestDTO(2L,
                LocalDateTime.of(2026, 4, 20, 20, 0), 99L);

        when(reservationRepository.findById(2L, connection)).thenReturn(Optional.of(reservation));
        when(roomRepository.findById(11L, connection)).thenReturn(Optional.of(room));
        when(roomTypeRepository.findById(5L, connection)).thenReturn(Optional.of(roomType));
        when(stayRepository.findById(2L, connection)).thenReturn(Optional.of(existingStay));
        when(serviceUsageRepository.findByStayId(2L, connection)).thenReturn(List.of());
        when(discountRepository.findActiveDiscountByDate(any(java.sql.Date.class), eq(connection)))
                .thenReturn(Optional.empty());
        when(invoiceRepository.findByStayId(2L, connection)).thenReturn(null);
        doAnswer(invocation -> {
            Invoice invoice = invocation.getArgument(0);
            invoice.setId(81L);
            return null;
        }).when(invoiceRepository).save(any(Invoice.class), eq(connection));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            CheckOutResponseDTO response = checkOutService.processCheckOut(request);

            assertEquals(1, response.getNumberOfNights());
            assertBigDecimalEquals("150.00", response.getAccommodationCost());
            assertBigDecimalEquals("0.00", response.getAdditionalServicesCost());
            assertBigDecimalEquals("0.00", response.getDiscountAmount());
            assertBigDecimalEquals("150.00", response.getFinalAmount());
        }

        verify(stayRepository).update(existingStay, connection);
        verify(connection).commit();
    }

    private static Date asDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, actual.compareTo(new BigDecimal(expected)));
    }
}
