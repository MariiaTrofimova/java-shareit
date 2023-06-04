package ru.practicum.shareit.item;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * TODO Sprint add-controllers.
 */
@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemService service;

    public ItemController(ItemService service) {
        this.service = service;
    }

    @GetMapping
    public List<ItemDto> findAllByUserId(@RequestHeader("X-Sharer-User-Id") long userId) {
        return service.findAllByUserId(userId).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @GetMapping("{itemId}")
    public ItemDto findById(@PathVariable long itemId) {
        return ItemMapper.toItemDto(service.findById(itemId));
    }

    @GetMapping("/search")
    public List<ItemDto> findByText(@RequestParam String text) {
        return service.findByText(text).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ItemDto add(@RequestHeader("X-Sharer-User-Id") Long userId,
                       @Valid @RequestBody ItemDto itemDto) {
        return ItemMapper.toItemDto(service.add(userId, ItemMapper.toItem(itemDto)));
    }

    @PatchMapping("/{itemId}")
    public Item patch(@RequestHeader("X-Sharer-User-Id") Long userId,
                      @RequestBody Map<String, String> updates,
                      @PathVariable("itemId") long itemId) {
        return service.patch(userId, itemId, updates);
    }

    @DeleteMapping("/{itemId}")
    public void deleteItem(@RequestHeader("X-Sharer-User-Id") long userId,
                           @PathVariable long itemId) {
        service.delete(userId, itemId);
    }
}