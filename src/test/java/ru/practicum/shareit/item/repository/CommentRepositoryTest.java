package ru.practicum.shareit.item.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Sort;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class CommentRepositoryTest {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    @Autowired
    private TestEntityManager em;

    @Autowired
    private CommentRepository repository;

    @Autowired
    private ItemRepository itemRepo;
    @Autowired
    private UserRepository userRepo;

    private  User author;
    private User owner;
    private Item item;
    private Comment comment;

    @BeforeEach
    void setup() {
        author = new User();
        author.setName("name");
        author.setEmail("e@mail.ru");

        owner = new User();
        owner.setName("name2");
        owner.setEmail("e2@mail.ru");

        item = new Item();
        item.setName("name");
        item.setDescription("description");
        item.setAvailable(true);
        item.setOwner(owner);

        comment = new Comment();
        comment.setText("comment");
        comment.setItem(item);
        comment.setAuthor(author);

        author = userRepo.save(author);
        owner = userRepo.save(owner);
        itemRepo.save(item);
        repository.save(comment);
    }

    @Test
    public void contextLoads() {
        assertNotNull(em);
    }

    @Test
    void findAllByItemId() {
        //EmptyList
        List<Comment> comments = repository.findAllByItemId(99L, SORT);
        assertNotNull(comments);
        assertEquals(0, comments.size());

        //Single List
        comments = repository.findAllByItemId(comment.getItem().getId(), SORT);
        assertNotNull(comments);
        assertEquals(1, comments.size());
        assertEquals(comments.get(0).getId(), comment.getId());

        //Проверка отката транзакций
        comments = repository.findAll();
        assertEquals(1, comments.size());
    }

    @Test
    void findAllByItemIdIn() {
        //EmptyList
        Item item1 = new Item();
        item1.setOwner(owner);
        item1.setAvailable(true);
        item1.setName("name1");
        item1.setDescription("description1");
        itemRepo.save(item1);

        List<Comment> comments = repository.findAllByItemIdIn(List.of(item1.getId()), SORT);
        assertNotNull(comments);
        assertEquals(0, comments.size());

        //SingleList
        comments = repository.findAllByItemIdIn(List.of(item.getId()), SORT);
        assertNotNull(comments);
        assertEquals(1, comments.size());

        //Sorting
        Comment comment1 = new Comment();
        comment1.setText("comment1");
        comment1.setItem(item1);
        comment1.setAuthor(author);
        repository.save(comment1);

        comments = repository.findAllByItemIdIn(List.of(item.getId(), item1.getId()), SORT);
        assertNotNull(comments);
        assertEquals(2, comments.size());
        assertEquals(comments.get(0).getId(), comment1.getId());

        //Проверка отката транзакций
        comments = repository.findAll();
        assertEquals(2, comments.size());
    }
}