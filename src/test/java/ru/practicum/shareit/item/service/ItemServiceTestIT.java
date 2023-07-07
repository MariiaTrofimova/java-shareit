package ru.practicum.shareit.item.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.dto.ItemBookingCommentsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class ItemServiceTestIT {
    private static final int SIZE_DEFAULT = 10;

    private final EntityManager em;
    private final ItemService service;

    @Test
    void findAllByUserId() {
        User owner = makeUser("name", "e@mail.ru");
        em.persist(owner);

        User booker = makeUser("name1", "e1@mail.ru");
        em.persist(booker);

        List<ItemDto> sourceItems = List.of(
                makeItemDto("name1", "description1"),
                makeItemDto("name2", "description2"),
                makeItemDto("name3", "description3")
        );
        List<Item> savedItems = new ArrayList<>();
        sourceItems.stream()
                .map(itemDto -> ItemMapper.toItem(itemDto, owner))
                .forEach(item -> {
                    em.persist(item);
                    savedItems.add(item);
                });

        Booking booking = makeNewBooking(Instant.now(), Instant.now().plusSeconds(60),
                savedItems.get(0), booker);
        em.persist(booking);
        Comment comment = makeComment("text", booker, savedItems.get(0));
        em.persist(comment);

        em.flush();

        List<ItemBookingCommentsDto> targetItems =
                service.findAllByUserId(owner.getId(), 0, SIZE_DEFAULT);

        assertThat(targetItems, hasSize(sourceItems.size()));
        for (ItemDto sourceRequest : sourceItems) {
            assertThat(targetItems, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("description", equalTo(sourceRequest.getDescription()))
            )));
        }
        assertThat(targetItems, hasItem(
                hasProperty("comments", notNullValue())
        ));

        assertThat(targetItems, hasItem(
                hasProperty("lastBooking", notNullValue())
        ));
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private ItemDto makeItemDto(String name, String description) {
        return ItemDto.builder()
                .name(name)
                .description(description)
                .available(true)
                .build();
    }

    private Comment makeComment(String text, User author, Item item) {
        Comment comment = new Comment();
        comment.setText(text);
        comment.setAuthor(author);
        comment.setItem(item);
        return comment;
    }

    private Booking makeNewBooking(Instant start, Instant end, Item item, User booker) {
        Booking booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStatus(Status.WAITING);
        booking.setStart(start);
        booking.setEnd(end);
        return booking;
    }
}