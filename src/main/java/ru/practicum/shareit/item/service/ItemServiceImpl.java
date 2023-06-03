package ru.practicum.shareit.item.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.*;

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
    public List<Item> findAllByUserId(long userId) {
        userRepo.findById(userId);
        return repo.findAllByUserId(userId);
    }

    @Override
    public Item findById(long itemId) {
        return repo.findById(itemId);
    }

    @Override
    public List<Item> findByText(String text) {
        if (text.isBlank()) {
            return Collections.EMPTY_LIST;
        }
        return repo.findByText(text.toLowerCase());
    }

    @Override
    public Item add(long userId, Item item) {
        userRepo.findById(userId);
        return repo.add(userId, item);
    }

    @Override
    public Item patch(long userId, long itemId, Map<String, String> updates) {
        userRepo.findById(userId);
        Item item = repo.findById(itemId);
        long ownerId = repo.findOwner(itemId);
        if (ownerId != userId) {
            log.warn("Пользователь с id {} не владеет вещью с id {}", userId, itemId);
            throw new NotFoundException(String.format("Пользователь с id %d не владеет вещью с id %d", userId, itemId));
        }
        Collection<String> paramsToUpdate = new HashSet<>(updates.keySet());
        if (paramsToUpdate.contains("name")) {
            item.setName(updates.get("name"));
        }
        if (paramsToUpdate.contains("description")) {
            item.setDescription(updates.get("description"));
        }
        if (paramsToUpdate.contains("available")) {
            item.setAvailable(Boolean.parseBoolean(updates.get("available")));
        }
        return repo.update(item);
    }

    @Override
    public void delete(long userId, long itemId) {
        userRepo.findById(userId);
        repo.findById(itemId);
        long ownerId = repo.findOwner(itemId);
        if (ownerId != userId) {
            log.warn("Пользователь с id {} не владеет вещью с id {}", userId, itemId);
            throw new NotFoundException(String.format("Пользователь с id %d не владеет вещью с id %d", userId, itemId));
        }
        repo.delete(userId, itemId);
    }
}
