package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, CrudRepository<Item, Long> {
    List<Item> findByOwnerId(long userId);

    @Query(" select i from Item i " +
            "join fetch i.owner " +
            "where (lower(i.name) like concat('%', :text, '%') " +
            " or lower(i.description) like concat('%', :text, '%')) " +
            " and i.available = true")
    List<Item> search(@Param(value = "text") String text);
}