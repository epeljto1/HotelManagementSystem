package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.dto.CheckInDTO;
import com.example.hotel_management_system.dto.CheckOutRequestDTO;
import com.example.hotel_management_system.dto.CheckOutResponseDTO;
import com.example.hotel_management_system.dto.DiscountApplyDTO;
import com.example.hotel_management_system.dto.InvoiceDTO;
import com.example.hotel_management_system.dto.PaymentDTO;
import com.example.hotel_management_system.dto.ReservationDTO;
import com.example.hotel_management_system.dto.ServiceUsageDTO;
import com.example.hotel_management_system.dto.StayDTO;
import com.example.hotel_management_system.dto.UserRegistrationDTO;
import com.example.hotel_management_system.enums.ReservationStatus;
import com.example.hotel_management_system.exception.InvoiceAlreadyExistsException;
import com.example.hotel_management_system.exception.InvalidReservationStatusException;
import com.example.hotel_management_system.exception.ReservationNotFoundException;
import com.example.hotel_management_system.exception.RoomNotFoundException;
import com.example.hotel_management_system.security.TokenBlacklist;
import com.example.hotel_management_system.service.CheckOutService;
import com.example.hotel_management_system.service.InvoiceService;
import com.example.hotel_management_system.service.PaymentService;
import com.example.hotel_management_system.service.ReservationService;
import com.example.hotel_management_system.service.ServiceUsageService;
import com.example.hotel_management_system.service.StayService;
import com.example.hotel_management_system.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WorkflowControllersTest {

    @Test
    void invoiceControllerCoversCrudAndDiscountApplication() {
        InvoiceService service = mock(InvoiceService.class);
        InvoiceController controller = new InvoiceController(service);
        InvoiceDTO dto = new InvoiceDTO(1L, LocalDate.of(2026, 4, 24),
                new BigDecimal("150.00"), "Paid", 5L);

        when(service.findAll()).thenReturn(List.of(dto));
        when(service.findById(1L)).thenReturn(dto);

        List<InvoiceDTO> invoices = controller.findAll();
        assertEquals(1, invoices.size());
        assertSame(dto, invoices.get(0));
        assertSame(dto, controller.findById(1L));

        controller.save(dto);
        controller.update(1L, dto);
        controller.delete(1L);

        assertEquals(HttpStatus.OK, controller.applyDiscount(new DiscountApplyDTO(1L, 2L)).getStatusCode());

        doThrow(new RuntimeException("bad discount")).when(service).applyDiscountManually(2L, 3L);
        assertEquals(HttpStatus.BAD_REQUEST, controller.applyDiscount(new DiscountApplyDTO(2L, 3L)).getStatusCode());
    }

    @Test
    void paymentControllerCoversSuccessValidationAndFallbackStatuses() throws Exception {
        PaymentService service = mock(PaymentService.class);
        PaymentController controller = new PaymentController(service);
        PaymentDTO dto = new PaymentDTO(1L, LocalDateTime.of(2026, 4, 24, 12, 0), 100.0, "Cash", 5L);

        when(service.createPayment(dto)).thenReturn(dto);
        when(service.getPaymentById(1L)).thenReturn(dto);
        when(service.getPaymentById(2L)).thenReturn(null);
        when(service.getAllPayments()).thenReturn(List.of(dto));
        when(service.updatePayment(1L, dto)).thenReturn(dto);
        when(service.updatePayment(2L, dto)).thenReturn(null);
        when(service.deletePayment(1L)).thenReturn(true);
        when(service.deletePayment(2L)).thenReturn(false);

        assertEquals(HttpStatus.CREATED, controller.createPayment(dto).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getPaymentById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getPaymentById(2L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAllPayments().getStatusCode());
        assertEquals(HttpStatus.OK, controller.updatePayment(1L, dto).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.updatePayment(2L, dto).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.deletePayment(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.deletePayment(2L).getStatusCode());

        when(service.createPayment(null)).thenThrow(new IllegalArgumentException("invalid method"));
        when(service.getAllPayments()).thenThrow(new SQLException("db"));

        assertEquals(HttpStatus.BAD_REQUEST, controller.createPayment(null).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getAllPayments().getStatusCode());
    }

    @Test
    void reservationControllerCoversCrudEndpoints() throws Exception {
        ReservationService reservationService = mock(ReservationService.class);
        CheckOutService checkOutService = mock(CheckOutService.class);
        ReservationController controller = new ReservationController(reservationService, checkOutService);
        ReservationDTO dto = new ReservationDTO();
        dto.setId(1L);
        dto.setStatus(ReservationStatus.PENDING);

        when(reservationService.createReservation(dto)).thenReturn(dto);
        when(reservationService.getAllReservations()).thenReturn(List.of(dto));
        when(reservationService.getReservationById(1L)).thenReturn(dto);
        when(reservationService.getReservationById(2L)).thenReturn(null);
        when(reservationService.updateReservation(1L, dto)).thenReturn(dto);
        when(reservationService.updateReservation(2L, dto)).thenReturn(null);
        when(reservationService.deleteReservation(1L)).thenReturn(true);
        when(reservationService.deleteReservation(2L)).thenReturn(false);

        assertEquals(HttpStatus.CREATED, controller.create(dto).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAll().getStatusCode());
        assertEquals(HttpStatus.OK, controller.getById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getById(2L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.update(1L, dto).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.update(2L, dto).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.delete(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.delete(2L).getStatusCode());

        when(reservationService.getAllReservations()).thenThrow(new SQLException("db"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getAll().getStatusCode());
    }

    @Test
    void reservationControllerMapsCheckoutErrorsToExpectedStatuses() throws Exception {
        ReservationService reservationService = mock(ReservationService.class);
        CheckOutService checkOutService = mock(CheckOutService.class);
        ReservationController controller = new ReservationController(reservationService, checkOutService);
        CheckOutRequestDTO request = new CheckOutRequestDTO();
        CheckOutResponseDTO response = CheckOutResponseDTO.builder().reservationId(5L).build();

        when(checkOutService.processCheckOut(any(CheckOutRequestDTO.class))).thenReturn(response);
        assertEquals(HttpStatus.OK, controller.checkOut(5L, request).getStatusCode());
        assertEquals(5L, request.getReservationId());

        doThrow(new ReservationNotFoundException(5L)).when(checkOutService).processCheckOut(any(CheckOutRequestDTO.class));
        var notFoundResponse = controller.checkOut(5L, request);
        assertEquals(HttpStatus.NOT_FOUND, notFoundResponse.getStatusCode());
        assertEquals("RESERVATION_NOT_FOUND", ((Map<?, ?>) notFoundResponse.getBody()).get("errorCode"));

        doThrow(new InvalidReservationStatusException("Only CONFIRMED reservations can be checked out"))
                .when(checkOutService).processCheckOut(any(CheckOutRequestDTO.class));
        assertEquals(HttpStatus.BAD_REQUEST, controller.checkOut(5L, request).getStatusCode());

        doThrow(new RoomNotFoundException(9L)).when(checkOutService).processCheckOut(any(CheckOutRequestDTO.class));
        assertEquals(HttpStatus.NOT_FOUND, controller.checkOut(5L, request).getStatusCode());

        doThrow(new InvoiceAlreadyExistsException("Invoice already exists", null))
                .when(checkOutService).processCheckOut(any(CheckOutRequestDTO.class));
        assertEquals(HttpStatus.CONFLICT, controller.checkOut(5L, request).getStatusCode());

        doThrow(new SQLException("db")).when(checkOutService).processCheckOut(any(CheckOutRequestDTO.class));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.checkOut(5L, request).getStatusCode());

        doThrow(new RuntimeException("boom")).when(checkOutService).processCheckOut(any(CheckOutRequestDTO.class));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.checkOut(5L, request).getStatusCode());
    }

    @Test
    void serviceUsageControllerDelegatesDirectlyToService() {
        ServiceUsageService service = mock(ServiceUsageService.class);
        ServiceUsageController controller = new ServiceUsageController(service);
        ServiceUsageDTO dto = new ServiceUsageDTO();
        dto.setId(1L);

        when(service.findAll()).thenReturn(List.of(dto));
        when(service.findById(1L)).thenReturn(dto);

        List<ServiceUsageDTO> serviceUsages = controller.findAll();
        assertEquals(1, serviceUsages.size());
        assertSame(dto, serviceUsages.get(0));
        assertSame(dto, controller.findById(1L));

        controller.save(dto);
        controller.update(1L, dto);
        controller.delete(1L);

        verify(service).save(dto);
        verify(service).update(1L, dto);
        verify(service).delete(1L);
    }

    @Test
    void stayControllerCoversSuccessAndErrorBranches() throws Exception {
        StayService service = mock(StayService.class);
        StayController controller = new StayController(service);
        StayDTO dto = new StayDTO(1L, LocalDateTime.of(2026, 4, 24, 14, 0),
                LocalDateTime.of(2026, 4, 25, 11, 0), 5L, 90.0);

        when(service.createStay(dto)).thenReturn(dto);
        when(service.getStayById(1L)).thenReturn(dto);
        when(service.getStayById(2L)).thenReturn(null);
        when(service.getAllStays()).thenReturn(List.of(dto));
        when(service.updateStay(1L, dto)).thenReturn(dto);
        when(service.updateStay(2L, dto)).thenReturn(null);
        when(service.deleteStay(1L)).thenReturn(true);
        when(service.deleteStay(2L)).thenReturn(false);

        assertEquals(HttpStatus.CREATED, controller.createStay(dto).getStatusCode());
        assertEquals(HttpStatus.OK, controller.checkIn(new CheckInDTO(1L)).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getStayById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getStayById(2L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAllStays().getStatusCode());
        assertEquals(HttpStatus.OK, controller.updateStay(1L, dto).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.updateStay(2L, dto).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteStay(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteStay(2L).getStatusCode());

        doThrow(new SQLException("bad request")).when(service).checkIn(new CheckInDTO(2L));
        assertEquals(HttpStatus.BAD_REQUEST, controller.checkIn(new CheckInDTO(2L)).getStatusCode());
    }

    @Test
    void userControllerCoversAuthenticationAndLogoutBranches() throws Exception {
        UserService userService = mock(UserService.class);
        TokenBlacklist tokenBlacklist = mock(TokenBlacklist.class);
        UserController controller = new UserController(userService, tokenBlacklist);
        UserRegistrationDTO registrationDTO = new UserRegistrationDTO();
        registrationDTO.setUsername("amina");
        UserController.LoginRequest loginRequest = new UserController.LoginRequest("amina", "secret");

        when(userService.login("amina", "secret")).thenReturn("jwt-token");
        when(userService.getAllUsers()).thenReturn(List.of());

        assertEquals(HttpStatus.OK, controller.register(registrationDTO).getStatusCode());
        var loginResponse = controller.login(loginRequest);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertEquals("jwt-token", ((UserController.AuthResponse) loginResponse.getBody()).getToken());
        assertEquals(HttpStatus.OK, controller.logout("Bearer jwt-token").getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, controller.logout("invalid").getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAllUsers().getStatusCode());

        doThrow(new RuntimeException("register failed")).when(userService).register(null);
        when(userService.login("bad", "bad")).thenThrow(new RuntimeException("bad credentials"));
        when(userService.getAllUsers()).thenThrow(new RuntimeException("db error"));

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.register(null).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, controller.login(new UserController.LoginRequest("bad", "bad")).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getAllUsers().getStatusCode());

        verify(tokenBlacklist).add("jwt-token");
    }
}
