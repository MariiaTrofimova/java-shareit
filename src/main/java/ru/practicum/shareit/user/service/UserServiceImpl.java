package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.EmailExistException;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class UserServiceImpl implements UserService {
    private static final String EMAIL_PATTERN = "\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*\\.\\w{2,4}";
    private final UserRepository repo;

    public UserServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<User> findAll() {
        return repo.findAll();
    }

    @Override
    public User findById(long id) {
        return repo.findById(id);
    }

    @Override
    public User add(User user) {
        String email = user.getEmail();
        if (!repo.isEmailExist(email)) {
            return repo.add(user);
        } else {
            throw new EmailExistException(String.format("Пользователь с email %s уже существует", email));
        }
    }

    @Override
    public User patch(long id, Map<String, String> updates) {
        User user = repo.findById(id);
        Collection<String> paramsToUpdate = new HashSet<>(updates.keySet());
        if (paramsToUpdate.contains("name")) {
            user.setName(updates.get("name"));
        }
        if (paramsToUpdate.contains("email")) {
            String email = updates.get("email");
            if (Pattern.matches(EMAIL_PATTERN, email)) {
                String oldEmail = user.getEmail();
                if (!oldEmail.equals(email) && repo.isEmailExist(email)) {
                    throw new EmailExistException(String.format("Пользователь с email %s уже существует", email));
                }
                repo.deleteEmail(oldEmail);
                user.setEmail(email);
            } else {
                throw new ValidationException(String.format("Email %s некорректный", email));
            }
        }
        return repo.update(user);
    }

    @Override
    public boolean delete(long id) {
        return repo.delete(id);
    }
}
