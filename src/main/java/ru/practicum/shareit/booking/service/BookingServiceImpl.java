package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository repository;
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;

    @Override
    public BookingOutDto findById(Long userId, long bookingId) {
        checkUser(userId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование с id %d не найдено", bookingId)));
        long bookerId = booking.getBooker().getId();
        long ownerId = booking.getItem().getOwner().getId();
        if (userId != bookerId && userId != ownerId) {
            log.warn("Пользователь с id {} не может просматривать бронирование с id {}", userId, bookingId);
            throw new NotFoundException(
                    String.format("Пользователь с id %d не может просматривать бронирование с id %d", userId, bookingId));
        }
        return BookingMapper.toBookingDtoOut(booking);
    }

    @Override
    public List<BookingOutDto> findByState(Long userId, State state) {
        checkUser(userId);
        List<Booking> bookings = new ArrayList<>();
        Instant now = Instant.now();
        switch (state) {
            case ALL:
                bookings = repository.findByBookerIdOrderByStartDesc(userId);
                break;
            case PAST:
                bookings = repository.findByBookerIdAndEndIsBeforeOrderByStartDesc(userId, now);
                break;
            case FUTURE:
                bookings = repository.findByBookerIdAndStartIsAfterOrderByStartDesc(userId, now);
                break;
            case CURRENT:
                bookings = repository.findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(userId, now, now);
                break;
            case WAITING:
                bookings = repository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.WAITING);
                break;
            case REJECTED:
                bookings = repository.findByBookerIdAndStatusOrderByStartDesc(userId, Status.REJECTED);
                break;
            case UNKNOWN:
                log.warn("Unknown state: UNSUPPORTED_STATUS");
                throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookings.stream().map(BookingMapper::toBookingDtoOut).collect(Collectors.toList());
    }

    @Override
    public List<BookingOutDto> findByOwnerItemsAndState(Long userId, State state) {
        checkUser(userId);
        List<Item> items = itemRepo.findByOwnerId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        List<Booking> bookings;
        Instant now = Instant.now();

        switch (state) {
            case ALL:
                bookings = repository.findByItemIdInOrderByStartDesc(itemIds);
                break;
            case PAST:
                bookings = repository.findByItemIdInAndEndIsBeforeOrderByStartDesc(itemIds, now);
                break;
            case FUTURE:
                bookings = repository.findByItemIdInAndStartIsAfterOrderByStartDesc(itemIds, now);
                break;
            case CURRENT:
                bookings = repository.findByItemIdInAndStartIsBeforeAndEndIsAfterOrderByStartDesc(itemIds, now, now);
                break;
            case WAITING:
                bookings = repository.findByItemIdInAndStatusOrderByStartDesc(itemIds, Status.WAITING);
                break;
            case REJECTED:
                bookings = repository.findByItemIdInAndStatusOrderByStartDesc(itemIds, Status.REJECTED);
                break;
            default:
                log.warn("Unknown state: UNSUPPORTED_STATUS");
                throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookings.stream().map(BookingMapper::toBookingDtoOut).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public BookingOutDto add(Long userId, BookingInDto bookingDto) {
        User booker = checkUser(userId);
        long itemId = bookingDto.getItemId();
        Item item = checkItem(itemId);
        if (isOwner(userId, itemId)) {
            log.warn("Пользователь с id {} владелец вещи с id {}", userId, item.getId());
            throw new OwnerBookingException(String.format(
                    "Пользователь с id %d владелец вещи с id %d", userId, item.getId()));
        }
        if (!item.isAvailable()) {
            log.warn("Вещь с id {} недоступна для бронирования", item.getId());
            throw new ValidationException(String.format(
                    "Вещь с id %d  недоступна для бронирования", item.getId()));
        }
        if (!bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            log.warn("Дата окончания бронирования должна быть после даты начала");
            throw new ValidationException("Дата окончания бронирования должна быть после даты начала");
        }
        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
        booking = repository.save(booking);
        booking.setItem(item);
        return BookingMapper.toBookingDtoOut(booking);
    }

    @Transactional
    @Override
    public BookingOutDto patch(Long userId, long bookingId, boolean approved) {
        checkUser(userId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование с id %d не найдено", bookingId)));
        long itemId = booking.getItem().getId();
        if (!isOwner(userId, itemId)) {
            log.warn("Пользователь с id {} не владеет вещью с id {}", userId, itemId);
            throw new NotFoundException(String.format("Пользователь с id %d не владеет вещью с id %d", userId, itemId));
        }
        Status status;
        if (approved) {
            if (booking.getStatus().equals(Status.APPROVED)) {
                log.warn("Бронирование с id {} уже подтверждено", bookingId);
                throw new ValidationException(String.format("Бронирование с id %d уже подтверждено", bookingId));
            }
            status = Status.APPROVED;
        } else {
            if (booking.getStatus().equals(Status.REJECTED)) {
                log.warn("Бронирование с id {} уже отклонено", bookingId);
                throw new ValidationException(String.format("Бронирование с id %d уже отклонено", bookingId));
            }
            status = Status.REJECTED;
        }
        booking.setStatus(status);
        booking = repository.save(booking);
        return BookingMapper.toBookingDtoOut(booking);
    }

    private User checkUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }

    private Item checkItem(Long itemId) {
        return itemRepo.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с id %d не найдена", itemId)));
    }

    private boolean isOwner(long userId, long itemId) {
        return itemRepo.findByOwnerId(userId).stream().anyMatch(it -> it.getId() == itemId);
    }
}