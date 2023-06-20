package ru.practicum.shareit.item;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemBookingDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.service.ItemService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemControllerTest {
    private static final String URL = "/items";

    @MockBean
    private final ItemService service;

    @Autowired
    private MockMvc mockMvc;

    private ItemDto itemDto;
    private ItemDto.ItemDtoBuilder itemDtoBuilder;

    private ItemBookingDto.ItemBookingDtoBuilder itemBookingDtoBuilder;
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setupBuilder() {
        itemDtoBuilder = ItemDto.builder()
                .name("name")
                .description("description")
                .available(true);
        itemBookingDtoBuilder = ItemBookingDto.builder()
                .name("name")
                .description("description")
                .available(true);
    }

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mockMvc);
    }

    @Test
    void shouldFindAllByUserId() throws Exception {
        // Empty List
        when(service.findAllByUserId(1L)).thenReturn(new ArrayList<>());
        this.mockMvc
                .perform(get(URL)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Single List
        when(service.findAllByUserId(1L)).thenReturn(List.of(
                itemBookingDtoBuilder.id(1L).build()));
        mockMvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        // Header absence
        mockMvc.perform(get(URL))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldFindById() throws Exception {
        //regular case
        itemDto = itemDtoBuilder.id(1L).build();
        String json = mapper.writeValueAsString(itemDto);

        when(service.findById(1)).thenReturn(itemDto);
        mockMvc.perform(get(URL + "/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        //user not found
        when(service.findById(1)).thenThrow(new NotFoundException("Вещь с id 1 не найдена"));
        mockMvc.perform(get(URL + "/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Вещь с id 1 не найдена\"}"));
    }

    @Test
    void shouldFindByText() throws Exception {
        // Empty List
        when(service.findByText("")).thenReturn(new ArrayList<>());
        this.mockMvc
                .perform(get(URL + "/search?text="))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Single List
        when(service.findByText("ОтВ")).thenReturn(List.of(
                itemDtoBuilder.id(1L).name("Отвертка").build()));
        mockMvc.perform(get(URL + "/search?text=ОтВ"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
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
        this.mockMvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(jsonAdded));

        //fail name
        itemDto = itemDtoBuilder.name("").build();
        json = mapper.writeValueAsString(itemDto);
        this.mockMvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Название не может быть пустым")));


        //fail empty description
        itemDto = itemDtoBuilder.name("name").description("").build();
        json = mapper.writeValueAsString(itemDto);
        this.mockMvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Описание не может быть пустым")));

        //fail header absence
        this.mockMvc.perform(post(URL)
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
        this.mockMvc.perform(patch(URL + "/1")
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
        this.mockMvc.perform(patch(URL + "/1")
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
        this.mockMvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonPatched));
    }

    @Test
    void shouldDeleteItem() throws Exception {
        this.mockMvc.perform(delete(URL + "/1")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk());
    }
}