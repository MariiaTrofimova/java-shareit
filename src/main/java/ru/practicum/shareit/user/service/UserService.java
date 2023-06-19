package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> findAll();

    UserDto findById(long id);

    UserDto add(UserDto userDto);

    UserDto patch(long id, UserDto userDto);

    void delete(long id);

}
