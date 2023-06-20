package ru.practicum.shareit.booking.service;

import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;

import java.util.List;

public interface BookingService {

    BookingOutDto findById(Long userId, long bookingId);

    List<BookingOutDto> findByState(Long userId, String state);

    List<BookingOutDto> findByOwnerItemsAndState(Long userId, String state);

    BookingOutDto add(Long userId, BookingInDto bookingDto);

    BookingOutDto patch(Long userId, long bookingId, boolean approved);
}