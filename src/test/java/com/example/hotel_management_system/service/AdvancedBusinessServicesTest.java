package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.CheckInDTO;
import com.example.hotel_management_system.dto.InvoiceDTO;
import com.example.hotel_management_system.dto.PaymentDTO;
import com.example.hotel_management_system.dto.ServiceUsageDTO;
import com.example.hotel_management_system.dto.StayDTO;
import com.example.hotel_management_system.enums.ReservationStatus;
import com.example.hotel_management_system.enums.RoomStatus;
import com.example.hotel_management_system.model.Discount;
import com.example.hotel_management_system.model.ExtraService;
import com.example.hotel_management_system.model.Invoice;
import com.example.hotel_management_system.model.Payment;
import com.example.hotel_management_system.model.Reservation;
import com.example.hotel_management_system.model.Room;
import com.example.hotel_management_system.model.ServiceUsage;
import com.example.hotel_management_system.model.Stay;
import com.example.hotel_management_system.repository.DiscountRepository;
import com.example.hotel_management_system.repository.ExtraServiceRepository;
import com.example.hotel_management_system.repository.InvoiceRepository;
import com.example.hotel_management_system.repository.PaymentRepository;
import com.example.hotel_management_system.repository.ReservationRepository;
import com.example.hotel_management_system.repository.RoomRepository;
import com.example.hotel_management_system.repository.ServiceUsageRepository;
import com.example.hotel_management_system.repository.StayRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdvancedBusinessServicesTest {

    @Test
    void paymentServiceRejectsInvalidMethod() {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        PaymentService service = new PaymentService(paymentRepository, invoiceRepository);
        PaymentDTO dto = new PaymentDTO(1L, LocalDateTime.now(), 50.0, "Cheque", 7L);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> service.createPayment(dto));
        assertTrue(exception.getMessage().contains("Allowed methods"));
    }

    @Test
    void paymentServiceUpdatesInvoiceStatusForPartialAndFullPayments() throws Exception {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        Connection connection = mock(Connection.class);
        PaymentService service = new PaymentService(paymentRepository, invoiceRepository);
        PaymentDTO partialPayment = new PaymentDTO(1L, LocalDateTime.of(2026, 4, 24, 10, 0),
                40.0, "Cash", 7L);
        PaymentDTO fullPayment = new PaymentDTO(2L, LocalDateTime.of(2026, 4, 24, 12, 0),
                60.0, "Cash", 7L);
        Invoice invoice = new Invoice(7L, LocalDate.of(2026, 4, 24),
                new BigDecimal("100.00"), "Unpaid", 5L, null, null, null);

        when(invoiceRepository.findById(7L, connection)).thenReturn(invoice);
        when(paymentRepository.getTotalPaidForInvoice(7L, connection)).thenReturn(40.0, 100.0);

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            assertPaymentDto(partialPayment, service.createPayment(partialPayment));
            verify(invoiceRepository).update(eq(7L), any(Invoice.class), eq(connection));
            assertEquals("Partially paid", invoice.getStatus());

            assertPaymentDto(fullPayment, service.createPayment(fullPayment));
            assertEquals("Paid", invoice.getStatus());
        }
    }

    @Test
    void paymentServiceCoversReadUpdateAndDelete() throws Exception {
        PaymentRepository paymentRepository = mock(PaymentRepository.class);
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        Connection connection = mock(Connection.class);
        PaymentService service = new PaymentService(paymentRepository, invoiceRepository);
        Payment payment = new Payment(4L, LocalDateTime.of(2026, 4, 24, 9, 0), 80.0, "Cash", 8L);
        PaymentDTO dto = new PaymentDTO(4L, LocalDateTime.of(2026, 4, 24, 9, 0), 80.0, "Cash", 8L);

        when(paymentRepository.findById(4L, connection)).thenReturn(Optional.of(payment));
        when(paymentRepository.findById(5L, connection)).thenReturn(Optional.empty());
        when(paymentRepository.findById(6L, connection)).thenReturn(Optional.of(payment));
        when(paymentRepository.findAll(connection)).thenReturn(List.of(payment));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            assertPaymentDto(dto, service.getPaymentById(4L));
            assertEquals(1, service.getAllPayments().size());
            assertNull(service.updatePayment(5L, dto));
            assertTrue(service.deletePayment(6L));
        }

        verify(paymentRepository).delete(6L, connection);
    }

    @Test
    void invoiceServiceAppliesActiveDiscountWhenSaving() throws Exception {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        DiscountRepository discountRepository = mock(DiscountRepository.class);
        Connection connection = mock(Connection.class);
        InvoiceService service = new InvoiceService(invoiceRepository, discountRepository);
        InvoiceDTO dto = new InvoiceDTO(1L, LocalDate.of(2026, 4, 24),
                new BigDecimal("200.00"), "Unpaid", 5L, null, null, null);
        Discount discount = new Discount(3L, "VIP", 25.0,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), "VIP");

        when(discountRepository.findActiveDiscountByDate(java.sql.Date.valueOf(LocalDate.of(2026, 4, 24)), connection))
                .thenReturn(Optional.of(discount));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            service.save(dto);
        }

        ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
        verify(invoiceRepository).save(captor.capture(), eq(connection));
        assertEquals(3L, captor.getValue().getDiscountId());
        assertBigDecimalEquals("50.00", captor.getValue().getDiscountAmount());
        assertBigDecimalEquals("150.00", captor.getValue().getFinalAmount());
    }

    @Test
    void invoiceServiceHandlesNoDiscountAndCoversReadUpdateDeleteAndManualDiscount() throws Exception {
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        DiscountRepository discountRepository = mock(DiscountRepository.class);
        Connection connection = mock(Connection.class);
        InvoiceService service = new InvoiceService(invoiceRepository, discountRepository);
        InvoiceDTO dto = new InvoiceDTO(1L, LocalDate.of(2026, 4, 24),
                new BigDecimal("120.00"), "Paid", 5L, null, null, null);
        Invoice invoice = new Invoice(1L, LocalDate.of(2026, 4, 24),
                new BigDecimal("120.00"), "Paid", 5L, null, BigDecimal.ZERO, new BigDecimal("120.00"));
        Discount discount = new Discount(4L, "Promo", 10.0,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), "Promo");

        when(discountRepository.findActiveDiscountByDate(java.sql.Date.valueOf(LocalDate.of(2026, 4, 24)), connection))
                .thenReturn(Optional.empty());
        when(invoiceRepository.findAll(connection)).thenReturn(List.of(invoice));
        when(invoiceRepository.findById(1L, connection)).thenReturn(invoice);
        when(discountRepository.findById(4L, connection)).thenReturn(Optional.of(discount));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            service.save(dto);
            assertEquals(1, service.findAll().size());
            assertEquals("Paid", service.findById(1L).getStatus());
            service.update(1L, dto);
            service.applyDiscountManually(1L, 4L);
            service.delete(1L);
        }

        verify(invoiceRepository, times(2)).update(eq(1L), any(Invoice.class), eq(connection));
        verify(invoiceRepository).delete(1L, connection);
    }

    @Test
    void invoiceServiceRejectsInvalidStatuses() {
        InvoiceService service = new InvoiceService(mock(InvoiceRepository.class), mock(DiscountRepository.class));
        InvoiceDTO dto = new InvoiceDTO(1L, LocalDate.now(), new BigDecimal("50.00"), "CANCELLED", 1L);

        RuntimeException saveException = assertThrows(RuntimeException.class, () -> service.save(dto));
        assertTrue(saveException.getMessage().contains("Invalid status"));
        RuntimeException updateException = assertThrows(RuntimeException.class, () -> service.update(1L, dto));
        assertTrue(updateException.getMessage().contains("Invalid status"));
    }

    @Test
    void serviceUsageServiceCalculatesTotalAndUpdatesStay() throws Exception {
        ServiceUsageRepository repository = mock(ServiceUsageRepository.class);
        ExtraServiceRepository extraServiceRepository = mock(ExtraServiceRepository.class);
        StayRepository stayRepository = mock(StayRepository.class);
        Connection connection = mock(Connection.class);
        ServiceUsageService service = new ServiceUsageService(repository, extraServiceRepository, stayRepository);
        ServiceUsageDTO dto = new ServiceUsageDTO(1L, 7L, 3L, 3,
                LocalDate.of(2026, 4, 24), BigDecimal.ZERO);
        ExtraService extraService = new ExtraService(3L, "Laundry", "Laundry service", 12.5, "Y");
        Stay stay = new Stay(7L, LocalDateTime.of(2026, 4, 20, 14, 0), null, 5L, 40.0);

        when(extraServiceRepository.findById(3L, connection)).thenReturn(Optional.of(extraService));
        when(stayRepository.findById(7L, connection)).thenReturn(Optional.of(stay));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            service.save(dto);
        }

        ArgumentCaptor<ServiceUsage> usageCaptor = ArgumentCaptor.forClass(ServiceUsage.class);
        verify(repository).save(usageCaptor.capture(), eq(connection));
        assertBigDecimalEquals("37.5", usageCaptor.getValue().getTotalPrice());
        assertEquals(77.5, stay.getActualTotalPrice(), 0.001);
        verify(stayRepository).update(stay, connection);
    }

    @Test
    void serviceUsageServiceHandlesMissingServiceAndCoversRemainingCrudMethods() throws Exception {
        ServiceUsageRepository repository = mock(ServiceUsageRepository.class);
        ExtraServiceRepository extraServiceRepository = mock(ExtraServiceRepository.class);
        StayRepository stayRepository = mock(StayRepository.class);
        Connection connection = mock(Connection.class);
        ServiceUsageService service = new ServiceUsageService(repository, extraServiceRepository, stayRepository);
        ServiceUsageDTO dto = new ServiceUsageDTO(1L, 7L, 3L, 3,
                LocalDate.of(2026, 4, 24), new BigDecimal("33.00"));
        ServiceUsage entity = new ServiceUsage(1L, 7L, 3L, 3,
                LocalDate.of(2026, 4, 24), new BigDecimal("33.00"));

        when(extraServiceRepository.findById(3L, connection)).thenReturn(Optional.empty());
        when(repository.findById(1L, connection)).thenReturn(entity);
        when(repository.findAll(connection)).thenReturn(List.of(entity));
        doNothing().when(repository).update(1L, entity, connection);
        doNothing().when(repository).delete(1L, connection);

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            RuntimeException exception = assertThrows(RuntimeException.class, () -> service.save(dto));
            assertTrue(exception.getMessage().contains("Service not found"));
            assertEquals(7L, service.findById(1L).getStayId());
            assertEquals(1, service.findAll().size());
            service.update(1L, dto);
            service.delete(1L);
        }

        verify(repository).update(eq(1L), any(ServiceUsage.class), eq(connection));
        verify(repository).delete(1L, connection);
    }

    @Test
    void stayServiceCheckInCreatesStayAndUpdatesStatuses() throws Exception {
        StayRepository stayRepository = mock(StayRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        Connection connection = mock(Connection.class);
        StayService service = new StayService(stayRepository, reservationRepository, roomRepository);
        Reservation reservation = new Reservation(5L, asDate(LocalDateTime.of(2026, 4, 1, 10, 0)),
                asDate(LocalDateTime.of(2026, 4, 24, 14, 0)),
                asDate(LocalDateTime.of(2026, 4, 27, 11, 0)),
                2, ReservationStatus.PENDING, 300.0, 30L, 9L, 1L);
        Room room = new Room(9L, "301", 3, RoomStatus.AVAILABLE, 1L, 2L);

        when(reservationRepository.findById(5L, connection)).thenReturn(Optional.of(reservation));
        when(stayRepository.findByReservationId(5L, connection)).thenReturn(Optional.empty());
        when(roomRepository.findById(9L, connection)).thenReturn(Optional.of(room));
        when(stayRepository.getNextId(connection)).thenReturn(44L);

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            service.checkIn(new CheckInDTO(5L));
        }

        ArgumentCaptor<Stay> stayCaptor = ArgumentCaptor.forClass(Stay.class);
        verify(connection).setAutoCommit(false);
        verify(stayRepository).save(stayCaptor.capture(), eq(connection));
        verify(reservationRepository).updateStatus(5L, ReservationStatus.CONFIRMED, connection);
        verify(roomRepository).updateStatus(9L, RoomStatus.OCCUPIED, connection);
        verify(connection).commit();
        verify(connection).close();
        assertEquals(44L, stayCaptor.getValue().getId());
        assertNotNull(stayCaptor.getValue().getCheckInTime());
    }

    @Test
    void stayServiceCheckInRollsBackWhenReservationCannotBeCheckedIn() throws Exception {
        StayRepository stayRepository = mock(StayRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        Connection connection = mock(Connection.class);
        StayService service = new StayService(stayRepository, reservationRepository, roomRepository);
        Reservation reservation = new Reservation(5L, asDate(LocalDateTime.of(2026, 4, 1, 10, 0)),
                asDate(LocalDateTime.of(2026, 4, 24, 14, 0)),
                asDate(LocalDateTime.of(2026, 4, 27, 11, 0)),
                2, ReservationStatus.CANCELLED, 300.0, 30L, 9L, 1L);

        when(reservationRepository.findById(5L, connection)).thenReturn(Optional.of(reservation));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            SQLException exception = assertThrows(SQLException.class, () -> service.checkIn(new CheckInDTO(5L)));
            assertTrue(exception.getMessage().contains("Cancelled reservation"));
        }

        verify(connection).rollback();
        verify(connection).close();
    }

    @Test
    void stayServiceCoversCrudMethods() throws Exception {
        StayRepository stayRepository = mock(StayRepository.class);
        ReservationRepository reservationRepository = mock(ReservationRepository.class);
        RoomRepository roomRepository = mock(RoomRepository.class);
        Connection connection = mock(Connection.class);
        StayService service = new StayService(stayRepository, reservationRepository, roomRepository);
        StayDTO dto = new StayDTO(5L, LocalDateTime.of(2026, 4, 24, 14, 0),
                LocalDateTime.of(2026, 4, 25, 11, 0), 8L, 99.0);
        Stay entity = new Stay(5L, LocalDateTime.of(2026, 4, 24, 14, 0),
                LocalDateTime.of(2026, 4, 25, 11, 0), 8L, 99.0);

        when(stayRepository.findById(5L, connection)).thenReturn(Optional.of(entity));
        when(stayRepository.findAll(connection)).thenReturn(List.of(entity));
        when(stayRepository.findById(6L, connection)).thenReturn(Optional.empty());
        when(stayRepository.findById(7L, connection)).thenReturn(Optional.of(entity));
        doNothing().when(stayRepository).delete(7L, connection);

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            assertStayDto(dto, service.createStay(dto));
            assertStayDto(dto, service.getStayById(5L));
            assertEquals(1, service.getAllStays().size());
            assertNull(service.updateStay(6L, dto));
            assertTrue(service.deleteStay(7L));
        }

        verify(stayRepository).save(any(Stay.class), eq(connection));
        verify(stayRepository).delete(7L, connection);
    }

    private static Date asDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static void assertBigDecimalEquals(String expected, BigDecimal actual) {
        assertEquals(0, actual.compareTo(new BigDecimal(expected)));
    }

    private static void assertPaymentDto(PaymentDTO expected, PaymentDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getPaymentDate(), actual.getPaymentDate());
        assertEquals(expected.getAmount(), actual.getAmount(), 0.001);
        assertEquals(expected.getPaymentMethod(), actual.getPaymentMethod());
        assertEquals(expected.getInvoiceId(), actual.getInvoiceId());
    }

    private static void assertStayDto(StayDTO expected, StayDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getCheckInTime(), actual.getCheckInTime());
        assertEquals(expected.getCheckOutTime(), actual.getCheckOutTime());
        assertEquals(expected.getReservationId(), actual.getReservationId());
        assertEquals(expected.getActualTotalPrice(), actual.getActualTotalPrice(), 0.001);
    }
}
