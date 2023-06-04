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
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.service.UserService;

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

@WebMvcTest(UserController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserControllerTest {
    private static final String URL = "/users";

    @MockBean
    private final UserService service;

    @Autowired
    private MockMvc mockMvc;

    private User user;
    private UserDto userDto;
    private User.UserBuilder userBuilder;
    private UserDto.UserDtoBuilder userDtoBuilder;

    ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setupBuilder() {
        userBuilder = User.builder()
                .name("name")
                .email("e@mail.ru");
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
                userBuilder.id(1L).build()));
        mockMvc.perform(get(URL))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()", is(1)))
                .andExpect(jsonPath("$[0].id", is(1)));
    }

    @Test
    void shouldFindById() throws Exception {
        //regular case
        user = userBuilder.id(1L).build();
        userDto = userDtoBuilder.id(1L).build();
        String json = mapper.writeValueAsString(userDto);

        when(service.findById(1)).thenReturn(user);
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
        User userAdded = userBuilder.id(1L).build();
        UserDto userDtoAdded = userDtoBuilder.id(1L).build();

        String json = mapper.writeValueAsString(userDto);
        String jsonAdded = mapper.writeValueAsString(userDtoAdded);

        when(service.add(UserMapper.toUser(userDto))).thenReturn(userAdded);
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
                .andExpect(content().json("{\"validationErrors\":{\"name\":\"Имя не может быть пустым\"}}"));

        //fail empty email
        userDto = userDtoBuilder.name("name").email("").build();
        json = mapper.writeValueAsString(userDto);
        this.mockMvc.perform(post(URL)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(content().json("{\"validationErrors\":{\"email\":\"E-mail не может быть пустым\"}}"));

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
        Map<String, String> patchParam = new HashMap<>();
        patchParam.put("name", "namePatched");

        //patch name
        String json = "{\"name\": \"namePatched\"}";
        user = userBuilder.id(1L).name("namePatched").build();
        userDto = userDtoBuilder.id(1L).name("namePatched").build();
        String jsonPatched = mapper.writeValueAsString(userDto);
        when(service.patch(1L, patchParam)).thenReturn(user);
        this.mockMvc.perform(patch(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonPatched));

        //patch email
        patchParam.put("email", "patched@mail.ru");
        patchParam.remove("name");
        json = "{\"email\": \"patched@mail.ru\"}";
        user = userBuilder.email("patched@mail.ru").build();
        userDto = userDtoBuilder.email("patched@mail.ru").build();
        jsonPatched = mapper.writeValueAsString(userDto);
        when(service.patch(1L, patchParam)).thenReturn(user);
        this.mockMvc.perform(patch(URL + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json(jsonPatched));
    }

    @Test
    void shouldDelete() throws Exception {
        when(service.delete(1L)).thenReturn(true);
        this.mockMvc.perform(delete(URL + "/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

    }
}