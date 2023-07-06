package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestNewDto;

import java.util.List;

public interface ItemRequestService {
    ItemRequestNewDto add(Long userId, ItemRequestNewDto itemRequestNewDto);

    List<ItemRequestDto> findAllByUserId(Long userId);

    List<ItemRequestDto> findAll(long userId, int from, int size);

    ItemRequestDto findById(long userId, long requestId);
}
