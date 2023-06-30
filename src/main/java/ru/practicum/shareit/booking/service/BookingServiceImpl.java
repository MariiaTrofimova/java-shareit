package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static ru.practicum.shareit.validation.Validation.checkPagingParams;

@Service
@Transactional(readOnly = true)
@Slf4j
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "start");

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
    public List<BookingOutDto> findByState(Long userId, State state,
                                           int from, Optional<Integer> size) {
        //Получение списка всех бронирований текущего пользователя
        checkPagingParams(from, size);
        checkUser(userId);
        List<Booking> bookings;
        Instant now = Instant.now();
        PageRequest page = size.map(integer -> PageRequest.of(from > 0 ? from / integer : 0, integer, SORT))
                .orElse(null);
        switch (state) {
            case ALL:
                bookings = size.isPresent() ?
                        repository.findByBookerId(userId, page).getContent() :
                        repository.findByBookerId(userId, SORT);
                break;
            case PAST:
                bookings = size.isPresent() ?
                        repository.findByBookerIdAndEndIsBefore(userId, now, page).getContent() :
                        repository.findByBookerIdAndEndIsBefore(userId, now, SORT);
                break;
            case FUTURE:
                bookings = size.isPresent() ?
                        repository.findByBookerIdAndStartIsAfter(userId, now, page).getContent() :
                        repository.findByBookerIdAndStartIsAfter(userId, now, SORT);
                break;
            case CURRENT:
                bookings = size.isPresent() ?
                        repository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, now, now, page).getContent() :
                        repository.findByBookerIdAndStartIsBeforeAndEndIsAfter(userId, now, now, SORT);
                break;
            case WAITING:
                bookings = size.isPresent() ?
                        repository.findByBookerIdAndStatus(userId, Status.WAITING, page).getContent() :
                        repository.findByBookerIdAndStatus(userId, Status.WAITING, SORT);
                break;
            case REJECTED:
                bookings = size.isPresent() ?
                        repository.findByBookerIdAndStatus(userId, Status.REJECTED, page).getContent() :
                        repository.findByBookerIdAndStatus(userId, Status.REJECTED, SORT);
                break;
            default:
                log.warn("Unknown state: UNSUPPORTED_STATUS");
                throw new UnsupportedStatusException("Unknown state: UNSUPPORTED_STATUS");
        }
        return bookings.stream().map(BookingMapper::toBookingDtoOut).collect(Collectors.toList());
    }

    @Override
    public List<BookingOutDto> findByOwnerItemsAndState(Long userId, State state,
                                                        int from, Optional<Integer> size) {
        //Получение списка бронирований для всех вещей текущего пользователя
        checkPagingParams(from, size);
        checkUser(userId);
        List<Booking> bookings;
        Instant now = Instant.now();
        PageRequest page = size.map(integer -> PageRequest.of(from > 0 ? from / integer : 0, integer, SORT))
                .orElse(null);
        switch (state) {
            case ALL:
                bookings = size.isPresent() ? repository.findByItemOwnerId(userId, page).getContent() :
                        repository.findByItemOwnerId(userId, SORT);
                break;
            case PAST:
                bookings = size.isPresent() ?
                        repository.findByItemOwnerIdAndEndIsBefore(userId, now, page).getContent() :
                        repository.findByItemOwnerIdAndEndIsBefore(userId, now, SORT);
                break;
            case FUTURE:
                bookings = size.isPresent() ?
                        repository.findByItemOwnerIdAndStartIsAfter(userId, now, page).getContent() :
                        repository.findByItemOwnerIdAndStartIsAfter(userId, now, SORT);
                break;
            case CURRENT:
                bookings = size.isPresent() ?
                        repository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(userId, now, now, page).getContent() :
                        repository.findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(userId, now, now, SORT);
                break;
            case WAITING:
                bookings = size.isPresent() ?
                        repository.findByItemOwnerIdAndStatus(userId, Status.WAITING, page).getContent() :
                        repository.findByItemOwnerIdAndStatus(userId, Status.WAITING, SORT);
                break;
            case REJECTED:
                bookings = size.isPresent() ?
                        repository.findByItemOwnerIdAndStatus(userId, Status.REJECTED, page).getContent() :
                        repository.findByItemOwnerIdAndStatus(userId, Status.REJECTED, SORT);
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
        if (isOwner(userId, item)) {
            log.warn("Пользователь с id {} владелец вещи с id {}", userId, itemId);
            throw new OwnerBookingException(String.format(
                    "Пользователь с id %d владелец вещи с id %d", userId, itemId));
        }
        if (!item.isAvailable()) {
            log.warn("Вещь с id {} недоступна для бронирования", itemId);
            throw new ValidationException(String.format(
                    "Вещь с id %d  недоступна для бронирования", itemId));
        }
        if (!bookingDto.getEnd().isAfter(bookingDto.getStart())) {
            log.warn("Дата окончания бронирования должна быть после даты начала");
            throw new ValidationException("Дата окончания бронирования должна быть после даты начала");
        }
        Booking booking = BookingMapper.toBooking(bookingDto);
        Instant start = booking.getStart();
        Instant end = booking.getEnd();
        List<Booking> bookingsAtSameTime = repository.findBookingsAtSameTime(itemId, Status.APPROVED, start, end);
        if (!bookingsAtSameTime.isEmpty()) {
            log.warn("Время для аренды недоступно");
            throw new ValidationException("Время для аренды недоступно");
        }

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
        Item item = booking.getItem();
        if (!isOwner(userId, item)) {
            log.warn("Пользователь с id {} не владеет вещью с id {}", userId, item.getId());
            throw new NotFoundException(
                    String.format("Пользователь с id %d не владеет вещью с id %d", userId, item.getId()));
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

    private boolean isOwner(long userId, Item item) {
        long ownerId = item.getOwner().getId();
        return ownerId == userId;
    }
}