package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.service.ItemService;
import ru.practicum.shareit.user.model.User;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@WebMvcTest(ItemController.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemControllerTest {
    private static final String URL = "/items";

    @MockBean
    private final ItemService service;

    @Autowired
    private MockMvc mockMvc;

    private User user;
    private Item item;
    private User.UserBuilder userBuilder;
    private Item.ItemBuilder itemBuilder;

    @BeforeEach
    void setupBuilder() {
        userBuilder = User.builder()
                .name("name")
                .email("e@mail.ru");
        itemBuilder = Item.builder()
                .name("name")
                .description("description")
                .available(true);
    }

    @Test
    void shouldCreateMockMvc() {
        assertNotNull(mockMvc);
    }

    @Test
    void shouldFindAll() throws Exception {
    }

    @Test
    void shouldFindById() throws Exception {
    }

    @Test
    void shouldFindByText() throws Exception {
    }

    @Test
    void shouldAdd() throws Exception {
    }

    @Test
    void shouldPatch() throws Exception {
    }

    @Test
    void shouldDeleteItem() throws Exception {
    }
}