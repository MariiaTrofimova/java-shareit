package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.error.exception.EmailExistException;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.user.UserMapper;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserServiceImpl service;

    private User user;

    @BeforeEach
    void setup() {
        user = new User();
        user.setName("name");
        user.setEmail("e@mail.ru");
        user.setId(1L);
    }

    @Test
    void findAll() {
        //Empty List
        when(repository.findAll()).thenReturn(Collections.emptyList());
        List<UserDto> users = service.findAll();
        assertNotNull(users);
        assertEquals(0, users.size());

        //Single List
        when(repository.findAll()).thenReturn(List.of(user));
        users = service.findAll();
        assertNotNull(users);
        assertEquals(1, users.size());
    }

    @Test
    void findById() {
        //Not Found
        long userIdNotFound = 0L;
        String error = String.format("Пользователь с id %d не найден", userIdNotFound);
        when(repository.findById(userIdNotFound)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.findById(userIdNotFound));
        assertEquals(error, exception.getMessage());

        //Regular case
        long userId = user.getId();
        when(repository.findById(userId)).thenReturn(Optional.of(user));
        UserDto userFound = service.findById(userId);
        assertNotNull(userFound);
        assertEquals(userId, userFound.getId());
    }

    @Test
    void add() {
        User userToSave = new User();
        userToSave.setName("name");
        userToSave.setEmail("e@mail.ru");

        when(repository.save(any())).thenReturn(user);
        UserDto userDto = UserMapper.toUserDto(userToSave);
        UserDto userSaved = service.add(userDto);

        assertNotNull(userSaved);
        assertEquals(user.getId(), userSaved.getId());
        verify(repository, times(1)).save(any());

        //Fail By Existing Email
        String email = user.getEmail();
        String error = String.format("Пользователь с email %s уже существует", email);
        when(repository.save(any())).thenThrow(new RuntimeException("uq_user_email"));
        EmailExistException exception = assertThrows(
                EmailExistException.class,
                () -> service.add(userDto)
        );
        assertEquals(error, exception.getMessage());
    }

    @Test
    void patch() {
        //Fail By Not Found User
        long userIdNotFound = 0L;
        String error = String.format("Пользователь с id %d не найден", userIdNotFound);
        when(repository.findById(userIdNotFound)).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.findById(userIdNotFound));
        assertEquals(error, exception.getMessage());

        //Update Name
        long userId = user.getId();
        String nameUpdated = "nameUpdated";
        User userUpdated = new User();
        userUpdated.setId(userId);
        userUpdated.setName(nameUpdated);
        userUpdated.setEmail(user.getEmail());
        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(any())).thenReturn(userUpdated);
        UserDto userDtoUpdated = service.patch(userId, UserDto.builder().name(nameUpdated).build());

        assertNotNull(userDtoUpdated);
        assertEquals(userId, userDtoUpdated.getId());
        assertEquals(nameUpdated, userDtoUpdated.getName());

        //Update Email
        String emailUpdated = "updated@mail.ru";
        userUpdated.setEmail(emailUpdated);
        when(repository.save(any())).thenReturn(userUpdated);
        userDtoUpdated = service.patch(userId, UserDto.builder().email(emailUpdated).build());

        assertNotNull(userDtoUpdated);
        assertEquals(userId, userDtoUpdated.getId());
        assertEquals(emailUpdated, userDtoUpdated.getEmail());
    }

    @Test
    void delete() {
        long userId = 1L;
        service.delete(userId);
        verify(repository, times(1)).deleteById(userId);
    }
}