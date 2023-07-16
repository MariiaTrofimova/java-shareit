package ru.practicum.shareit.booking.service;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.dto.BookingInDto;
import ru.practicum.shareit.booking.dto.BookingOutDto;
import ru.practicum.shareit.booking.enums.State;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Transactional
@SpringBootTest
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class BookingServiceITTest {
    private final EntityManager em;
    private final BookingService service;

    @Test
    void findByState() {
        User owner = makeUser("name1", "e1@mail.ru");
        User booker = makeUser("name2", "e2@mail.ru");
        em.persist(owner);
        em.persist(booker);

        Item item = makeAvailableItem("name", "description", owner);
        em.persist(item);

        LocalDateTime now = LocalDateTime.now();
        List<BookingInDto> sourceBookings = List.of(
                makeBookingInDto(now, now.plusMinutes(5), item.getId()),
                makeBookingInDto(now.plusMinutes(10), now.plusMinutes(15), item.getId()),
                makeBookingInDto(now.plusMinutes(20), now.plusMinutes(25), item.getId())
        );
        sourceBookings.stream()
                .map(BookingMapper::toBooking)
                .forEach(booking -> {
                    booking.setBooker(booker);
                    booking.setStatus(Status.WAITING);
                    em.merge(booking);
                    booking.setItem(item);
                });

        em.flush();

        List<BookingOutDto> targetBookings = service.findByState(booker.getId(),
                State.ALL, 0, 10);

        assertThat(targetBookings, hasSize(sourceBookings.size()));
        for (BookingInDto sourceBooking : sourceBookings) {
            assertThat(targetBookings, hasItem(allOf(
                    hasProperty("id", notNullValue()),
                    hasProperty("status", equalTo(sourceBooking.getStatus()))
            )));
        }
    }

    private BookingInDto makeBookingInDto(LocalDateTime start,
                                          LocalDateTime end,
                                          long itemId) {
        return BookingInDto.builder()
                .start(start)
                .end(end)
                .itemId(itemId)
                .status(Status.WAITING)
                .build();
    }

    private User makeUser(String name, String email) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        return user;
    }

    private Item makeAvailableItem(String name, String description, User owner) {
        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setAvailable(true);
        item.setOwner(owner);
        return item;
    }
}