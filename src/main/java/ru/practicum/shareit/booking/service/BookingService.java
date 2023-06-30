package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.State;

import java.util.List;
import java.util.Optional;

public interface BookingService {

    BookingOutDto findById(Long userId, long bookingId);

    List<BookingOutDto> findByState(Long userId, State state, int from, Optional<Integer> size);

    List<BookingOutDto> findByOwnerItemsAndState(Long userId, State state, int from, Optional<Integer> size);

    BookingOutDto add(Long userId, BookingInDto bookingDto);

    BookingOutDto patch(Long userId, long bookingId, boolean approved);
}