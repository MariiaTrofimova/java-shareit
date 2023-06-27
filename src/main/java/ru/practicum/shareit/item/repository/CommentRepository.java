package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Comment;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long>, CrudRepository<Comment, Long> {
    List<Comment> findAllByItemIdOrderByCreatedDesc(Long id);

    List<Comment> findAllByItemIdInOrderByCreatedDesc(List<Long> itemIds);
}
