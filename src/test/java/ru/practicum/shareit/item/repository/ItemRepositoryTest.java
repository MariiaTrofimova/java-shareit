package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.repository.ItemRequestRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import javax.persistence.TypedQuery;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class ItemRepositoryTest {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRepository repository;

    @Autowired
    private UserRepository userRepo;

    @Autowired
    private ItemRequestRepository requestRepo;

    private User owner;
    private Item item;
    private User requestor;
    private ItemRequest request;

    @BeforeEach
    void setup() {
        owner = new User();
        owner.setName("name");
        owner.setEmail("e@mail.ru");
        owner = userRepo.save(owner);

        requestor = new User();
        requestor.setName("name1");
        requestor.setEmail("e1@mail.ru");
        requestor = userRepo.save(requestor);

        request = new ItemRequest();
        request.setDescription("description");
        request.setRequestor(requestor);
        request = requestRepo.save(request);

        item = new Item();
        item.setName("Набор отверток");
        item.setDescription("Большой набор отверток");
        item.setAvailable(true);
        item.setOwner(owner);
        item.setRequest(request);
        item = repository.save(item);
    }

    @Test
    public void contextLoads() {
        assertNotNull(em);
    }

    @Test
    void findByOwnerId() {
        //Empty List
        List<Item> items = repository.findByOwnerId(0L);
        assertNotNull(items);
        assertEquals(0, items.size());

        //Single List
        items = repository.findByOwnerId(owner.getId());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());

        //With Paging
        Item item1 = new Item();
        item1.setOwner(owner);
        item1.setAvailable(true);
        item1.setName("Дрель");
        item1.setDescription("Дрель — ваш ответ соседям с перфоратором");
        repository.save(item1);

        int pageNum = 0;
        int size = 1;
        PageRequest page = PageRequest.of(pageNum, size);

        items = repository.findByOwnerId(owner.getId(), page).getContent();
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());

        pageNum = 1;
        page = PageRequest.of(pageNum, size);
        items = repository.findByOwnerId(owner.getId(), page).getContent();
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item1.getId(), items.get(0).getId());
    }

    @Test
    void search() {
        //Empty List
        String text = "фыва";
        TypedQuery<Item> query = em.getEntityManager()
                .createQuery(" select i from Item i " +
                        "where (lower(i.name) like concat('%', :text, '%') " +
                        " or lower(i.description) like concat('%', :text, '%')) " +
                        " and i.available = true", Item.class);
        List<Item> items = query.setParameter("text", text).getResultList();
        assertEquals(0, items.size());
        List<Item> itemsSearch = repository.search(text);
        assertNotNull(itemsSearch);
        assertEquals(0, itemsSearch.size());

        //Single List
        text = "отв";
        items = query.setParameter("text", text).getResultList();
        assertEquals(1, items.size());
        itemsSearch = repository.search(text);
        assertNotNull(itemsSearch);
        assertEquals(1, itemsSearch.size());
        assertEquals(items.get(0).getId(), itemsSearch.get(0).getId());

        //With Paging
        Item item1 = new Item();
        item1.setOwner(owner);
        item1.setAvailable(true);
        item1.setName("Дрель");
        item1.setDescription("Дрель — ваш ответ соседям с перфоратором");
        repository.save(item1);

        int pageNum = 0;
        int size = 1;
        PageRequest page = PageRequest.of(pageNum, size);
        itemsSearch = repository.searchWithPaging(text, page).getContent();
        assertNotNull(itemsSearch);
        assertEquals(1, itemsSearch.size());

        size = 2;
        page = PageRequest.of(pageNum, size);
        itemsSearch = repository.searchWithPaging(text, page).getContent();
        assertNotNull(itemsSearch);
        assertEquals(2, itemsSearch.size());
    }

    @Test
    void findByRequestId() {
        //Empty List
        List<Item> items = repository.findByRequestId(0L);
        assertNotNull(items);
        assertEquals(0, items.size());

        //Single List
        items = repository.findByRequestId(request.getId());
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
    }

    @Test
    void findByRequestIdIn() {
        //Empty List
        List<Item> items = repository.findByRequestIdIn(List.of(0L));
        assertNotNull(items);
        assertEquals(0, items.size());

        //Single List
        items = repository.findByRequestIdIn(List.of(request.getId()));
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(item.getId(), items.get(0).getId());
    }
}