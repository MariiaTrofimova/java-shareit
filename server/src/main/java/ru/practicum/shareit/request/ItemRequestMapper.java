package ru.practicum.shareit.request;

import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestNewDto;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class ItemRequestMapper {
    private static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();

    public static ItemRequestDto toItemRequestDto(ItemRequest itemRequest) {
        LocalDateTime created = LocalDateTime.ofInstant(itemRequest.getCreated(), ZONE_OFFSET);
        return ItemRequestDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(created)
                .build();
    }

    public static ItemRequestNewDto toItemRequestNewDto(ItemRequest itemRequest) {
        LocalDateTime created = LocalDateTime.ofInstant(itemRequest.getCreated(), ZONE_OFFSET);
        return ItemRequestNewDto.builder()
                .id(itemRequest.getId())
                .description(itemRequest.getDescription())
                .created(created)
                .build();
    }

    public static ItemRequest toItemRequest(ItemRequestDto itemRequestDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestDto.getDescription());
        return itemRequest;
    }

    public static ItemRequest toItemRequest(ItemRequestNewDto itemRequestNewDto) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setDescription(itemRequestNewDto.getDescription());
        return itemRequest;
    }
}