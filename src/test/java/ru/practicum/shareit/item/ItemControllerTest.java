package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingCommentsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
class ItemControllerTest {
    private static final String URL = "/items";

    @Autowired
    ObjectMapper mapper;

    @MockBean
    ItemService service;

    @Autowired
    private MockMvc mvc;

    private ItemDto itemDto;
    private ItemDto.ItemDtoBuilder itemDtoBuilder;
    private ItemBookingCommentsDto.ItemBookingCommentsDtoBuilder itemBookingCommentsDtoBuilder;
    private CommentDto.CommentDtoBuilder commentDtoBuilder;

    @BeforeEach
    void setupBuilder() {
        itemDtoBuilder = ItemDto.builder()
                .name("name")
                .description("description")
                .available(true);
        itemBookingCommentsDtoBuilder = ItemBookingCommentsDto.builder()
                .name("name")
                .description("description")
                .available(true);
        commentDtoBuilder = CommentDto.builder()
                .text("comment");
    }

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mvc);
    }

    @Test
    void shouldFindAllByUserId() throws Exception {
        // Empty List
        when(service.findAllByUserId(1L, 0, Optional.empty())).thenReturn(new ArrayList<>());
        mvc
                .perform(get(URL)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Single List
        ItemBookingCommentsDto itemDto = itemBookingCommentsDtoBuilder.id(1L).build();
        when(service.findAllByUserId(1L, 0, Optional.of(1))).thenReturn(List.of(itemDto));
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1)
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName()), String.class));

        // Header absence
        String error = "X-Sharer-User-Id";
        mvc.perform(get(URL))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", containsString(error)));
    }

    @Test
    void shouldFindById() throws Exception {
        //regular case
        ItemBookingCommentsDto itemBookingCommentsDto = itemBookingCommentsDtoBuilder.id(1L).build();
        String json = mapper.writeValueAsString(itemBookingCommentsDto);

        when(service.findById(1, 1)).thenReturn(itemBookingCommentsDto);
        mvc.perform(get(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        //user not found
        String error = "Вещь с id 1 не найдена";
        when(service.findById(1, 1)).thenThrow(new NotFoundException(error));
        mvc.perform(get(URL + "/1")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(error)));
    }

    @Test
    void shouldFindByText() throws Exception {
        // Empty List
        when(service.findByText("", 0, Optional.empty())).thenReturn(new ArrayList<>());
        mvc
                .perform(get(URL + "/search")
                        .param("text", ""))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Single List
        ItemDto itemDto = itemDtoBuilder.id(1L).name("Отвертка").build();
        when(service.findByText("ОтВ", 0, Optional.of(1))).thenReturn(List.of(itemDto));
        mvc.perform(get(URL + "/search")
                        .param("text", "ОтВ")
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(itemDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].name", is(itemDto.getName()), String.class));
    }

    @Test
    void shouldAdd() throws Exception {
        //addRegular
        long userId = 1L;
        itemDto = itemDtoBuilder.build();
        ItemDto itemDtoAdded = itemDtoBuilder.id(1L).build();

        String json = mapper.writeValueAsString(itemDto);
        String jsonAdded = mapper.writeValueAsString(itemDtoAdded);

        when(service.add(userId, itemDto)).thenReturn(itemDtoAdded);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(jsonAdded));

        //fail name
        itemDto = itemDtoBuilder.name("").build();
        json = mapper.writeValueAsString(itemDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Название не может быть пустым")));


        //fail empty description
        itemDto = itemDtoBuilder.name("name").description("").build();
        json = mapper.writeValueAsString(itemDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Описание не может быть пустым")));

        //fail header absence
        mvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPatch() throws Exception {
        //patch name
        String json = "{\"name\": \"namePatched\"}";
        itemDto = ItemDto.builder().name("namePatched").build();
        ItemDto itemDtoPatched = itemDtoBuilder.id(1L).name("namePatched").build();
        String jsonPatched = mapper.writeValueAsString(itemDtoPatched);
        System.out.println(jsonPatched);
        when(service.patch(1L, 1L, itemDto)).thenReturn(itemDtoPatched);
        mvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonPatched));

        //patch description
        json = "{\"description\": \"descriptionPatched\"}";
        itemDto = ItemDto.builder().description("descriptionPatched").build();
        itemDtoPatched = itemDtoBuilder.description("descriptionPatched").build();
        jsonPatched = mapper.writeValueAsString(itemDtoPatched);
        when(service.patch(1L, 1L, itemDto)).thenReturn(itemDtoPatched);
        mvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonPatched));

        //patch available
        json = "{\"available\": \"false\"}";
        itemDto = ItemDto.builder().available(false).build();
        itemDtoPatched = itemDtoBuilder.available(false).build();
        jsonPatched = mapper.writeValueAsString(itemDto);
        when(service.patch(1L, 1L, itemDto)).thenReturn(itemDtoPatched);
        mvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonPatched));
    }

    @Test
    void shouldDeleteItem() throws Exception {
        mvc.perform(delete(URL + "/1")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    void shouldAddComment() throws Exception {
        //Regular Case
        CommentDto commentDto = commentDtoBuilder.build();
        String jsonIn = mapper.writeValueAsString(commentDto);
        CommentDto commentDtoOut = commentDtoBuilder
                .id(1L)
                .authorName("name")
                .created(LocalDateTime.now())
                .build();
        String json = mapper.writeValueAsString(commentDtoOut);
        when(service.addComment(1L, 1L, commentDto)).thenReturn(commentDtoOut);
        mvc.perform(post(URL + "/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonIn))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        //Fail By Empty Text
        commentDto.setText(null);
        jsonIn = mapper.writeValueAsString(commentDto);
        String error = "Текст комментария не может быть пустым";
        mvc.perform(post(URL + "/1/comment")
                        .header("X-Sharer-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonIn))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.text", containsString(error)));
    }
}