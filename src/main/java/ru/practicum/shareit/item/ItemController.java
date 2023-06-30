package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingCommentsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import java.util.List;
import java.util.Optional;

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
    public List<ItemBookingCommentsDto> findAllByUserId(
            @RequestHeader("X-Sharer-User-Id") long userId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam Optional<Integer> size) {
        return service.findAllByUserId(userId, from, size);
    }

    @GetMapping("{itemId}")
    public ItemBookingCommentsDto findById(@RequestHeader("X-Sharer-User-Id") long userId,
                                           @PathVariable long itemId) {
        return service.findById(userId, itemId);
    }

    @GetMapping("/search")
    public List<ItemDto> findByText(
            @RequestParam String text,
            @RequestParam(defaultValue = "0") @Min(0) int from,
            @RequestParam Optional<Integer> size) {
        return service.findByText(text, from, size);
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

    @PostMapping("/{itemId}/comment")
    @ResponseStatus(HttpStatus.OK)
    public CommentDto addComment(@RequestHeader("X-Sharer-User-Id") Long userId,
                                 @Valid @RequestBody CommentDto commentDto,
                                 @PathVariable("itemId") long itemId
    ) {
        return service.addComment(userId, itemId, commentDto);
    }
}