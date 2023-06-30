package ru.practicum.shareit.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.service.ItemRequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ItemRequestController.class)
class ItemRequestControllerTest {
    private static final String URL = "/requests";

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemRequestService service;

    @Autowired
    private MockMvc mvc;

    private ItemRequestDto request;
    private ItemRequestDto.ItemRequestDtoBuilder builder;

    @BeforeEach
    void setupBuilder() {
        builder = ItemRequestDto.builder()
                .id(1L)
                .description("Нужен мужчина с перфоратором")
                .created(LocalDateTime.now());

    }

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mvc);
    }

    @Test
    void shouldAddRequest() throws Exception {
        //Fail By Empty Description
        ItemRequestDto requestIn = ItemRequestDto.builder()
                .build();
        String json = mapper.writeValueAsString(requestIn);
        String error = "Описание не может быть пустым";
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));

        //Regular case
        requestIn.setDescription("Нужен мужчина с перфоратором");
        request = builder.build();
        json = mapper.writeValueAsString(requestIn);
        when(service.add(1L, requestIn)).thenReturn(request);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(request.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(request.getDescription()), String.class));

        // Wrong user
        error = String.format("Пользователь с id %d не найден", -1);
        when(service.add(-1L, requestIn)).thenThrow(new NotFoundException(error));
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", -1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(error), String.class));
    }

    @Test
    void shouldFindAllByUserId() throws Exception {
        //Empty List
        when(service.findAllByUserId(2L)).thenReturn(new ArrayList<>());
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 2))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        //Single List
        request = builder.build();
        when(service.findAllByUserId(1L)).thenReturn(List.of(request));
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(request.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(request.getDescription()), String.class));

        //Fail By UserId
        String error = String.format("Пользователь с id %d не найден", -1);
        when(service.findAllByUserId(-1L)).thenThrow(new NotFoundException(error));
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", -1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(error), String.class));
    }

    @Test
    void shouldFindAll() throws Exception {
        //Empty List
        when(service.findAll(1L, 0, Optional.empty())).thenReturn(new ArrayList<>());
        mvc.perform(get(URL + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        //Single List
        request = builder.build();
        when(service.findAll(1L, 0, Optional.of(1))).thenReturn(List.of(request));
        mvc.perform(get(URL + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(request.getId()), Long.class))
                .andExpect(jsonPath("$[0].description", is(request.getDescription()), String.class));

        //Fail By UserId
        String error = String.format("Пользователь с id %d не найден", -1);
        when(service.findAll(-1L, 0, Optional.of(1))).thenThrow(new NotFoundException(error));
        mvc.perform(get(URL + "/all")
                        .header("X-Sharer-User-Id", -1)
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(error), String.class));

        //Fail By From
        error = "Индекс первого элемента не может быть отрицательным";
        mvc.perform(get(URL + "/all")
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "-1")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error), String.class));
    }

    @Test
    void shouldFindById() throws Exception {
        //Regular Case
        request = builder.build();
        when(service.findById(1L, 1L)).thenReturn(request);
        mvc.perform(get(URL + "/1")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(request.getId()), Long.class))
                .andExpect(jsonPath("$.description", is(request.getDescription()), String.class));

        //Fail By UserId
        String error = String.format("Пользователь с id %d не найден", -1);
        when(service.findById(-1L, 1L)).thenThrow(new NotFoundException(error));
        mvc.perform(get(URL + "/1")
                        .header("X-Sharer-User-Id", -1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(error), String.class));

        //Fail By RequestId
        error = String.format("Запрос с id %d не найден", 99);
        when(service.findById(1L, 99L)).thenThrow(new NotFoundException(error));
        mvc.perform(get(URL + "/99")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(error), String.class));
    }
}