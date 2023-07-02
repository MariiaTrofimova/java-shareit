package ru.practicum.shareit.user.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserServiceImpl service;

    @Test
    void findAll() {
        //Empty List
        when(repository.findAll()).thenReturn(Collections.emptyList());
        List<UserDto> users = service.findAll();
        assertNotNull(users);
    }

    @Test
    void findById() {
    }

    @Test
    void add() {
    }

    @Test
    void patch() {
    }

    @Test
    void delete() {
    }
}