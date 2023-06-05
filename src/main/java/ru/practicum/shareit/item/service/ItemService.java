package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;

public interface ItemService {
    List<ItemDto> findAllByUserId(long userId);

    ItemDto findById(long itemId);

    List<ItemDto> findByText(String text);

    ItemDto add(long userId, ItemDto itemDto);

    ItemDto patch(long userId, long itemId, ItemDto itemDto);

    void delete(long userId, long itemId);
}
