package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;

import static ru.practicum.shareit.validation.ValidationGroups.Create;
import static ru.practicum.shareit.validation.ValidationGroups.Update;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
@Validated
@RequiredArgsConstructor
public class ItemController {
    private final ItemService service;

    @GetMapping
    public List<ItemBookingDto> findAllByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return service.findAllByUserId(userId);
    }

    @GetMapping("{itemId}")
    public ItemDto findById(@PathVariable long itemId) {
        return service.findById(itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> findByText(@RequestParam String text) {
        return service.findByText(text);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(Create.class)
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") Long userId,
                       @Valid @RequestBody ItemDto itemDto) {
        return service.add(userId, itemDto);
    }

    @PatchMapping("/{itemId}")
    @Validated(Update.class)
    public ItemDto patch(@RequestHeader("X-Sharer-User-Id") Long userId,
                         @Valid @RequestBody ItemDto itemDto,
                         @PathVariable("itemId") long itemId) {
        return service.patch(userId, itemId, itemDto);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @PathVariable long itemId) {
        service.delete(userId, itemId);
    }
}