package ru.practicum.shareit.request.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ItemRequestRepositoryTest {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ItemRequestRepository repository;

    @Autowired
    private UserRepository userRepo;

    private ItemRequest request;
    private User requestor;

    @BeforeEach
    void setup() {
        requestor = new User();
        requestor.setName("name");
        requestor.setEmail("e@mail.ru");

        request = new ItemRequest();
        request.setDescription("description");
        request.setRequestor(requestor);
    }

    @Test
    public void contextLoads() {
        assertNotNull(em);
    }

    @Test
    void verifyBootstrappingByPersistingRequest() {
        assertNull(request.getId());
        em.persist(requestor);
        em.persist(request);
        assertNotNull(request.getId());
    }

    @Test
    void verifyRepositoryByPersistingRequest() {
        assertNull(request.getId());
        userRepo.save(requestor);
        repository.save(request);
        assertNotNull(request.getId());
    }

    @Test
    void shouldFindByRequestorId() {
        //Empty List
        List<ItemRequest> requests = repository.findByRequestorId(1L);
        assertNotNull(requests);
        assertEquals(0, requests.size());

        //Single List
        em.persist(requestor);
        em.persist(request);
        requests = repository.findByRequestorId(requestor.getId());
        assertNotNull(requests);
        assertEquals(1, requests.size());
    }

    @Test
    void shouldFindByRequestorIdNotWithPaging() {
        int pageNum = 0;
        int size = 1;
        PageRequest page = PageRequest.of(pageNum, size, SORT);

        //Empty List
        List<ItemRequest> requests = repository.findByRequestorIdNot(1L, page).getContent();
        assertNotNull(requests);
        assertEquals(0, requests.size());

        //Single List
        em.persist(requestor);
        em.persist(request);
        requests = repository.findByRequestorIdNot(2L, page).getContent();
        assertEquals(1, requests.size());

        //Sort and Paging
        ItemRequest request2 = new ItemRequest();
        request2.setDescription("description2");
        request2.setRequestor(requestor);
        em.persist(request2);
        requests = repository.findByRequestorIdNot(2L, page).getContent();
        assertEquals(1, requests.size());
        assertEquals("description2", requests.get(0).getDescription());
    }
}