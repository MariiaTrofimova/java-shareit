package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repo;
    private final UserRepository userRepo;

    public ItemServiceImpl(ItemRepository repo, UserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    @Override
    public List<ItemDto> findAllByUserId(long userId) {
        userRepo.findById(userId);
        return repo.findAllByUserId(userId).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public ItemDto findById(long itemId) {
        return ItemMapper.toItemDto(repo.findById(itemId));
    }

    @Override
    public List<ItemDto> findByText(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return repo.findByText(text.toLowerCase()).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public ItemDto add(long userId, ItemDto itemDto) {
        userRepo.findById(userId);
        return ItemMapper.toItemDto(repo.add(userId, ItemMapper.toItem(itemDto)));
    }

    @Override
    public ItemDto patch(long userId, long itemId, ItemDto itemDto) {
        Item item = checkOwner(userId, itemId);
        if (itemDto.getName() != null) {
            if (itemDto.getName().isBlank()) {
                throw new ValidationException("Название не может быть пустым");
            }
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            if (itemDto.getDescription().isBlank()) {
                throw new ValidationException("Описание не может быть пустым");
            }
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        return ItemMapper.toItemDto(repo.update(item));
    }

    @Override
    public void delete(long userId, long itemId) {
        checkOwner(userId, itemId);
        repo.delete(userId, itemId);
    }

    private Item checkOwner(long userId, long itemId) {
        userRepo.findById(userId);
        Item item = repo.findById(itemId);
        long ownerId = item.getOwner().getId();
        if (ownerId != userId) {
            log.warn("Пользователь с id {} не владеет вещью с id {}", userId, itemId);
            throw new NotFoundException(String.format("Пользователь с id %d не владеет вещью с id %d", userId, itemId));
        }
        return item;
    }
}
