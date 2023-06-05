package ru.practicum.shareit.user.service;

import org.springframework.stereotype.Service;
import ru.practicum.shareit.error.exception.EmailExistException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.validation.ValidationException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repo;

    public UserServiceImpl(UserRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<UserDto> findAll() {
        return repo.findAll().stream()
                .map(UserMapper::toUserDto).collect(Collectors.toList());
    }

    @Override
    public UserDto findById(long id) {
        return UserMapper.toUserDto(repo.findById(id));
    }

    @Override
    public UserDto add(UserDto userDto) {
        String email = userDto.getEmail();
        if (!repo.isEmailExist(email)) {
            return UserMapper.toUserDto(repo.add(UserMapper.toUser(userDto)));
        } else {
            throw new EmailExistException(String.format("Пользователь с email %s уже существует", email));
        }
    }

    @Override
    public UserDto patch(long id, UserDto userDto) {
        User user = repo.findById(id);
        if (userDto.getName() != null) {
            if (userDto.getName().isBlank()) {
                throw new ValidationException("Имя не может быть пустым");
            }
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null) {
            if (userDto.getEmail().isBlank()) {
                throw new ValidationException("E-mail не может быть пустым");
            }
            String oldEmail = user.getEmail();
            String email = userDto.getEmail();
            if (!email.equals(oldEmail) && repo.isEmailExist(email)) {
                throw new EmailExistException(String.format("Пользователь с email %s уже существует", email));
            }
            user.setEmail(userDto.getEmail());
        }
        return UserMapper.toUserDto(repo.update(user));
    }

    @Override
    public boolean delete(long id) {
        return repo.delete(id);
    }
}
