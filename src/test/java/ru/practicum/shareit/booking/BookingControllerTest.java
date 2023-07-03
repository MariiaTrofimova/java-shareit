package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.service.BookingService;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.error.exception.UnsupportedStatusException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.ValidationException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    private static final String URL = "/bookings";

    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingService service;

    @Autowired
    private MockMvc mvc;

    private BookingOutDto bookingOutDto;

    private UserDto.UserDtoBuilder userDtoBuilder;
    private ItemDto.ItemDtoBuilder itemDtoBuilder;
    private BookingInDto.BookingInDtoBuilder builderIn;
    private BookingOutDto.BookingOutDtoBuilder builderOut;

    @BeforeEach
    void setupBuilder() {
        LocalDateTime now = LocalDateTime.now();
        userDtoBuilder = UserDto.builder()
                .id(1L)
                .name("name")
                .email("e@mail.ru");
        itemDtoBuilder = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true);
        builderIn = BookingInDto.builder()
                .itemId(1L)
                .start(now.plusMinutes(1))
                .end(now.plusMinutes(2));
        builderOut = BookingOutDto.builder()
                .id(1L)
                .booker(userDtoBuilder.build())
                .item(itemDtoBuilder.build())
                .start(now.plusMinutes(1))
                .end(now.plusMinutes(2))
                .status(Status.WAITING);
    }

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mvc);
    }

    @Test
    void shouldFindById() throws Exception {
        bookingOutDto = builderOut.build();
        //Regular Case
        when(service.findById(1L, 1L)).thenReturn(bookingOutDto);
        mvc.perform(get(URL + "/1")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingOutDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingOutDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.start", containsString(String.valueOf(
                        bookingOutDto.getStart().getSecond())), String.class))
                .andExpect(jsonPath("$.status", is(bookingOutDto.getStatus().toString()), String.class));

        //fail by userId
        String error = String.format("Пользователь с id %d не найден", -1);
        when(service.findById(-1L, 1L)).thenThrow(new NotFoundException(error));
        mvc.perform(get(URL + "/1")
                        .header("X-Sharer-User-Id", -1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(error), String.class));

        //fail by bookId
        error = String.format("Бронирование с id %d не найдено", 99);
        when(service.findById(1L, 99L)).thenThrow(new NotFoundException(error));
        mvc.perform(get(URL + "/99")
                        .header("X-Sharer-User-Id", 1))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(error), String.class));
    }

    @Test
    void shouldFindByState() throws Exception {
        //Empty List
        when(service.findByState(1L, State.REJECTED, 0, Optional.empty()))
                .thenReturn(Collections.emptyList());
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "rejected"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        //Single List
        bookingOutDto = builderOut.build();
        when(service.findByState(1L, State.WAITING, 0, Optional.of(1)))
                .thenReturn(List.of(bookingOutDto));
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "waiting")
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingOutDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(bookingOutDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].start", is(bookingOutDto.getStart().toString()), String.class))
                .andExpect(jsonPath("$[0].end", is(bookingOutDto.getEnd().toString()), String.class))
                .andExpect(jsonPath("$[0].status", is(bookingOutDto.getStatus().toString()), String.class));

        //Fail By State
        String error = "Unknown state: UNSUPPORTED_STATUS";
        when(service.findByState(1L, State.UNKNOWN, 0, Optional.of(1)))
                .thenThrow(new UnsupportedStatusException(error));
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "qwerty")
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));

        //Fail By From
        error = "Индекс первого элемента не может быть отрицательным";
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "qwerty")
                        .param("from", "-1")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));
    }

    @Test
    void shouldFindByOwnerItemsAndState() throws Exception {
        //Empty List
        when(service.findByOwnerItemsAndState(1L, State.REJECTED, 0, Optional.empty()))
                .thenReturn(Collections.emptyList());
        mvc.perform(get(URL + "/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "rejected"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        //Single List
        bookingOutDto = builderOut.build();
        when(service.findByOwnerItemsAndState(1L, State.WAITING, 0, Optional.of(1)))
                .thenReturn(List.of(bookingOutDto));
        mvc.perform(get(URL + "/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "waiting")
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$[0].item.id", is(bookingOutDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$[0].booker.id", is(bookingOutDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$[0].start", containsString(String.valueOf(
                        bookingOutDto.getStart().getSecond())), String.class))
                .andExpect(jsonPath("$[0].status", is(bookingOutDto.getStatus().toString()), String.class));

        //Fail By State
        String error = "Unknown state: UNSUPPORTED_STATUS";
        when(service.findByOwnerItemsAndState(1L, State.UNKNOWN, 0, Optional.of(1)))
                .thenThrow(new UnsupportedStatusException(error));
        mvc.perform(get(URL + "/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "qwerty")
                        .param("from", "0")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));

        //Fail By From
        error = "Индекс первого элемента не может быть отрицательным";
        mvc.perform(get(URL + "/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "qwerty")
                        .param("from", "-1")
                        .param("size", "1"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));

    }

    @Test
    void shouldAdd() throws Exception {
        BookingInDto bookingInDto = builderIn.build();
        bookingOutDto = builderOut.build();
        String json = mapper.writeValueAsString(bookingInDto);
        //Regular Case
        when(service.add(1L, bookingInDto)).thenReturn(bookingOutDto);
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingOutDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingOutDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.status", is(bookingOutDto.getStatus().toString()), String.class));

        //fail by userId
        String error = String.format("Пользователь с id %d не найден", -1);
        when(service.add(-1L, bookingInDto)).thenThrow(new NotFoundException(error));
        this.mvc
                .perform(post(URL)
                        .header("X-Sharer-User-Id", -1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(error), String.class));

        //fail by itemId
        error = String.format("Вещь с id %d не найдена", 99);
        bookingInDto.setItemId(99L);
        json = mapper.writeValueAsString(bookingInDto);
        when(service.add(1L, bookingInDto)).thenThrow(new NotFoundException(error));
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is(error), String.class));

        //fail by itemId null
        bookingInDto.setItemId(null);
        json = mapper.writeValueAsString(bookingInDto);
        error = "Не указана вещь";
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.error", containsString(error), String.class));

        //fail by start time
        bookingInDto.setItemId(1L);
        bookingInDto.setStart(null);
        json = mapper.writeValueAsString(bookingInDto);
        error = "Дата начала бронирования не может быть пустой";
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", notNullValue()))
                .andExpect(jsonPath("$.error", containsString(error), String.class));

        //fail by end time
        bookingInDto.setStart(LocalDateTime.now().plusMinutes(1));
        bookingInDto.setEnd(null);
        json = mapper.writeValueAsString(bookingInDto);
        error = "Дата окончания бронирования не может быть пустой";
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error), String.class));
    }

    @Test
    void shouldPatch() throws Exception {
        //Regular Case True
        bookingOutDto = builderOut.status(Status.APPROVED).build();
        when(service.patch(1L, 1L, true)).thenReturn(bookingOutDto);
        mvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "true"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingOutDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingOutDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.start",
                        containsString(String.valueOf(bookingOutDto.getStart().getSecond())), String.class))
                .andExpect(jsonPath("$.status", is(bookingOutDto.getStatus().toString()), String.class));

        //Regular Case False
        bookingOutDto = builderOut.status(Status.REJECTED).build();
        when(service.patch(1L, 1L, false)).thenReturn(bookingOutDto);
        mvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "false"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookingOutDto.getId()), Long.class))
                .andExpect(jsonPath("$.item.id", is(bookingOutDto.getItem().getId()), Long.class))
                .andExpect(jsonPath("$.booker.id", is(bookingOutDto.getBooker().getId()), Long.class))
                .andExpect(jsonPath("$.start", containsString(String.valueOf(
                        bookingOutDto.getStart().getSecond())), String.class))
                .andExpect(jsonPath("$.status", is(bookingOutDto.getStatus().toString()), String.class));

        //Fail by repeat answer
        String error = String.format("Бронирование с id %d уже отклонено", 1);
        when(service.patch(1L, 1L, false)).thenThrow(new ValidationException(error));
        mvc.perform(patch(URL + "/1")
                        .header("X-Sharer-User-Id", 1)
                        .param("approved", "false"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error), String.class));
    }
}