package ru.practicum.shareit.booking;

import ru.practicum.shareit.booking.dto.BookingDto;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class BookingMapper {
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd hh:mm:ss").withZone(ZoneOffset.UTC);

    public static BookingDto toBookingDto(Booking booking) {
        String start = formatter.format(booking.getStart());
        String end = formatter.format(booking.getStart());
        return BookingDto.builder()
                .id(booking.getId())
                .start(start)
                .end(end)
                .item(booking.getItem())
                .booker(booking.getBooker())
                .status(booking.getStatus())
                .build();
    }

    public static Booking toBooking(BookingDto bookingDto) {
        Booking booking = new Booking();
        booking.setId(bookingDto.getId());
        LocalDateTime start = LocalDateTime.parse(bookingDto.getStart(), formatter);
        LocalDateTime end = LocalDateTime.parse(bookingDto.getEnd(), formatter);
        booking.setStart(start);
        booking.setEnd(end);
        booking.setBooker(bookingDto.getBooker());
        booking.setItem(bookingDto.getItem());
        booking.setStatus(bookingDto.getStatus());
        return booking;
    }
}
