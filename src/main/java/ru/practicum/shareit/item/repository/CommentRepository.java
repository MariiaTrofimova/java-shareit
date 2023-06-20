package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>,
        CrudRepository<Comment, Long> {
    List<Comment> findAllByItemIdIn(List<Long> itemIds, Sort sort);
    List<Comment> findAllByItemId(Long itemId, Sort sort);
}
