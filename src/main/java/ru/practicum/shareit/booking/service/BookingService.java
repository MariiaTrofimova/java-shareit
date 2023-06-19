package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.State;

import java.util.List;

public interface BookingService {

    BookingDto findById(Long userId, long bookingId);

    List<BookingDto> findByState(Long userId, State state);

    List<BookingDto> findByOwnerItemsAndState(Long userId, State state);

    BookingDto add(Long userId, BookingDto bookingDto);

    BookingDto patch(Long userId, long bookingId, boolean approved);
}
