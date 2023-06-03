package ru.practicum.shareit.user.model;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.model.Item;

import java.util.HashSet;
import java.util.Set;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
public class User {
    private Long id;
    private String name;
    private String email;
    private final Set<Item> items = new HashSet<>();
}
