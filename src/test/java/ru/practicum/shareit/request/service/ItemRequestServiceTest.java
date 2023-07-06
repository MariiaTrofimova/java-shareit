package ru.practicum.shareit.request.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestNewDto;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemRequestServiceTest {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    @Mock
    private ItemRequestRepository repository;

    @Mock
    private UserRepository userRepo;

    @Mock
    private ItemRepository itemRepo;

    @InjectMocks
    ItemRequestServiceImpl service;

    private User requestor;
    private User owner;
    private Item item;
    private ItemRequest request;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setName("name");
        owner.setEmail("e@mail.ru");
        owner.setId(1L);

        requestor = new User();
        requestor.setName("name2");
        requestor.setEmail("e2@mail.ru");
        requestor.setId(2L);

        request = new ItemRequest();
        request.setDescription("description");
        request.setRequestor(requestor);
        request.setCreated(Instant.now());
        request.setId(1L);

        item = new Item();
        item.setId(1L);
        item.setName("name");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
    }


    @Test
    void add() {
        //Regular case
        when(repository.save(any())).thenReturn(request);
        when(userRepo.findById(requestor.getId())).thenReturn(Optional.of(requestor));
        ItemRequestNewDto requestDto = service.add(requestor.getId(),
                ItemRequestNewDto.builder().description("description").build());
        assertNotNull(requestDto);
        assertEquals(request.getId(), requestDto.getId());
        verify(repository, times(1)).save(any());
    }

    @Test
    void addFailByUserNotFound() {
        //Fail By User Not Found
        long userNotFoundId = 0L;
        String error = String.format("Пользователь с id %d не найден", userNotFoundId);
        when(userRepo.findById(userNotFoundId)).thenThrow(new NotFoundException(error));
        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> service.add(userNotFoundId, ItemRequestNewDto.builder().description("description").build())
        );
        assertEquals(error, exception.getMessage());
        verify(repository, times(0)).save(any());
    }

    @Test
    void findAllByUserId() {
        long userId = requestor.getId();
        when(userRepo.findById(userId)).thenReturn(Optional.of(requestor));
        when(repository.findByRequestorId(userId)).thenReturn(List.of(request));
        List<ItemRequestDto> requests = service.findAllByUserId(userId);
        assertNotNull(requests);
        assertEquals(1, requests.size());
        verify(repository, times(1)).findByRequestorId(userId);
    }

    @Test
    void findAll() {
        //Empty List
        long userId = requestor.getId();
        int from = 0;
        int size = 1;
        PageRequest page = PageRequest.of(from / size, size, SORT);
        when(userRepo.findById(userId)).thenReturn(Optional.of(requestor));
        when(itemRepo.findByRequestIdIn(Collections.emptyList())).thenReturn(Collections.emptyList());
        when(repository.findByRequestorIdNot(userId, page)).thenReturn(Page.empty());
        List<ItemRequestDto> requestDtos = service.findAll(userId, from, size);
        assertNotNull(requestDtos);
        assertEquals(0, requestDtos.size());

        //Single List
        userId = owner.getId();
        long requestId = request.getId();
        when(userRepo.findById(userId)).thenReturn(Optional.of(owner));
        when(itemRepo.findByRequestIdIn(List.of(requestId))).thenReturn(List.of(item));
        when(repository.findByRequestorIdNot(userId, page)).thenReturn(new PageImpl<>(List.of(request)));
        requestDtos = service.findAll(userId, from, size);
        assertNotNull(requestDtos);
        assertEquals(1, requestDtos.size());
    }

    @Test
    void findById() {
        long userId = requestor.getId();
        when(userRepo.findById(userId)).thenReturn(Optional.of(requestor));
        long requestId = request.getId();
        when(repository.findById(requestId)).thenReturn(Optional.of(request));
        when(itemRepo.findByRequestId(requestId)).thenReturn(List.of(item));
        ItemRequestDto requestDto = service.findById(userId, requestId);

        assertNotNull(requestDto);
        assertEquals(requestId, requestDto.getId());
        assertEquals(1, requestDto.getItems().size());
        assertEquals(item.getId(), requestDto.getItems().get(0).getId());

        InOrder inOrder = inOrder(userRepo, repository, itemRepo);
        inOrder.verify(userRepo).findById(userId);
        inOrder.verify(repository).findById(requestId);
        inOrder.verify(itemRepo).findByRequestId(requestId);
    }
}