package ru.practicum.shareit.user;

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
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.service.UserService;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {
    private static final String URL = "/users";

    @MockBean
    private final UserService service;

    @Autowired
    private MockMvc mockMvc;

    private UserDto userDto;
    private UserDto.UserDtoBuilder userDtoBuilder;

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setupBuilder() {
        userDtoBuilder = UserDto.builder()
                .name("name")
                .email("e@mail.ru");
    }

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mockMvc);
    }

    @Test
    void shouldFindAll() throws Exception {
        // Empty List
        when(service.findAll()).thenReturn(new ArrayList<>());
        this.mockMvc
                .perform(get(URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Single List
        when(service.findAll()).thenReturn(List.of(
                userDtoBuilder.id(1L).build()));
        mockMvc.perform(get(URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void shouldFindById() throws Exception {
        //regular case
        userDto = userDtoBuilder.id(1L).build();
        String json = mapper.writeValueAsString(userDto);

        when(service.findById(1)).thenReturn(userDto);
        mockMvc.perform(get(URL + "/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(json));

        //user not found
        when(service.findById(1)).thenThrow(new NotFoundException("Пользователь с id 1 не найден"));
        mockMvc.perform(get(URL + "/1"))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().json("{\"error\":\"Пользователь с id 1 не найден\"}"));
    }

    @Test
    void shouldAdd() throws Exception {
        //addRegular
        userDto = userDtoBuilder.build();
        UserDto userDtoAdded = userDtoBuilder.id(1L).build();

        String json = mapper.writeValueAsString(userDto);
        String jsonAdded = mapper.writeValueAsString(userDtoAdded);

        when(service.add(userDto)).thenReturn(userDtoAdded);
        this.mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(content().json(jsonAdded));

        //fail name
        userDto = userDtoBuilder.name("").build();
        json = mapper.writeValueAsString(userDto);
        this.mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Имя не может быть пустым")));
        //.andExpect(content().json("{\"validationErrors\":{\"name\":\"Имя не может быть пустым\"}}"));

        //fail empty email
        userDto = userDtoBuilder.name("name").email("").build();
        json = mapper.writeValueAsString(userDto);
        this.mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("E-mail не может быть пустым")));

        //fail некорректный email
        userDto = userDtoBuilder.email("email").build();
        json = mapper.writeValueAsString(userDto);
        this.mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"validationErrors\":{\"email\":\"Введен некорректный e-mail\"}}"));
    }

    @Test
    void shouldPatch() throws Exception {

        //patch name
        String json = "{\"name\": \"namePatched\"}";
        userDto = UserDto.builder().name("namePatched").build();
        UserDto userDtoUpdated = userDtoBuilder.id(1L).name("namePatched").build();
        String jsonPatched = mapper.writeValueAsString(userDtoUpdated);
        when(service.patch(1L, userDto)).thenReturn(userDtoUpdated);
        this.mockMvc.perform(patch(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(jsonPatched));

        //patch email
        json = "{\"email\": \"patched@mail.ru\"}";
        userDto = UserDto.builder().email("patched@mail.ru").build();
        userDtoUpdated = userDtoBuilder.email("patched@mail.ru").build();
        jsonPatched = mapper.writeValueAsString(userDtoUpdated);
        when(service.patch(1L, userDto)).thenReturn(userDtoUpdated);
        this.mockMvc.perform(patch(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonPatched));

        //fail некорректный email
        json = "{\"email\": \"patched\"}";
        this.mockMvc.perform(patch(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content()
                        .json("{\"validationErrors\":{\"email\":\"Введен некорректный e-mail\"}}"));

    }

    @Test
    void shouldDelete() throws Exception {
        this.mockMvc.perform(delete(URL + "/1"))
                .andDo(print())
                .andExpect(status().isOk());

    }
}