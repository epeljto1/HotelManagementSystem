package com.example.hotel_management_system.controller;

import com.example.hotel_management_system.dto.DiscountDTO;
import com.example.hotel_management_system.dto.ExtraServiceDTO;
import com.example.hotel_management_system.dto.GuestDTO;
import com.example.hotel_management_system.dto.HotelDTO;
import com.example.hotel_management_system.dto.RoomDTO;
import com.example.hotel_management_system.dto.RoomTypeDTO;
import com.example.hotel_management_system.enums.RoomStatus;
import com.example.hotel_management_system.service.DiscountService;
import com.example.hotel_management_system.service.ExtraServiceService;
import com.example.hotel_management_system.service.GuestService;
import com.example.hotel_management_system.service.HotelService;
import com.example.hotel_management_system.service.RoomService;
import com.example.hotel_management_system.service.RoomTypeService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CrudControllersTest {

    @Test
    void discountControllerCoversSuccessAndFailureBranches() throws Exception {
        DiscountService service = mock(DiscountService.class);
        DiscountController controller = new DiscountController(service);
        DiscountDTO dto = new DiscountDTO(1L, "Spring", 10.0,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), "Promo");

        when(service.createDiscount(dto)).thenReturn(dto);
        when(service.getDiscountById(1L)).thenReturn(dto);
        when(service.getDiscountById(2L)).thenReturn(null);
        when(service.getAllDiscounts()).thenReturn(List.of(dto));
        when(service.updateDiscount(1L, dto)).thenReturn(dto);
        when(service.updateDiscount(2L, dto)).thenReturn(null);
        when(service.deleteDiscount(1L)).thenReturn(true);
        when(service.deleteDiscount(2L)).thenReturn(false);
        when(service.createDiscount(null)).thenThrow(new SQLException("boom"));

        assertEquals(HttpStatus.CREATED, controller.createDiscount(dto).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getDiscountById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getDiscountById(2L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAllDiscounts().getStatusCode());
        assertEquals(HttpStatus.OK, controller.updateDiscount(1L, dto).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.updateDiscount(2L, dto).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteDiscount(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteDiscount(2L).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.createDiscount(null).getStatusCode());
    }

    @Test
    void extraServiceControllerCoversSuccessAndFailureBranches() throws Exception {
        ExtraServiceService service = mock(ExtraServiceService.class);
        ExtraServiceController controller = new ExtraServiceController(service);
        ExtraServiceDTO dto = new ExtraServiceDTO(1L, "Spa", "Massage", 40.0, "Y");

        when(service.createExtraService(dto)).thenReturn(dto);
        when(service.getExtraServiceById(1L)).thenReturn(dto);
        when(service.getExtraServiceById(2L)).thenReturn(null);
        when(service.getAllExtraServices()).thenReturn(List.of(dto)).thenThrow(new SQLException("boom"));
        when(service.updateExtraService(1L, dto)).thenReturn(dto);
        when(service.updateExtraService(2L, dto)).thenReturn(null);
        when(service.deleteExtraService(1L)).thenReturn(true);
        when(service.deleteExtraService(2L)).thenReturn(false);

        assertEquals(HttpStatus.CREATED, controller.createExtraService(dto).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getExtraServiceById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getExtraServiceById(2L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAllExtraServices().getStatusCode());
        assertEquals(HttpStatus.OK, controller.updateExtraService(1L, dto).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.updateExtraService(2L, dto).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteExtraService(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteExtraService(2L).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getAllExtraServices().getStatusCode());
    }

    @Test
    void guestControllerCoversSuccessAndFailureBranches() throws Exception {
        GuestService service = mock(GuestService.class);
        GuestController controller = new GuestController(service);
        GuestDTO dto = new GuestDTO();
        dto.setId(1L);
        dto.setFirstName("Amina");

        when(service.createGuest(dto)).thenReturn(dto);
        when(service.getGuestById(1L)).thenReturn(dto);
        when(service.getGuestById(2L)).thenReturn(null);
        when(service.getAllGuests()).thenReturn(List.of(dto));
        when(service.updateGuest(1L, dto)).thenReturn(dto);
        when(service.updateGuest(2L, dto)).thenReturn(null);
        when(service.deleteGuest(1L)).thenReturn(true);
        when(service.deleteGuest(2L)).thenReturn(false);
        when(service.createGuest(null)).thenThrow(new SQLException("boom"));

        assertEquals(HttpStatus.CREATED, controller.createGuest(dto).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getGuestById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getGuestById(2L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAllGuests().getStatusCode());
        assertEquals(HttpStatus.OK, controller.updateGuest(1L, dto).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.updateGuest(2L, dto).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteGuest(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteGuest(2L).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.createGuest(null).getStatusCode());
    }

    @Test
    void hotelControllerCoversSuccessAndFailureBranches() throws Exception {
        HotelService service = mock(HotelService.class);
        HotelController controller = new HotelController(service);
        HotelDTO dto = new HotelDTO(1L, "Grand", "Luxury", "555-1", "grand@example.com", 7L);

        when(service.createHotel(dto)).thenReturn(dto);
        when(service.getHotelById(1L)).thenReturn(dto);
        when(service.getHotelById(2L)).thenReturn(null);
        when(service.getAllHotels()).thenReturn(List.of(dto)).thenThrow(new SQLException("boom"));
        when(service.updateHotel(1L, dto)).thenReturn(dto);
        when(service.updateHotel(2L, dto)).thenReturn(null);
        when(service.deleteHotel(1L)).thenReturn(true);
        when(service.deleteHotel(2L)).thenReturn(false);

        assertEquals(HttpStatus.CREATED, controller.createHotel(dto).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getHotelById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getHotelById(2L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAllHotels().getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.getAllHotels().getStatusCode());
        assertEquals(HttpStatus.OK, controller.updateHotel(1L, dto).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.updateHotel(2L, dto).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteHotel(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteHotel(2L).getStatusCode());
    }

    @Test
    void roomControllerCoversSuccessAndFailureBranches() throws Exception {
        RoomService service = mock(RoomService.class);
        RoomController controller = new RoomController(service);
        RoomDTO dto = new RoomDTO(1L, "101", 1, RoomStatus.AVAILABLE, 2L, 3L);

        when(service.createRoom(dto)).thenReturn(dto);
        when(service.getRoomById(1L)).thenReturn(dto);
        when(service.getRoomById(2L)).thenReturn(null);
        when(service.getAllRooms()).thenReturn(List.of(dto));
        when(service.updateRoom(1L, dto)).thenReturn(dto);
        when(service.updateRoom(2L, dto)).thenReturn(null);
        when(service.deleteRoom(1L)).thenReturn(true);
        when(service.deleteRoom(2L)).thenReturn(false);
        when(service.createRoom(null)).thenThrow(new SQLException("boom"));

        assertEquals(HttpStatus.CREATED, controller.createRoom(dto).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getRoomById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getRoomById(2L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAllRooms().getStatusCode());
        assertEquals(HttpStatus.OK, controller.updateRoom(1L, dto).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.updateRoom(2L, dto).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.deleteRoom(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.deleteRoom(2L).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.createRoom(null).getStatusCode());
    }

    @Test
    void roomTypeControllerCoversSuccessAndFailureBranches() throws Exception {
        RoomTypeService service = mock(RoomTypeService.class);
        RoomTypeController controller = new RoomTypeController(service);
        RoomTypeDTO dto = new RoomTypeDTO(1L, "Suite", "Large room", 4, 220.0);

        when(service.createRoomType(dto)).thenReturn(dto);
        when(service.getRoomTypeById(1L)).thenReturn(dto);
        when(service.getRoomTypeById(2L)).thenReturn(null);
        when(service.getAllRoomTypes()).thenReturn(List.of(dto));
        when(service.updateRoomType(1L, dto)).thenReturn(dto);
        when(service.updateRoomType(2L, dto)).thenReturn(null);
        when(service.deleteRoomType(1L)).thenReturn(true);
        when(service.deleteRoomType(2L)).thenReturn(false);
        when(service.createRoomType(null)).thenThrow(new SQLException("boom"));

        assertEquals(HttpStatus.CREATED, controller.createRoomType(dto).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getById(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.getById(2L).getStatusCode());
        assertEquals(HttpStatus.OK, controller.getAll().getStatusCode());
        assertEquals(HttpStatus.OK, controller.update(1L, dto).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.update(2L, dto).getStatusCode());
        assertEquals(HttpStatus.NO_CONTENT, controller.delete(1L).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND, controller.delete(2L).getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, controller.createRoomType(null).getStatusCode());
    }
}
