package ru.practicum.shareit.request.service;

import ru.practicum.shareit.request.dto.ItemRequestDto;

import java.util.List;
import java.util.Optional;

public interface ItemRequestService {
    ItemRequestDto add(Long userId, ItemRequestDto itemRequestDto);

    List<ItemRequestDto> findAllByUserId(Long userId);

    List<ItemRequestDto> findAll(long userId, int from, Optional<Integer> size);

    ItemRequestDto findById(long userId, long requestId);
}
