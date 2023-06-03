package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.model.User;

import java.util.List;
import java.util.Map;

public interface UserService {
    List<User> findAll();

    User findById(long id);

    User add(User user);

    User patch(long id, Map<String, String> updates);

    boolean delete(long id);

}
