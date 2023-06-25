package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingForItemsOutDto;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.UserMapper;

import java.time.*;

public class BookingMapper {
    private static final ZoneId ZONE_ID = ZoneId.systemDefault();
    private static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();

    public static BookingOutDto toBookingDtoOut(Booking booking) {
        LocalDateTime start = LocalDateTime.ofInstant(booking.getStart(), ZONE_ID);
        LocalDateTime end = LocalDateTime.ofInstant(booking.getEnd(), ZONE_ID);
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
        Instant start = bookingDto.getStart().toInstant(ZONE_OFFSET);
        Instant end = bookingDto.getEnd().toInstant(ZONE_OFFSET);
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
        LocalDateTime start = LocalDateTime.ofInstant(booking.getStart(), ZONE_ID);
        LocalDateTime end = LocalDateTime.ofInstant(booking.getEnd(), ZONE_ID);
        return BookingForItemsOutDto.builder()
                .id(booking.getId())
                .start(start)
                .end(end)
                .bookerId(booking.getBooker().getId())
                .status(booking.getStatus())
                .build();
    }
}