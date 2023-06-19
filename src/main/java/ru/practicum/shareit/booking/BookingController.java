package ru.practicum.shareit.booking;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.Valid;
import java.util.List;

/**
 * TODO Sprint add-bookings.
 */
@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService service;

    @GetMapping("{bookingId}")
    public BookingDto findById(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @PathVariable long bookingId) {
        return service.findById(userId, bookingId);
    }

    @GetMapping
    public List<BookingDto> findByState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestParam (defaultValue = "ALL") State state
    ) {
        return service.findByState(userId, state);
        //отсортированными по дате от более новых к более старым.
    }

    //GET /bookings/owner?state={state}
    //Получение списка бронирований для всех вещей текущего пользователя.
    @GetMapping("/owner")
    public List<BookingDto> findByOwnerItemsAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                        @RequestParam (defaultValue = "ALL") State state
    ) {
        return service.findByOwnerItemsAndState(userId, state);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(ValidationGroups.Create.class)
    public BookingDto add(@RequestHeader("X-Sharer-User-Id") Long userId,
                          @Valid @RequestBody BookingDto bookingDto) {
        return service.add(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    @Validated(ValidationGroups.Update.class)
    public BookingDto patch(@RequestHeader("X-Sharer-User-Id") Long userId,
                         @PathVariable("bookingId") long bookingId,
                         @RequestParam boolean approved) {
        return service.patch(userId, bookingId, approved);
    }

}
