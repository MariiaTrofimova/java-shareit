package ru.practicum.shareit.booking.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.LocalDateTime;
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
    public BookingDto findById(Long userId, long bookingId) {
        checkIfUserExists(userId);
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() -> new NotFoundException(String.format("Бронирование с id %d не найдено", bookingId)));
        long bookerId = booking.getBooker().getId();
        long ownerId = booking.getItem().getOwner().getId();
        if (userId != bookerId && userId != ownerId) {
            log.warn("Пользователь с id {} не может просматривать бронирование с id {}", userId, bookingId);
            throw new NotFoundException(
                    String.format("Пользователь с id %d не может просматривать бронирование с id %d", userId, bookingId));
        }
        return BookingMapper.toBookingDto(booking);
    }

    @Override
    public List<BookingDto> findByState(Long userId, State state) {
        checkIfUserExists(userId);
        // список бронирований текущего пользователя
        List<Booking> bookings = new ArrayList<>();
        Sort sort = Sort.by("start_date").descending();
        LocalDateTime now = LocalDateTime.now();
        switch (state) {
            case ALL:
                bookings = repository.findByBookerId(userId, sort);
                break;
            case PAST:
                bookings = repository.findByBookerIdAndEndIsBefore(
                        userId, LocalDateTime.now(), sort);
                break;
            case FUTURE:
                bookings = repository.findByBookerIdAndStartIsAfter(
                        userId, now, sort);
                break;
            case CURRENT:
                bookings = repository.findByBookerIdAndStartIsBeforeAndEndIsAfter(
                        userId, now, now, sort);
                break;
            case WAITING:
                bookings = repository.findByBookerIdAndStatus(userId, Status.WAITING, sort);
                break;
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Override
    public List<BookingDto> findByOwnerItemsAndState(Long userId, State state) {
        checkIfUserExists(userId);
        List<Item> items = itemRepo.findByOwnerId(userId);
        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> itemIds = items.stream().map(Item::getId).collect(Collectors.toList());
        List<Booking> bookings = new ArrayList<>();
        Sort sort = Sort.by("start_date").descending();
        LocalDateTime now = LocalDateTime.now();

        switch (state) {
            case ALL:
                bookings = repository.findByItemIdIn(itemIds, sort);
                break;
            case PAST:
                bookings = repository.findByItemIdInAndEndIsBefore(
                        itemIds, LocalDateTime.now(), sort);
                break;
            case FUTURE:
                bookings = repository.findByItemIdInAndStartIsAfter(
                        itemIds, now, sort);
                break;
            case CURRENT:
                bookings = repository.findByItemIdInAndStartIsBeforeAndEndIsAfter(
                        itemIds, now, now, sort);
                break;
            case WAITING:
                bookings = repository.findByItemIdInAndStatus(itemIds, Status.WAITING, sort);
                break;
        }
        return bookings.stream().map(BookingMapper::toBookingDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public BookingDto add(Long userId, BookingDto bookingDto) {
        User booker = checkIfUserExists(userId);
        Booking booking = BookingMapper.toBooking(bookingDto);
        booking.setBooker(booker);
        return BookingMapper.toBookingDto(repository.save(booking));
    }

    @Transactional
    @Override
    public BookingDto patch(Long userId, long bookingId, boolean approved) {
        checkIfUserExists(userId);
        Status status;
        if (approved) {
            status = Status.APPROVED;
        } else {
            status = Status.REJECTED;
        }
        Booking booking = repository.updateStatus(bookingId, status);
        return BookingMapper.toBookingDto(booking);
    }

    private User checkIfUserExists(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }
}