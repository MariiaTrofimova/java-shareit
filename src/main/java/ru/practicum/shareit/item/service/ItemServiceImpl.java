package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.ItemMapper;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ItemServiceImpl implements ItemService {
    private final ItemRepository repository;
    private final UserRepository userRepo;

    public ItemServiceImpl(ItemRepository repository, UserRepository userRepo) {
        this.repository = repository;
        this.userRepo = userRepo;
    }

    @Override
    public List<ItemDto> findAllByUserId(long userId) {
        userRepo.findById(userId);
        return repository.findByOwnerId(userId).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public ItemDto findById(long itemId) {
        Item item = repository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с id %d не найдена", itemId)));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemDto> findByText(String text) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        return repository.findByText(text).stream()
                .map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Override
    public ItemDto add(long userId, ItemDto itemDto) {
        User owner = userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
        Item item = ItemMapper.toItem(itemDto);
        item.setOwner(owner);
        return ItemMapper.toItemDto(repository.save(item));
    }

    @Override
    public ItemDto patch(long userId, long itemId, ItemDto itemDto) {
        Item item = checkOwner(userId, itemId);
        String newName = itemDto.getName();
        String newDescription = itemDto.getDescription();
        Boolean newAvailable = itemDto.getAvailable();

        if (newName != null) {
            checkNotBlank(newName, "Название");
            item.setName(newName);
        }
        if (newDescription != null) {
            checkNotBlank(newDescription, "Описание");
            item.setDescription(newDescription);
        }
        if (newAvailable != null) {
            item.setAvailable(newAvailable);
        }
        item = repository.update(itemId, item.getName(), item.getDescription(), item.isAvailable());
        return ItemMapper.toItemDto(item);
    }

    @Override
    public void delete(long userId, long itemId) {
        checkOwner(userId, itemId);
        repository.deleteById(itemId);
    }

    private Item checkOwner(long userId, long itemId) {
        userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
        Item item = repository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с id %d не найдена", itemId)));
        long ownerId = item.getOwner().getId();
        if (ownerId != userId) {
            log.warn("Пользователь с id {} не владеет вещью с id {}", userId, itemId);
            throw new NotFoundException(String.format("Пользователь с id %d не владеет вещью с id %d", userId, itemId));
        }
        return item;
    }

    private void checkNotBlank(String s, String parameterName) {
        if (s.isBlank()) {
            log.warn("{} не может быть пустым", parameterName);
            throw new ValidationException(String.format("%s не может быть пустым", parameterName));
        }
    }
}
