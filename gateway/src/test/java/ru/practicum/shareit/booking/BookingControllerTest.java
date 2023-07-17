package ru.practicum.shareit.booking;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.booking.dto.BookItemRequestDto;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BookingController.class)
class BookingControllerTest {
    private static final String URL = "/bookings";
    private static final String SIZE_DEFAULT = "10";
    private static final String FROM_ERROR_MESSAGE = "Индекс первого элемента не может быть отрицательным";
    private static final String SIZE_ERROR_MESSAGE = "Количество элементов для отображения должно быть положительным";


    @Autowired
    ObjectMapper mapper;

    @MockBean
    BookingClient client;

    @Autowired
    private MockMvc mvc;

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mvc);
    }

    @Test
    void shouldValidateGetBookings() throws Exception {
        //Fail By State
        String state = "qwerty";
        String error = "Unknown state: " + state;
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", state)
                        .param("from", "0")
                        .param("size", SIZE_DEFAULT))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));

        //Fail By From
        error = FROM_ERROR_MESSAGE;
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "all")
                        .param("from", "-1")
                        .param("size", SIZE_DEFAULT))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));

        //Fail By Size
        error = SIZE_ERROR_MESSAGE;
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "all")
                        .param("from", "0")
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));

        // Header absence
        error = "X-Sharer-User-Id";
        mvc.perform(get(URL))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("error", containsString(error)));
    }

    @Test
    void shouldValidateGetBookingsForOwnerItems() throws Exception {
        //Fail By State
        String state = "qwerty";
        String error = "Unknown state: " + state;
        mvc.perform(get(URL + "/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", state)
                        .param("from", "0")
                        .param("size", SIZE_DEFAULT))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));

        //Fail By From
        error = FROM_ERROR_MESSAGE;
        mvc.perform(get(URL + "/owner")
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "all")
                        .param("from", "-1")
                        .param("size", SIZE_DEFAULT))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));

        //Fail By Size
        error = SIZE_ERROR_MESSAGE;
        mvc.perform(get(URL)
                        .header("X-Sharer-User-Id", 1)
                        .param("state", "all")
                        .param("from", "0")
                        .param("size", "0"))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString(error)));
    }

    @Test
    void shouldValidateBookItem() throws Exception {
        LocalDateTime now = LocalDateTime.now();
        BookItemRequestDto bookingInDto = BookItemRequestDto.builder()
                .itemId(1L)
                .start(now.plusMinutes(1))
                .end(now.plusMinutes(2)).build();

        //fail by start time
        bookingInDto.setItemId(1L);
        bookingInDto.setStart(null);
        String json = mapper.writeValueAsString(bookingInDto);
        String error = "Дата начала бронирования не может быть пустой";
        mvc.perform(post(URL)
                        .header("X-Sharer-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.start", containsString(error), String.class));

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
                .andExpect(jsonPath("$.validationErrors.end", containsString(error), String.class));
    }
}