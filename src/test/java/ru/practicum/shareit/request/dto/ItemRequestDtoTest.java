package ru.practicum.shareit.request.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;
import ru.practicum.shareit.item.dto.ItemDto;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoTest {
    @Autowired
    private JacksonTester<ItemRequestDto> json;

    private long id;
    private String description;
    private LocalDateTime created;
    private List<ItemDto> items;

    @BeforeEach
    void setup() {
        id = 1L;
        description = "description";
        created = LocalDateTime.now();
        ItemDto itemDto = ItemDto.builder()
                .id(1L)
                .name("name")
                .description("description")
                .available(true)
                .requestId(id)
                .build();
        items = List.of(itemDto);
    }

    @Test
    void testRequestSerialization() throws Exception {
        ItemRequestDto itemRequestDto =
                new ItemRequestDto(id, description, created);
        itemRequestDto.addAllItems(items);
        JsonContent<ItemRequestDto> result = json.write(itemRequestDto);

        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(description);
        assertThat(result).extractingJsonPathStringValue("$.items[0].name").isEqualTo("name");
    }

    @Test
    void testRequestDeserialization() throws Exception {
        String jsonContent = String.format("{\"description\": \"%s\"}", description);
        ItemRequestDto result = this.json.parse(jsonContent).getObject();

        assertThat(result.getDescription()).isEqualTo(description);
    }
}