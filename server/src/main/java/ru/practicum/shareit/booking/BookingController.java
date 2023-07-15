package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.service.BookingService;

import java.util.List;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService service;

    @GetMapping("{bookingId}")
    public BookingOutDto findById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                  @PathVariable long bookingId) {
        return service.findById(userId, bookingId);
    }

    @GetMapping
    public List<BookingOutDto> findByState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam String state,
                                           @RequestParam int from,
                                           @RequestParam int size) {
        State stateEnum = State.from(state);
        return service.findByState(userId, stateEnum, from, size);
    }

    @GetMapping("/owner")
    public List<BookingOutDto> findByOwnerItemsAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                        @RequestParam String state,
                                                        @RequestParam int from,
                                                        @RequestParam int size) {
        State stateEnum = State.from(state);
        return service.findByOwnerItemsAndState(userId, stateEnum, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingOutDto add(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @RequestBody BookingInDto bookingDto) {
        return service.add(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    public BookingOutDto patch(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @PathVariable("bookingId") long bookingId,
                               @RequestParam boolean approved) {
        return service.patch(userId, bookingId, approved);
    }
}