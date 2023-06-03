package ru.practicum.shareit.user.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.model.User;

import java.util.*;

@Repository("UserRepositoryInMemory")
@Slf4j
public class UserRepositoryInMemoryImpl implements UserRepository {
    private final Map<Long, User> users = new HashMap<>();
    private final Set<String> userEmails = new HashSet<>();
    private long nextId = 1;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(long id) {
        try {
            return users.get(id);
        } catch (NullPointerException e) {
            log.warn("Пользователь с id {} не найден", id);
            throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
        }
    }

    @Override
    public User add(User user) {
        long userId = nextId++;
        user.setId(userId);
        users.put(userId, user);
        userEmails.add(user.getEmail());
        log.info("Добавлен пользователь с id {}", userId);
        return user;
    }

    @Override
    public User update(User user) {
        User userToUpdate = users.get(user.getId());
        String oldEmail = userToUpdate.getEmail();
        users.put(user.getId(), user);
        userEmails.remove(oldEmail);
        userEmails.add(user.getEmail());
        return user;
    }

    @Override
    public boolean delete(long id) {
        if (users.containsKey(id)) {
            userEmails.remove(users.get(id).getEmail());
            users.remove(id);
            return true;
        }
        return false;
    }

    @Override
    public boolean isEmailExist(String email) {
        return userEmails.contains(email);
    }

    @Override
    public void deleteEmail(String oldEmail) {
        userEmails.remove(oldEmail);
    }
}
