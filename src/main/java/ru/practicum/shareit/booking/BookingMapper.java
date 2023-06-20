package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingForItemsOutDto;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public class BookingMapper {

    public static BookingOutDto toBookingDtoOut(Booking booking) {
        LocalDateTime start = LocalDateTime.ofInstant(booking.getStart(), ZoneId.of("Europe/Moscow"));
        LocalDateTime end = LocalDateTime.ofInstant(booking.getEnd(), ZoneId.of("Europe/Moscow"));
        return BookingOutDto.builder()
                .id(booking.getId())
                .start(start)
                .end(end)
                .item(ItemMapper.toItemDto(booking.getItem()))
                .booker(UserMapper.toUserDto(booking.getBooker()))
                .status(booking.getStatus())
                .build();
    }

    public static Booking toBooking(BookingInDto bookingDto) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        Instant start = bookingDto.getStart().toInstant(ZoneOffset.of("+03:00"));
        Instant end = bookingDto.getEnd().toInstant(ZoneOffset.of("+03:00"));
        booking.setStart(start);
        booking.setEnd(end);
        Item item = new Item();
        item.setId(bookingDto.getItemId());
        booking.setItem(item);
        if (bookingDto.getStatus() != null) {
            Status status = Status.valueOf(bookingDto.getStatus().toUpperCase());
            booking.setStatus(status);
        }
        return booking;
    }

    public static BookingForItemsOutDto toBookingForItemsOutDto(Booking booking) {
        LocalDateTime start = LocalDateTime.ofInstant(booking.getStart(), ZoneId.of("+0"));
        LocalDateTime end = LocalDateTime.ofInstant(booking.getEnd(), ZoneId.of("+0"));
        return BookingForItemsOutDto.builder()
                .id(booking.getId())
                .start(start)
                .end(end)
                .bookerId(booking.getBooker().getId())
                .status(booking.getStatus())
                .build();
    }
}