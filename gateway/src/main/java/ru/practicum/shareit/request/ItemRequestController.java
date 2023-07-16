package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.request.dto.ItemRequestNewDto;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;

@RestController
@RequestMapping(path = "/requests")
@Validated
@RequiredArgsConstructor
public class ItemRequestController {
    private final ItemRequestClient requestClient;

    @PostMapping
    @Validated(ValidationGroups.Create.class)
    public ResponseEntity<Object> addRequest(
            @RequestHeader("X-Sharer-User-Id") Long userId,
            @Valid @RequestBody ItemRequestNewDto itemRequestNewDto
    ) {
        return requestClient.addRequest(userId, itemRequestNewDto);
    }

    @GetMapping
    public ResponseEntity<Object> findAllByUserId(@RequestHeader("X-Sharer-User-Id") Long userId) {
        return requestClient.findRequestsByUserId(userId);
    }

    @GetMapping("/all")
    public ResponseEntity<Object> findAll(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(defaultValue = "0")
            @PositiveOrZero(message = "Индекс первого элемента не может быть отрицательным")
            Integer from,
            @RequestParam(defaultValue = "10")
            @Positive(message = "Количество элементов для отображения должно быть положительным")
            Integer size) {
        return requestClient.findAllRequests(userId, from, size);
    }

    @GetMapping("/{requestId}")
    public ResponseEntity<Object> findById(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @PathVariable long requestId) {
        return requestClient.findRequestById(userId, requestId);
    }
}
