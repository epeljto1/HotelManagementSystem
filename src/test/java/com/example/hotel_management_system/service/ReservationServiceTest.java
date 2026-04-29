package com.example.hotel_management_system.service;

import com.example.hotel_management_system.config.DbConfig;
import com.example.hotel_management_system.dto.ReservationDTO;
import com.example.hotel_management_system.enums.ReservationStatus;
import com.example.hotel_management_system.enums.RoomStatus;
import com.example.hotel_management_system.model.Reservation;
import com.example.hotel_management_system.model.Room;
import com.example.hotel_management_system.repository.ReservationRepository;
import com.example.hotel_management_system.repository.RoomRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private Connection connection;

    @InjectMocks
    private ReservationService reservationService;

    @Test
    void createReservationDefaultsPendingStatusAndReservationDateWhenRoomIsAvailable() throws Exception {
        Date checkInDate = asDate(LocalDateTime.of(2026, 5, 10, 14, 0));
        Date checkOutDate = asDate(LocalDateTime.of(2026, 5, 12, 11, 0));
        ReservationDTO dto = new ReservationDTO(null, null, checkInDate, checkOutDate,
                2, null, 300.0, 15L, 5L, 99L);
        Room requestedRoom = new Room(5L, "105", 1, RoomStatus.AVAILABLE, 1L, 2L);

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class);
             MockedStatic<RoomRepository> roomRepositoryStatic = mockStatic(RoomRepository.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);
            roomRepositoryStatic.when(() -> RoomRepository.findAvailableRooms(
                    connection,
                    new java.sql.Date(checkInDate.getTime()),
                    new java.sql.Date(checkOutDate.getTime())
            )).thenReturn(List.of(requestedRoom));

            doAnswer(invocation -> {
                Reservation reservation = invocation.getArgument(0);
                reservation.setId(55L);
                return null;
            }).when(reservationRepository).save(org.mockito.ArgumentMatchers.any(Reservation.class), eq(connection));

            ReservationDTO result = reservationService.createReservation(dto);

            ArgumentCaptor<Reservation> reservationCaptor = ArgumentCaptor.forClass(Reservation.class);
            verify(reservationRepository).save(reservationCaptor.capture(), eq(connection));

            Reservation savedReservation = reservationCaptor.getValue();
            assertEquals(55L, savedReservation.getId());
            assertEquals(ReservationStatus.PENDING, savedReservation.getStatus());
            assertNotNull(savedReservation.getReservationDate());
            assertEquals(ReservationStatus.PENDING, result.getStatus());
            assertEquals(5L, result.getRoomId());
        }
    }

    @Test
    void createReservationThrowsWhenRequestedRoomIsNotAvailable() throws Exception {
        Date checkInDate = asDate(LocalDateTime.of(2026, 5, 10, 14, 0));
        Date checkOutDate = asDate(LocalDateTime.of(2026, 5, 12, 11, 0));
        ReservationDTO dto = new ReservationDTO(null, null, checkInDate, checkOutDate,
                2, ReservationStatus.PENDING, 300.0, 15L, 5L, 99L);
        Room differentRoom = new Room(6L, "106", 1, RoomStatus.AVAILABLE, 1L, 2L);

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class);
             MockedStatic<RoomRepository> roomRepositoryStatic = mockStatic(RoomRepository.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);
            roomRepositoryStatic.when(() -> RoomRepository.findAvailableRooms(
                    connection,
                    new java.sql.Date(checkInDate.getTime()),
                    new java.sql.Date(checkOutDate.getTime())
            )).thenReturn(List.of(differentRoom));

            RuntimeException exception = assertThrows(RuntimeException.class,
                    () -> reservationService.createReservation(dto));
            assertTrue(exception.getMessage().contains("Soba nije dostupna"));
        }

        verify(reservationRepository, never()).save(org.mockito.ArgumentMatchers.any(Reservation.class), eq(connection));
    }

    @Test
    void getAllReservationsMapsEntitiesToDtos() throws Exception {
        Reservation first = new Reservation(1L, asDate(LocalDateTime.of(2026, 4, 1, 10, 0)),
                asDate(LocalDateTime.of(2026, 4, 10, 14, 0)),
                asDate(LocalDateTime.of(2026, 4, 12, 11, 0)),
                2, ReservationStatus.CONFIRMED, 250.0, 21L, 7L, 1L);
        Reservation second = new Reservation(2L, asDate(LocalDateTime.of(2026, 4, 2, 10, 0)),
                asDate(LocalDateTime.of(2026, 4, 15, 14, 0)),
                asDate(LocalDateTime.of(2026, 4, 18, 11, 0)),
                1, ReservationStatus.PENDING, 180.0, 22L, 8L, 1L);

        when(reservationRepository.findAll(connection)).thenReturn(List.of(first, second));

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            List<ReservationDTO> reservations = reservationService.getAllReservations();
            assertEquals(2, reservations.size());
            assertReservationSummary(reservations.get(0), 1L, ReservationStatus.CONFIRMED);
            assertReservationSummary(reservations.get(1), 2L, ReservationStatus.PENDING);
        }
    }

    @Test
    void deleteReservationReturnsFalseWhenRepositoryThrows() throws Exception {
        doThrow(new SQLException("delete failed")).when(reservationRepository).delete(44L, connection);

        try (MockedStatic<DbConfig> dbConfig = mockStatic(DbConfig.class)) {
            dbConfig.when(DbConfig::getConnection).thenReturn(connection);

            assertFalse(reservationService.deleteReservation(44L));
        }
    }

    private static void assertReservationSummary(ReservationDTO dto, long id, ReservationStatus status) {
        assertEquals(id, dto.getId());
        assertEquals(status, dto.getStatus());
    }

    private static Date asDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}
