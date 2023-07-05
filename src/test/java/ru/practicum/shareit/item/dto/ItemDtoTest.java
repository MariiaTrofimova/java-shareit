package ru.practicum.shareit.item.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemDtoTest {
    @Autowired
    private JacksonTester<ItemDto> json;

    private long id;
    private String name;
    private String description;
    private boolean available;
    private long requestId;

    @BeforeEach
    void setup() {
        id = 1L;
        name = "name";
        description = "description";
        available = true;
        requestId = 1L;
    }

    @Test
    void testItemSerialization() throws Exception {
        ItemDto itemDto = new ItemDto(id, name, description, available, requestId);
        JsonContent<ItemDto> result = json.write(itemDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(name);
        assertThat(result).extractingJsonPathStringValue("$.description").isEqualTo(description);
        assertThat(result).extractingJsonPathBooleanValue("$.available").isEqualTo(available);
    }

    @Test
    void testItemDeserialization() throws Exception {
        String jsonContent = String.format(
                "{\"name\": \"%s\", \"description\": \"%s\", \"available\": \"%s\"}",
                name, description, available);
        ItemDto result = this.json.parse(jsonContent).getObject();

        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getAvailable()).isEqualTo(available);
    }
}