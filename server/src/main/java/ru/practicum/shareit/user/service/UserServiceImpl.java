package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.EmailExistException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository repository;

    @Override
    public List<UserDto> findAll() {
        List<User> users = repository.findAll();
        return UserMapper.toUserDto(users);
    }

    @Override
    public UserDto findById(long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", id)));
        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto add(UserDto userDto) {
        User user = UserMapper.toUser(userDto);
        User userAdded;
        try {
            userAdded = repository.save(user);
        } catch (RuntimeException e) {
            String error = e.getMessage();
            String constraint = "uq_user_email";
            if (error.contains(constraint)) {
                error = String.format("Пользователь с email %s уже существует", userDto.getEmail());
                throw new EmailExistException(error);
            }
            throw new RuntimeException("Ошибка при передаче данных в БД");
        }
        return UserMapper.toUserDto(userAdded);
    }

    @Override
    public UserDto patch(long id, UserDto userDto) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", id)));
        String newName = userDto.getName();
        String newEmail = userDto.getEmail();

        if (newName != null) {
            user.setName(newName);
        }
        if (newEmail != null) {
            user.setEmail(newEmail);
        }
        user = repository.save(user);
        return UserMapper.toUserDto(user);
    }

    @Override
    public void delete(long id) {
        repository.deleteById(id);
    }
}