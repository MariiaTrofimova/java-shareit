package ru.practicum.shareit.booking.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.TypedQuery;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class BookingRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BookingRepository repository;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ItemRepository itemRepo;

    private Booking booking;
    private User owner;
    private User booker;
    private Item item;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setName("name");
        owner.setEmail("e@mail.ru");
        owner = userRepo.save(owner);

        booker = new User();
        booker.setName("name1");
        booker.setEmail("e1@mail.ru");
        booker = userRepo.save(booker);

        item = new Item();
        item.setName("Набор отверток");
        item.setDescription("Большой набор отверток");
        item.setAvailable(true);
        item.setOwner(owner);
        item = itemRepo.save(item);

        Instant now = Instant.now();
        booking = new Booking();
        booking.setItem(item);
        booking.setBooker(booker);
        booking.setStart(now.plusSeconds(5));
        booking.setEnd(now.plusSeconds(60));
        booking.setStatus(Status.APPROVED);
        booking = repository.save(booking);
    }

    @Test
    public void contextLoads() {
        assertNotNull(em);
    }

    @Test
    void findBookingsAtSameTime() {
        //Empty List
        Instant start = booking.getEnd().plusSeconds(5);
        Instant end = booking.getEnd().plusSeconds(25);
        Status status = Status.APPROVED;
        TypedQuery<Booking> query = em.getEntityManager()
                .createQuery("select b from Booking b where (b.item.id = :itemId) and " +
                "(b.status = :status) and " +
                "(b.start between :start and :end " +
                "OR b.end between :start and :end " +
                "OR b.start <= :start AND b.end >= :end)", Booking.class);
        List<Booking> bookings = query
                .setParameter("itemId", item.getId())
                .setParameter("status", status)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        assertNotNull(bookings);
        assertEquals(0, bookings.size());
        List<Booking> bookingsFound = repository.findBookingsAtSameTime(item.getId(), status, start, end);
        assertNotNull(bookingsFound);
        assertEquals(0, bookingsFound.size());

        //Single List
        start = booking.getStart().plusSeconds(5);
        bookings = query
                .setParameter("itemId", item.getId())
                .setParameter("status", status)
                .setParameter("start", start)
                .setParameter("end", end)
                .getResultList();
        assertNotNull(bookings);
        assertEquals(1, bookings.size());
        bookingsFound = repository.findBookingsAtSameTime(item.getId(), status, start, end);
        assertNotNull(bookingsFound);
        assertEquals(1, bookingsFound.size());
        assertEquals(bookings.get(0).getId(), bookingsFound.get(0).getId());
    }

}