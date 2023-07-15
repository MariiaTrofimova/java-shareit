package ru.practicum.shareit.user.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.test.json.JsonContent;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class UserDtoTest {
    @Autowired
    private JacksonTester<UserDto> json;

    private String name;
    private String email;

    @BeforeEach
    void setup() {
        name = "John";
        email = "john@mail.com";
    }

    @Test
    void testUserDtoSerialize() throws Exception {
        UserDto userDto = new UserDto(1L, name, email);
        JsonContent<UserDto> result = json.write(userDto);

        assertThat(result).extractingJsonPathStringValue("$.name").isEqualTo(name);
        assertThat(result).extractingJsonPathStringValue("$.email").isEqualTo(email);
    }

    @Test
    void testUserDtoDeserialize() throws Exception {
        String jsonContent = String.format("{\"name\":\"%s\", \"email\": \"%s\"}", name, email);
        UserDto result = this.json.parse(jsonContent).getObject();

        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getEmail()).isEqualTo(email);
    }
}