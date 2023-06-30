package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingCommentsDto;
import ru.practicum.shareit.item.dto.ItemDto;

import java.util.List;
import java.util.Optional;

public interface ItemService {
    List<ItemBookingCommentsDto> findAllByUserId(long userId, int from, Optional<Integer> size);

    ItemBookingCommentsDto findById(long userId, long itemId);

    List<ItemDto> findByText(String text, int from, Optional<Integer> size);

    ItemDto add(long userId, ItemDto itemDto);

    ItemDto patch(long userId, long itemId, ItemDto itemDto);

    void delete(long userId, long itemId);

    CommentDto addComment(Long userId, long itemId, CommentDto commentDto);
}