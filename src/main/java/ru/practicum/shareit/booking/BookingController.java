package ru.practicum.shareit.booking;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/bookings")
@RequiredArgsConstructor
@Validated
public class BookingController {
    private final BookingService service;

    private final ObjectMapper mapper = JsonMapper.builder()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_USING_DEFAULT_VALUE, true)
            .build();


    @GetMapping("{bookingId}")
    public BookingOutDto findById(@RequestHeader("X-Sharer-User-Id") Long userId,
                                  @PathVariable long bookingId) {
        return service.findById(userId, bookingId);
    }

    @GetMapping
    public List<BookingOutDto> findByState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                           @RequestParam(defaultValue = "ALL") String state,
                                           @RequestParam(defaultValue = "0") @Min(value = 0,
                                                   message = "Индекс первого элемента не может быть отрицательным") int from,
                                           @RequestParam Optional<Integer> size) throws JsonProcessingException {
        State stateEnum = mapper.readValue(mapper.writeValueAsString(state.toUpperCase()), State.class);
        return service.findByState(userId, stateEnum, from, size);
    }

    @GetMapping("/owner")
    public List<BookingOutDto> findByOwnerItemsAndState(@RequestHeader("X-Sharer-User-Id") Long userId,
                                                        @RequestParam(defaultValue = "ALL") String state,
                                                        @RequestParam(defaultValue = "0")
                                                        @Min(value = 0,
                                                                message = "Индекс первого элемента не может быть отрицательным")
                                                        int from,
                                                        @RequestParam Optional<Integer> size
    ) throws JsonProcessingException {
        State stateEnum = mapper.readValue(mapper.writeValueAsString(state.toUpperCase()), State.class);
        return service.findByOwnerItemsAndState(userId, stateEnum, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(ValidationGroups.Create.class)
    public BookingOutDto add(@RequestHeader("X-Sharer-User-Id") Long userId,
                             @Valid @RequestBody BookingInDto bookingDto) {
        return service.add(userId, bookingDto);
    }

    @PatchMapping("/{bookingId}")
    @ResponseStatus(HttpStatus.OK)
    @Validated(ValidationGroups.Update.class)
    public BookingOutDto patch(@RequestHeader("X-Sharer-User-Id") Long userId,
                               @PathVariable("bookingId") long bookingId,
                               @RequestParam boolean approved) {
        return service.patch(userId, bookingId, approved);
    }

}