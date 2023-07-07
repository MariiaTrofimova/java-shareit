package ru.practicum.shareit.booking.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class BookingInDtoTest {
    @Autowired
    private JacksonTester<BookingInDto> json;
    private static final LocalDateTime NOW = LocalDateTime.now();

    LocalDateTime start;
    LocalDateTime end;
    long itemId;

    @BeforeEach
    void setup() {
        start = NOW;
        end = NOW.plusMinutes(30);
        itemId = 1L;
    }

    @Test
    void testBookingDeserialization() throws IOException {
        String jsonContent = String.format("{\"itemId\":\"%d\", \"start\": \"%s\", \"end\": \"%s\"}", itemId, start, end);
        BookingInDto result = this.json.parse(jsonContent).getObject();

        assertThat(result.getItemId()).isEqualTo(itemId);
        assertThat(result.getStart()).isEqualTo(start);
    }
}