package ru.practicum.shareit.item.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long>, CrudRepository<Item, Long>,
        PagingAndSortingRepository<Item, Long> {
    List<Item> findByOwnerId(long userId);

    Page<Item> findByOwnerId(long userId, Pageable page);

    @Query(" select i from Item i " +
            "where (lower(i.name) like concat('%', :text, '%') " +
            " or lower(i.description) like concat('%', :text, '%')) " +
            " and i.available = true")
    List<Item> search(@Param("text") String text);

    @Query(" select i from Item i " +
            "where (lower(i.name) like concat('%', :text, '%') " +
            " or lower(i.description) like concat('%', :text, '%')) " +
            " and i.available = true")
    Page<Item> searchWithPaging(@Param("text") String text, Pageable page);

    List<Item> findByRequestId(long requestId);

    List<Item> findByRequestIdIn(List<Long> requestIds);
}