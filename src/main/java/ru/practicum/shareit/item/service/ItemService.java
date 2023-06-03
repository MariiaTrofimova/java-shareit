package ru.practicum.shareit.item.service;

import ru.practicum.shareit.item.model.Item;

import java.util.List;
import java.util.Map;

public interface ItemService {
    List<Item> findAllByUserId(long userId);

    Item findById(long itemId);

    List<Item> findByText(String text);

    Item add(long userId, Item item);

    Item patch(long userId, long itemId, Map<String, String> updates);

    void delete(long userId, long itemId);
}
