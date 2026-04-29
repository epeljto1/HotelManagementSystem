package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.DiscountDTO;
import com.example.hotel_management_system.dto.ExtraServiceDTO;
import com.example.hotel_management_system.dto.GuestDTO;
import com.example.hotel_management_system.dto.HotelDTO;
import com.example.hotel_management_system.dto.RoomDTO;
import com.example.hotel_management_system.dto.RoomTypeDTO;
import com.example.hotel_management_system.enums.RoomStatus;
import com.example.hotel_management_system.model.Discount;
import com.example.hotel_management_system.model.ExtraService;
import com.example.hotel_management_system.model.Guest;
import com.example.hotel_management_system.model.Hotel;
import com.example.hotel_management_system.model.Room;
import com.example.hotel_management_system.model.RoomType;
import com.example.hotel_management_system.repository.DiscountRepository;
import com.example.hotel_management_system.repository.ExtraServiceRepository;
import com.example.hotel_management_system.repository.GuestRepository;
import com.example.hotel_management_system.repository.HotelRepository;
import com.example.hotel_management_system.repository.RoomRepository;
import com.example.hotel_management_system.repository.RoomTypeRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CrudSupportServicesTest {

    @Test
    void discountServiceCoversCrudAndActiveDiscountLookup() throws Exception {
        DiscountRepository repository = mock(DiscountRepository.class);
        Connection connection = mock(Connection.class);
        DiscountService service = new DiscountService(repository);
        DiscountDTO dto = new DiscountDTO(1L, "Spring", 15.0,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), "Seasonal");
        Discount model = new Discount(1L, "Spring", 15.0,
                LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), "Seasonal");

        when(repository.findById(1L, connection)).thenReturn(Optional.of(model));
        when(repository.findAll(connection)).thenReturn(List.of(model));
        when(repository.findById(9L, connection)).thenReturn(Optional.empty());
        when(repository.findById(2L, connection)).thenReturn(Optional.of(model));
        when(repository.findActiveDiscountByDate(java.sql.Date.valueOf(LocalDate.of(2026, 4, 15)), connection))
                .thenReturn(Optional.of(model));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            assertDiscountDto(dto, service.createDiscount(dto));
            assertDiscountDto(dto, service.getDiscountById(1L));
            assertEquals(1, service.getAllDiscounts().size());
            assertNull(service.updateDiscount(9L, dto));
            assertTrue(service.deleteDiscount(2L));
            assertDiscountDto(dto, service.getActiveDiscountForDate(LocalDate.of(2026, 4, 15)));
        }

        ArgumentCaptor<Discount> captor = ArgumentCaptor.forClass(Discount.class);
        verify(repository).save(captor.capture(), eq(connection));
        assertEquals("Spring", captor.getValue().getName());
        verify(repository).delete(2L, connection);
        verify(repository, never()).update(any(Discount.class), eq(connection));
    }

    @Test
    void extraServiceServiceCoversCrudBranches() throws Exception {
        ExtraServiceRepository repository = mock(ExtraServiceRepository.class);
        Connection connection = mock(Connection.class);
        ExtraServiceService service = new ExtraServiceService(repository);
        ExtraServiceDTO dto = new ExtraServiceDTO(1L, "Spa", "Massage", 40.0, "Y");
        ExtraService entity = new ExtraService(1L, "Spa", "Massage", 40.0, "Y");

        when(repository.findById(1L, connection)).thenReturn(Optional.of(entity));
        when(repository.findAll(connection)).thenReturn(List.of(entity));
        when(repository.findById(8L, connection)).thenReturn(Optional.empty());
        when(repository.findById(2L, connection)).thenReturn(Optional.of(entity));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            assertExtraServiceDto(dto, service.createExtraService(dto));
            assertExtraServiceDto(dto, service.getExtraServiceById(1L));
            assertEquals(1, service.getAllExtraServices().size());
            assertNull(service.updateExtraService(8L, dto));
            assertTrue(service.deleteExtraService(2L));
        }

        ArgumentCaptor<ExtraService> captor = ArgumentCaptor.forClass(ExtraService.class);
        verify(repository).save(captor.capture(), eq(connection));
        assertEquals(40.0, captor.getValue().getUnitPrice(), 0.001);
        verify(repository).delete(2L, connection);
    }

    @Test
    void guestServiceCoversCrudBranches() throws Exception {
        GuestRepository repository = mock(GuestRepository.class);
        Connection connection = mock(Connection.class);
        GuestService service = new GuestService(repository);
        Date birthDate = asDate(LocalDateTime.of(1990, 2, 3, 0, 0));
        GuestDTO dto = new GuestDTO(1L, "Amina", "Hadzic", "amina@example.com",
                "123", birthDate, "ID123", 5L);
        Guest entity = new Guest(1L, "Amina", "Hadzic", "amina@example.com",
                "123", birthDate, "ID123", 5L);

        when(repository.findById(1L, connection)).thenReturn(Optional.of(entity));
        when(repository.findAll(connection)).thenReturn(List.of(entity));
        when(repository.findById(8L, connection)).thenReturn(Optional.empty());
        when(repository.findById(2L, connection)).thenReturn(Optional.of(entity));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            assertGuestDto(dto, service.createGuest(dto));
            assertGuestDto(dto, service.getGuestById(1L));
            assertEquals(1, service.getAllGuests().size());
            assertNull(service.updateGuest(8L, dto));
            assertTrue(service.deleteGuest(2L));
        }

        ArgumentCaptor<Guest> captor = ArgumentCaptor.forClass(Guest.class);
        verify(repository).save(captor.capture(), eq(connection));
        assertEquals("ID123", captor.getValue().getDocumentNumber());
        verify(repository).delete(2L, connection);
    }

    @Test
    void hotelServiceCoversCrudBranches() throws Exception {
        HotelRepository repository = mock(HotelRepository.class);
        Connection connection = mock(Connection.class);
        HotelService service = new HotelService(repository);
        HotelDTO dto = new HotelDTO(1L, "Grand", "Luxury", "555-100", "grand@example.com", 7L);
        Hotel entity = new Hotel(1L, "Grand", "Luxury", "555-100", "grand@example.com", 7L);

        when(repository.findById(1L, connection)).thenReturn(Optional.of(entity));
        when(repository.findAll(connection)).thenReturn(List.of(entity));
        when(repository.findById(8L, connection)).thenReturn(Optional.empty());
        when(repository.findById(2L, connection)).thenReturn(Optional.of(entity));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            assertHotelDto(dto, service.createHotel(dto));
            assertHotelDto(dto, service.getHotelById(1L));
            assertEquals(1, service.getAllHotels().size());
            assertNull(service.updateHotel(8L, dto));
            assertTrue(service.deleteHotel(2L));
        }

        ArgumentCaptor<Hotel> captor = ArgumentCaptor.forClass(Hotel.class);
        verify(repository).save(captor.capture(), eq(connection));
        assertEquals("grand@example.com", captor.getValue().getEmail());
        verify(repository).delete(2L, connection);
    }

    @Test
    void roomServiceCoversCrudAndAvailabilityLookup() throws Exception {
        RoomRepository repository = mock(RoomRepository.class);
        Connection connection = mock(Connection.class);
        RoomService service = new RoomService(repository);
        RoomDTO dto = new RoomDTO(1L, "101", 1, RoomStatus.AVAILABLE, 2L, 3L);
        Room entity = new Room(1L, "101", 1, RoomStatus.AVAILABLE, 2L, 3L);

        when(repository.findById(1L, connection)).thenReturn(Optional.of(entity));
        when(repository.findAll(connection)).thenReturn(List.of(entity));
        doNothing().when(repository).update(any(Room.class), eq(connection));
        doNothing().when(repository).delete(9L, connection);

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class);
             MockedStatic<RoomRepository> roomRepositoryStatic = mockStatic(RoomRepository.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);
            roomRepositoryStatic.when(() -> RoomRepository.findAvailableRooms(
                    connection,
                    java.sql.Date.valueOf(LocalDate.of(2026, 6, 1)),
                    java.sql.Date.valueOf(LocalDate.of(2026, 6, 3))
            )).thenReturn(List.of(entity));

            assertRoomDto(dto, service.createRoom(dto));
            assertRoomDto(dto, service.getRoomById(1L));
            assertEquals(1, service.getAllRooms().size());
            assertEquals(1L, service.updateRoom(1L, dto).getId());
            List<RoomDTO> availableRooms = service.getAvailableRooms(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 3));
            assertEquals(1, availableRooms.size());
            assertEquals("101", availableRooms.get(0).getRoomNumber());
            assertTrue(service.deleteRoom(9L));
        }

        ArgumentCaptor<Room> saveCaptor = ArgumentCaptor.forClass(Room.class);
        ArgumentCaptor<Room> updateCaptor = ArgumentCaptor.forClass(Room.class);
        verify(repository).save(saveCaptor.capture(), eq(connection));
        verify(repository).update(updateCaptor.capture(), eq(connection));
        assertEquals("101", saveCaptor.getValue().getRoomNumber());
        assertEquals(1L, updateCaptor.getValue().getId());
    }

    @Test
    void roomServiceDeleteReturnsFalseWhenRepositoryThrows() throws Exception {
        RoomRepository repository = mock(RoomRepository.class);
        Connection connection = mock(Connection.class);
        RoomService service = new RoomService(repository);

        org.mockito.Mockito.doThrow(new RuntimeException("delete failed")).when(repository).delete(11L, connection);

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            assertFalse(service.deleteRoom(11L));
        }
    }

    @Test
    void roomTypeServiceCoversCrudBranches() throws Exception {
        RoomTypeRepository repository = mock(RoomTypeRepository.class);
        Connection connection = mock(Connection.class);
        RoomTypeService service = new RoomTypeService(repository);
        RoomTypeDTO dto = new RoomTypeDTO(1L, "Suite", "Large room", 4, 220.0);
        RoomType entity = new RoomType(1L, "Suite", "Large room", 4, 220.0);

        when(repository.findById(1L, connection)).thenReturn(Optional.of(entity));
        when(repository.findAll(connection)).thenReturn(List.of(entity));
        when(repository.findById(8L, connection)).thenReturn(Optional.empty());
        when(repository.findById(2L, connection)).thenReturn(Optional.of(entity));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            assertRoomTypeDto(dto, service.createRoomType(dto));
            assertRoomTypeDto(dto, service.getRoomTypeById(1L));
            assertEquals(1, service.getAllRoomTypes().size());
            assertNull(service.updateRoomType(8L, dto));
            assertTrue(service.deleteRoomType(2L));
        }

        ArgumentCaptor<RoomType> captor = ArgumentCaptor.forClass(RoomType.class);
        verify(repository).save(captor.capture(), eq(connection));
        assertEquals(4, captor.getValue().getCapacity());
        verify(repository).delete(2L, connection);
    }

    private static Date asDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static void assertDiscountDto(DiscountDTO expected, DiscountDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getPercentage(), actual.getPercentage(), 0.001);
        assertEquals(expected.getStartDate(), actual.getStartDate());
        assertEquals(expected.getEndDate(), actual.getEndDate());
        assertEquals(expected.getDescription(), actual.getDescription());
    }

    private static void assertExtraServiceDto(ExtraServiceDTO expected, ExtraServiceDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getUnitPrice(), actual.getUnitPrice(), 0.001);
        assertEquals(expected.getAvailable(), actual.getAvailable());
    }

    private static void assertGuestDto(GuestDTO expected, GuestDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getFirstName(), actual.getFirstName());
        assertEquals(expected.getLastName(), actual.getLastName());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getPhoneNumber(), actual.getPhoneNumber());
        assertEquals(expected.getDateOfBirth(), actual.getDateOfBirth());
        assertEquals(expected.getDocumentNumber(), actual.getDocumentNumber());
        assertEquals(expected.getAddressId(), actual.getAddressId());
    }

    private static void assertHotelDto(HotelDTO expected, HotelDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getPhoneNumber(), actual.getPhoneNumber());
        assertEquals(expected.getEmail(), actual.getEmail());
        assertEquals(expected.getAddressId(), actual.getAddressId());
    }

    private static void assertRoomDto(RoomDTO expected, RoomDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getRoomNumber(), actual.getRoomNumber());
        assertEquals(expected.getFloorNumber(), actual.getFloorNumber());
        assertEquals(expected.getStatus(), actual.getStatus());
        assertEquals(expected.getHotelId(), actual.getHotelId());
        assertEquals(expected.getRoomTypeId(), actual.getRoomTypeId());
    }

    private static void assertRoomTypeDto(RoomTypeDTO expected, RoomTypeDTO actual) {
        assertEquals(expected.getId(), actual.getId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getCapacity(), actual.getCapacity());
        assertEquals(expected.getPricePerNight(), actual.getPricePerNight(), 0.001);
    }
}
