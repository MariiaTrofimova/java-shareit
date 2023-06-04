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
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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

    private Item item;
    private ItemDto itemDto;
    private Item.ItemBuilder itemBuilder;
    private ItemDto.ItemDtoBuilder itemDtoBuilder;
    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setupBuilder() {
        itemBuilder = Item.builder()
                .name("name")
                .description("description")
                .available(true);
        itemDtoBuilder = ItemDto.builder()
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
                itemBuilder.id(1L).build()));
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
        item = itemBuilder.id(1L).build();
        itemDto = itemDtoBuilder.id(1L).build();
        String json = mapper.writeValueAsString(itemDto);

        when(service.findById(1)).thenReturn(item);
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
                itemBuilder.id(1L).name("Отвертка").build()));
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
        Item itemAdded = itemBuilder.id(1L).build();
        ItemDto itemDtoAdded = itemDtoBuilder.id(1L).build();

        String json = mapper.writeValueAsString(itemDto);
        String jsonAdded = mapper.writeValueAsString(itemDtoAdded);

        when(service.add(userId, ItemMapper.toItem(itemDto))).thenReturn(itemAdded);
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
                .andExpect(content()
                        .json("{\"validationErrors\":{\"name\":\"Название не может быть пустым\"}}"));

        //fail empty description
        itemDto = itemDtoBuilder.name("name").description("").build();
        json = mapper.writeValueAsString(itemDto);
        this.mockMvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json("{\"validationErrors\":{\"description\":\"Описание не может быть пустым\"}}"));

        //fail header absence
        this.mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldPatch() throws Exception {
        Map<String, String> patchParam = new HashMap<>();

        //patch name
        patchParam.put("name", "namePatched");
        String json = "{\"name\": \"namePatched\"}";
        item = itemBuilder.id(1L).name("namePatched").build();
        itemDto = itemDtoBuilder.id(1L).name("namePatched").build();
        String jsonPatched = mapper.writeValueAsString(itemDto);
        when(service.patch(1L, 1L, patchParam)).thenReturn(item);
        this.mockMvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonPatched));

        //patch description
        patchParam.put("description", "descriptionPatched");
        patchParam.remove("name");
        json = "{\"description\": \"descriptionPatched\"}";
        item = itemBuilder.description("descriptionPatched").build();
        itemDto = itemDtoBuilder.description("descriptionPatched").build();
        jsonPatched = mapper.writeValueAsString(itemDto);
        when(service.patch(1L, 1L, patchParam)).thenReturn(item);
        this.mockMvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonPatched));

        //patch available
        patchParam.put("available", "false");
        patchParam.remove("description");
        json = "{\"available\": \"false\"}";
        item = itemBuilder.available(false).build();
        itemDto = itemDtoBuilder.available(false).build();
        jsonPatched = mapper.writeValueAsString(itemDto);
        when(service.patch(1L, 1L, patchParam)).thenReturn(item);
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