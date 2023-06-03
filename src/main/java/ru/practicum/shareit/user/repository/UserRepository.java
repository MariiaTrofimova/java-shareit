package ru.practicum.shareit.user.repository;

import ru.practicum.shareit.user.model.User;

import java.util.List;

public interface UserRepository {
    List<User> findAll();

    User findById(long id);

    User add(User user);

    User update(User user);

    boolean delete(long id);

    boolean isEmailExist(String email);

    void deleteEmail(String oldEmail);
}
