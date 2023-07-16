package ru.practicum.shareit.request.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestMapper;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemRequestServiceITTest {
    private static final int SIZE_DEFAULT = 10;

    private final EntityManager em;
    private final ItemRequestService service;

    @Test
    void findAll() {
        User requestor = makeUser("name", "e@mail.ru");
        em.persist(requestor);
        System.out.println(requestor.getId());
        List<ItemRequestDto> sourceRequests = List.of(
                makeRequestDto("description1"),
                makeRequestDto("description2"),
                makeRequestDto("description1")
        );
        List<ItemRequest> savedRequests = new ArrayList<>();
        sourceRequests.stream()
                .map(ItemRequestMapper::toItemRequest)
                .forEach(request -> {
                    request.setRequestor(requestor);
                    em.persist(request);
                    savedRequests.add(request);
                });
        User owner = makeUser("name1", "e1@mail.ru");
        em.persist(owner);
        ItemRequest request = savedRequests.get(0);
        Item item = makeAvailableItem("name", "description", owner, request);
        em.persist(item);

        em.flush();

        List<ItemRequestDto> targetRequests = service.findAll(owner.getId(), 0, SIZE_DEFAULT);

        assertThat(targetRequests, hasSize(sourceRequests.size()));
        for (ItemRequestDto sourceRequest : sourceRequests) {
            assertThat(targetRequests, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceRequest.getDescription()))
            )));
        }
        assertThat(targetRequests, hasItem(
                hasProperty("items", notNullValue())
        ));
    }

    private ItemRequestDto makeRequestDto(String description) {
        return ItemRequestDto.builder()
                .description(description)
                .build();
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Item makeAvailableItem(String name, String description, User owner, ItemRequest request) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        return item;
    }
}