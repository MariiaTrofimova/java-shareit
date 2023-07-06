package ru.practicum.shareit.booking.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.error.exception.OwnerBookingException;
import ru.practicum.shareit.error.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "start");
    private static final Instant NOW = Instant.now();
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();

    @Mock
    BookingRepository repository;

    @Mock
    private UserRepository userRepo;

    @Mock
    private ItemRepository itemRepo;

    @InjectMocks
    BookingServiceImpl service;

    private User owner;
    private User booker;

    private User user;
    private Item item;
    private Booking booking;
    private Booking booking2;

    @BeforeEach
    void setup() {
        Instant start = NOW.minusSeconds(120);
        Instant end = NOW.minusSeconds(60);
        owner = new User();
        owner.setName("name");
        owner.setEmail("e@mail.ru");
        owner.setId(1L);

        booker = new User();
        booker.setName("name2");
        booker.setEmail("e2@mail.ru");
        booker.setId(2L);

        user = new User();
        user.setName("name3");
        user.setEmail("e3@mail.ru");
        user.setId(3L);

        item = new Item();
        item.setId(1L);
        item.setName("Дрель");
        item.setDescription("Хочешь, задрелю соседей, что мешают спать?");
        item.setAvailable(true);
        item.setOwner(owner);

        booking = new Booking();
        booking.setId(1L);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.APPROVED);

        booking2 = new Booking();
        booking2.setId(2L);
        booking2.setStart(start.plusSeconds(10));
        booking2.setEnd(end.plusSeconds(10));
        booking2.setItem(item);
        booking2.setBooker(booker);
        booking2.setStatus(Status.APPROVED);
    }

    @Test
    void findById() {
        //Fail No Rights
        long userId = user.getId();
        long bookingId = booking.getId();
        when(userRepo.findById(userId)).thenReturn(Optional.of(user));
        when(repository.findById(bookingId)).thenReturn(Optional.of(booking));
        String error = String.format(
                "Пользователь с id %d не может просматривать бронирование с id %d", userId, bookingId);
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.findById(userId, bookingId)
        );
        assertEquals(error, exception.getMessage());

        //Regular Case with owner
        long ownerId = owner.getId();
        when(userRepo.findById(ownerId)).thenReturn(Optional.of(owner));
        when(repository.findById(bookingId)).thenReturn(Optional.of(booking));
        BookingOutDto bookingOutDto = service.findById(ownerId, bookingId);
        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());

        //Regular Case with booker
        long bookerId = owner.getId();
        when(userRepo.findById(bookerId)).thenReturn(Optional.of(booker));
        when(repository.findById(bookingId)).thenReturn(Optional.of(booking));
        bookingOutDto = service.findById(bookerId, bookingId);
        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
    }

    @Test
    void findByState() {
        int from = 0;
        int size = 1;
        long userId = booker.getId();
        PageRequest page = PageRequest.of(0, size, SORT);
        when(userRepo.findById(userId)).thenReturn(Optional.of(booker));

        //Fail By Wrong State
        String error = "Unknown state: UNSUPPORTED_STATUS";
        UnsupportedStatusException exception = assertThrows(
                UnsupportedStatusException.class,
                () -> service.findByState(userId, State.UNKNOWN, from, Optional.of(size))
        );
        assertEquals(error, exception.getMessage());

        //State All
        when(repository.findByBookerId(userId, page)).thenReturn(new PageImpl<>(List.of(booking)));
        List<BookingOutDto> bookingOutDtos = service.findByState(userId, State.ALL, from, Optional.of(size));

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());

        when(repository.findByBookerId(userId, SORT)).thenReturn(List.of(booking));
        List<BookingOutDto> bookingOutDtosWithoutPaging = service.findByState(userId, State.ALL, from, Optional.empty());

        assertNotNull(bookingOutDtosWithoutPaging);
        assertEquals(1, bookingOutDtosWithoutPaging.size());
        assertEquals(booking.getId(), bookingOutDtosWithoutPaging.get(0).getId());

        //State PAST
        when(repository.findByBookerIdAndEndIsBefore(anyLong(), any(), (Pageable) any())).thenReturn(new PageImpl<>(List.of(booking)));
        when(repository.findByBookerIdAndEndIsBefore(anyLong(), any(), (Sort) any())).thenReturn(List.of(booking));

        bookingOutDtos = service.findByState(userId, State.PAST, from, Optional.of(size));
        bookingOutDtosWithoutPaging = service.findByState(userId, State.PAST, from, Optional.empty());

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertNotNull(bookingOutDtosWithoutPaging);
        assertEquals(1, bookingOutDtosWithoutPaging.size());

        //State CURRENT
        booking.setEnd(NOW.plusSeconds(120));

        when(repository.findByBookerIdAndStartIsBeforeAndEndIsAfter(anyLong(), any(), any(), (Pageable) any())).thenReturn(new PageImpl<>(List.of(booking)));
        when(repository.findByBookerIdAndStartIsBeforeAndEndIsAfter(anyLong(), any(), any(), (Sort) any())).thenReturn(List.of(booking));

        bookingOutDtos = service.findByState(userId, State.CURRENT, from, Optional.of(size));
        bookingOutDtosWithoutPaging = service.findByState(userId, State.CURRENT, from, Optional.empty());

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertNotNull(bookingOutDtosWithoutPaging);
        assertEquals(1, bookingOutDtosWithoutPaging.size());

        //State FUTURE
        booking.setStart(NOW.plusSeconds(60));

        when(repository.findByBookerIdAndStartIsAfter(anyLong(), any(), (Pageable) any())).thenReturn(new PageImpl<>(List.of(booking)));
        when(repository.findByBookerIdAndStartIsAfter(anyLong(), any(), (Sort) any())).thenReturn(List.of(booking));

        bookingOutDtos = service.findByState(userId, State.FUTURE, from, Optional.of(size));
        bookingOutDtosWithoutPaging = service.findByState(userId, State.FUTURE, from, Optional.empty());

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertNotNull(bookingOutDtosWithoutPaging);
        assertEquals(1, bookingOutDtosWithoutPaging.size());

        //STATE WAITING
        booking.setStatus(Status.WAITING);

        when(repository.findByBookerIdAndStatus(anyLong(), any(), (Pageable) any())).thenReturn(new PageImpl<>(List.of(booking)));
        when(repository.findByBookerIdAndStatus(anyLong(), any(), (Sort) any())).thenReturn(List.of(booking));

        bookingOutDtos = service.findByState(userId, State.WAITING, from, Optional.of(size));
        bookingOutDtosWithoutPaging = service.findByState(userId, State.WAITING, from, Optional.empty());

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertNotNull(bookingOutDtosWithoutPaging);
        assertEquals(1, bookingOutDtosWithoutPaging.size());

        //SATE REJECTING
        booking.setStatus(Status.REJECTED);

        bookingOutDtos = service.findByState(userId, State.REJECTED, from, Optional.of(size));
        bookingOutDtosWithoutPaging = service.findByState(userId, State.REJECTED, from, Optional.empty());

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertNotNull(bookingOutDtosWithoutPaging);
        assertEquals(1, bookingOutDtosWithoutPaging.size());
    }

    @Test
    void findByOwnerItemsAndState() {
        int from = 0;
        int size = 1;
        long userId = owner.getId();
        PageRequest page = PageRequest.of(0, size, SORT);
        when(userRepo.findById(userId)).thenReturn(Optional.of(owner));

        //Fail By Wrong State
        String error = "Unknown state: UNSUPPORTED_STATUS";
        UnsupportedStatusException exception = assertThrows(
                UnsupportedStatusException.class,
                () -> service.findByOwnerItemsAndState(userId, State.UNKNOWN, from, Optional.of(size))
        );
        assertEquals(error, exception.getMessage());

        //State ALL
        when(repository.findByItemOwnerId(userId, page)).thenReturn(new PageImpl<>(List.of(booking)));
        when(repository.findByItemOwnerId(userId, SORT)).thenReturn(List.of(booking));

        List<BookingOutDto> bookingOutDtos = service.findByOwnerItemsAndState(userId, State.ALL, from, Optional.of(size));
        List<BookingOutDto> bookingOutDtosWithoutPaging = service.findByOwnerItemsAndState(userId, State.ALL, from, Optional.empty());

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());
        assertEquals(booking.getId(), bookingOutDtos.get(0).getId());

        assertNotNull(bookingOutDtosWithoutPaging);
        assertEquals(1, bookingOutDtosWithoutPaging.size());

        //State PAST
        when(repository.findByItemOwnerIdAndEndIsBefore(anyLong(), any(), (Pageable) any())).thenReturn(new PageImpl<>(List.of(booking)));
        when(repository.findByItemOwnerIdAndEndIsBefore(anyLong(), any(), (Sort) any())).thenReturn(List.of(booking));

        bookingOutDtos = service.findByOwnerItemsAndState(userId, State.PAST, from, Optional.of(size));
        bookingOutDtosWithoutPaging = service.findByOwnerItemsAndState(userId, State.PAST, from, Optional.empty());

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        assertNotNull(bookingOutDtosWithoutPaging);
        assertEquals(1, bookingOutDtosWithoutPaging.size());

        //STATE CURRENT
        booking.setEnd(NOW.plusSeconds(120));
        when(repository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(anyLong(), any(), any(), (Pageable) any())).thenReturn(new PageImpl<>(List.of(booking)));
        when(repository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(anyLong(), any(), any(), (Sort) any())).thenReturn(List.of(booking));

        bookingOutDtos = service.findByOwnerItemsAndState(userId, State.CURRENT, from, Optional.of(size));
        bookingOutDtosWithoutPaging = service.findByOwnerItemsAndState(userId, State.CURRENT, from, Optional.empty());

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        assertNotNull(bookingOutDtosWithoutPaging);
        assertEquals(1, bookingOutDtosWithoutPaging.size());

        //State FUTURE
        booking.setStart(NOW.plusSeconds(60));
        when(repository.findByItemOwnerIdAndStartIsAfter(anyLong(), any(), (Pageable) any())).thenReturn(new PageImpl<>(List.of(booking)));
        when(repository.findByItemOwnerIdAndStartIsAfter(anyLong(), any(), (Sort) any())).thenReturn(List.of(booking));

        bookingOutDtos = service.findByOwnerItemsAndState(userId, State.FUTURE, from, Optional.of(size));
        bookingOutDtosWithoutPaging = service.findByOwnerItemsAndState(userId, State.FUTURE, from, Optional.empty());

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        assertNotNull(bookingOutDtosWithoutPaging);
        assertEquals(1, bookingOutDtosWithoutPaging.size());

        //State WAITING
        booking.setStatus(Status.WAITING);
        when(repository.findByItemOwnerIdAndStatus(anyLong(), any(), (Pageable) any())).thenReturn(new PageImpl<>(List.of(booking)));
        when(repository.findByItemOwnerIdAndStatus(anyLong(), any(), (Sort) any())).thenReturn(List.of(booking));

        bookingOutDtos = service.findByOwnerItemsAndState(userId, State.WAITING, from, Optional.of(size));
        bookingOutDtosWithoutPaging = service.findByOwnerItemsAndState(userId, State.WAITING, from, Optional.empty());

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        assertNotNull(bookingOutDtosWithoutPaging);
        assertEquals(1, bookingOutDtosWithoutPaging.size());

        //State REJECT
        booking.setStatus(Status.REJECTED);
        when(repository.findByItemOwnerIdAndStatus(anyLong(), any(), (Pageable) any())).thenReturn(new PageImpl<>(List.of(booking)));
        when(repository.findByItemOwnerIdAndStatus(anyLong(), any(), (Sort) any())).thenReturn(List.of(booking));

        bookingOutDtos = service.findByOwnerItemsAndState(userId, State.REJECTED, from, Optional.of(size));
        bookingOutDtosWithoutPaging = service.findByOwnerItemsAndState(userId, State.REJECTED, from, Optional.empty());

        assertNotNull(bookingOutDtos);
        assertEquals(1, bookingOutDtos.size());

        assertNotNull(bookingOutDtosWithoutPaging);
        assertEquals(1, bookingOutDtosWithoutPaging.size());
    }

    @Test
    void add() {
        //Fail By OwnerBookingException
        long ownerId = owner.getId();
        long itemId = item.getId();
        LocalDateTime start = LocalDateTime.ofInstant(booking.getStart(), ZONE_ID);
        LocalDateTime end = LocalDateTime.ofInstant(booking.getEnd(), ZONE_ID);
        BookingInDto bookingToSave = BookingInDto.builder()
                .itemId(itemId)
                .start(start)
                .end(end)
                .build();

        when(userRepo.findById(ownerId)).thenReturn(Optional.of(owner));
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        String error = String.format("Пользователь с id %d владелец вещи с id %d", ownerId, itemId);
        OwnerBookingException exception = assertThrows(
                OwnerBookingException.class,
                () -> service.add(ownerId, bookingToSave));
        assertEquals(error, exception.getMessage());

        //Fail By Item Isn't available
        item.setAvailable(false);
        long bookerId = booker.getId();
        when(userRepo.findById(bookerId)).thenReturn(Optional.of(booker));
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        error = String.format("Вещь с id %d  недоступна для бронирования", itemId);
        ValidationException ex = assertThrows(
                ValidationException.class,
                () -> service.add(bookerId, bookingToSave));
        assertEquals(error, ex.getMessage());

        //Fail By Date Validation
        item.setAvailable(true);
        when(itemRepo.findById(itemId)).thenReturn(Optional.of(item));
        when(repository.findBookingsAtSameTime(itemId, Status.APPROVED, booking.getStart(), booking.getEnd()))
                .thenReturn(List.of(booking2));
        error = "Время для аренды недоступно";
        ex = assertThrows(
                ValidationException.class,
                () -> service.add(bookerId, bookingToSave));
        assertEquals(error, ex.getMessage());

        //Regular case
        when(repository.findBookingsAtSameTime(itemId, Status.APPROVED, booking.getStart(), booking.getEnd()))
                .thenReturn(Collections.emptyList());
        when(repository.save(any())).thenReturn(booking);
        BookingOutDto bookingOutDto = service.add(bookerId, bookingToSave);

        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
    }

    @Test
    void patch() {
        //Fail By Double Status
        long userId = owner.getId();
        long bookingId = booking.getId();

        when(userRepo.findById(userId)).thenReturn(Optional.of(owner));
        when(repository.findById(bookingId)).thenReturn(Optional.of(booking));

        String error = String.format("Бронирование с id %d уже подтверждено", bookingId);
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> service.patch(userId, bookingId, true)
        );
        assertEquals(error, exception.getMessage());

        //Regular Case
        when(repository.save(any())).thenReturn(booking);

        BookingOutDto bookingOutDto = service.patch(userId, bookingId, false);
        assertNotNull(bookingOutDto);
        assertEquals(booking.getId(), bookingOutDto.getId());
    }
}