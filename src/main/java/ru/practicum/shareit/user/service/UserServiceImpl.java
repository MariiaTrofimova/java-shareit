package ru.practicum.shareit.user.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.validation.Validation;

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
        User userAdded = repository.save(user);
        return UserMapper.toUserDto(userAdded);
    }

    @Override
    public UserDto patch(long id, UserDto userDto) {
        User user = repository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", id)));
        String newName = userDto.getName();
        String newEmail = userDto.getEmail();

        if (newName != null) {
            Validation.checkNotBlank(newName, "Имя");
            user.setName(newName);
        }
        if (newEmail != null) {
            Validation.checkNotBlank(newEmail, "Email");
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