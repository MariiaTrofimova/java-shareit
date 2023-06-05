package ru.practicum.shareit.item.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository("ItemRepositoryInMemory")
@Slf4j
public class ItemRepositoryInMemoryImpl implements ItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private final Map<Long, List<Long>> usersItems = new HashMap<>();
    private long nextIndex = 1;

    @Override
    public List<Item> findAllByUserId(long userId) {
        List<Long> itemsId = usersItems.getOrDefault(userId, new ArrayList<>());
        return itemsId.stream()
                .map(items::get)
                .collect(Collectors.toList());
    }

    @Override
    public Item findById(long itemId) {
        if (!items.containsKey(itemId)) {
            throw new NotFoundException(String.format("Вещь с id %d не найдена", itemId));
        }
        return items.get(itemId);
    }

    @Override
    public List<Item> findByText(String text) {
        return items.values().stream()
                .filter(item -> item.isAvailable()
                        && (item.getName().toLowerCase().contains(text)
                        || item.getDescription().toLowerCase().contains(text)))
                .collect(Collectors.toList());
    }

    @Override
    public Item add(long userId, Item item) {
        long itemId = nextIndex++;
        item.setId(itemId);
        User owner = User.builder().id(userId).build();
        item.setOwner(owner);
        items.put(itemId, item);
        usersItems.computeIfAbsent(userId, k -> new ArrayList<>()).add(itemId);
        log.info("Добавлена вещь с id {}", itemId);
        return item;
    }

    @Override
    public Item update(Item item) {
        items.put(item.getId(), item);
        log.info("Обновлена вещь с id {}", item.getId());
        return item;
    }

    @Override
    public void delete(long userId, Long itemId) {
        usersItems.get(userId).remove(itemId);
        items.remove(itemId);
        log.info("Удалена вещь с id {}", itemId);
    }
}
