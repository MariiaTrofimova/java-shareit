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
    private final Set<String> emails = new HashSet<>();
    private long nextId = 1;

    @Override
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    @Override
    public User findById(long id) {
        if (!users.containsKey(id)) {
            throw new NotFoundException(String.format("Пользователь с id %d не найден", id));
        }
        return users.get(id);
    }

    @Override
    public User add(User user) {
        long userId = nextId++;
        user.setId(userId);
        users.put(userId, user);
        emails.add(user.getEmail());
        log.info("Добавлен пользователь с id {}", userId);
        return user;
    }

    @Override
    public User update(User user) {
        //если емейл не совпадает, проверить, не содержися ли, если нет, обновить, удалить старый
        long id = user.getId();
        String email = user.getEmail();
        String oldEmail = users.get(id).getEmail();
        if (!email.equals(oldEmail)) {
            emails.remove(oldEmail);
            emails.add(email);
        }
        users.put(id, user);
        log.info("Обновлен пользователь с id {}", user.getId());
        return user;
    }

    @Override
    public boolean delete(long id) {
        if (users.containsKey(id)) {
            emails.remove(users.get(id).getEmail());
            users.remove(id);
            log.info("Удален пользователь с id {}", id);
            return true;
        }
        return false;
    }

    @Override
    public boolean isEmailExist(String email) {
        return emails.contains(email);
    }

}
