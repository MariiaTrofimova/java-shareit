package ru.practicum.shareit.item.repository;

import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository {
    List<Item> findAllByUserId(long userId);

    Item findById(long itemId);

    List<Item> findByText(String lowerCase);

    Item add(long userId, Item item);

    long findOwner(long itemId);

    Item update(Item item);

    void delete(long userId, Long itemId);
}
