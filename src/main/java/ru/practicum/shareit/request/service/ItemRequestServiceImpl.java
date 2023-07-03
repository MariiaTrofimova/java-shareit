package ru.practicum.shareit.request.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.validation.Validation;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@AllArgsConstructor
public class ItemRequestServiceImpl implements ItemRequestService {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    private final ItemRequestRepository repository;
    private final UserRepository userRepo;
    private final ItemRepository itemRepo;

    @Override
    @Transactional
    public ItemRequestDto add(Long userId, ItemRequestDto itemRequestDto) {
        User requestor = checkUser(userId);
        ItemRequest itemRequest = ItemRequestMapper.toItemRequest(itemRequestDto);
        itemRequest.setRequestor(requestor);
        itemRequest = repository.save(itemRequest);
        return ItemRequestMapper.toItemRequestDto(itemRequest);
    }

    @Override
    public List<ItemRequestDto> findAllByUserId(Long userId) {
        checkUser(userId);
        List<ItemRequestDto> itemRequestDtos = repository.findByRequestorId(userId).stream()
                .map(ItemRequestMapper::toItemRequestDto).collect(Collectors.toList());
        addItemsToRequests(itemRequestDtos);
        return itemRequestDtos;
    }

    @Override
    public List<ItemRequestDto> findAll(long userId, int from, Optional<Integer> sizeOptional) {
        checkUser(userId);
        Validation.checkPagingParams(from, sizeOptional);
        List<ItemRequestDto> itemRequestDtos;
        if (sizeOptional.isEmpty()) {
            itemRequestDtos = repository.findByRequestorIdNot(userId, SORT).stream()
                    .map(ItemRequestMapper::toItemRequestDto).collect(Collectors.toList());
        } else {
            int size = sizeOptional.get();
            PageRequest page = PageRequest.of(from > 0 ? from / size : 0, size, SORT);
            itemRequestDtos = repository.findByRequestorIdNot(userId, page)
                    .map(ItemRequestMapper::toItemRequestDto).getContent();
        }
        addItemsToRequests(itemRequestDtos);
        return itemRequestDtos;
    }

    @Override
    public ItemRequestDto findById(long userId, long requestId) {
        checkUser(userId);
        ItemRequest itemRequest = repository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("Запрос с id %d не найден", requestId)));
        ItemRequestDto requestDto = ItemRequestMapper.toItemRequestDto(itemRequest);
        List<ItemDto> items = itemRepo.findByRequestId(requestId).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
        requestDto.addAllItems(items);
        return requestDto;
    }

    private User checkUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }

    private void addItemsToRequests(List<ItemRequestDto> itemRequestDtos) {
        List<Long> requestIds = itemRequestDtos.stream().map(ItemRequestDto::getId).collect(Collectors.toList());
        List<ItemDto> itemDtos = itemRepo.findByRequestIdIn(requestIds).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());

        if (itemDtos.isEmpty()) {
            return;
        }
        Map<Long, ItemRequestDto> requests = new HashMap<>();
        Map<Long, List<ItemDto>> items = new HashMap<>();

        itemDtos.forEach(itemDto -> items.computeIfAbsent(itemDto.getRequestId(), key -> new ArrayList<>()).add(itemDto));
        itemRequestDtos.forEach(request -> requests.put(request.getId(), request));
        items.forEach((key, value) -> requests.get(key).addAllItems(value));
    }
}
