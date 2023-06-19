package ru.practicum.shareit.item.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.item.model.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long>,
        CrudRepository<Item, Long> {
    List<Item> findByOwnerId(long userId);

    /*@Query(" select i from Item i " +
            "where upper(i.name) like upper(concat('%', :text, '%')) " +
            " or upper(i.description) like upper(concat('%', :text, '%'))")*/
    @Query(value ="select * from items where name ilike :text or description ilike :text", nativeQuery = true)
    List<Item> findByText(@Param(value = "text") String text);

    @Modifying
    @Query("update Item i set i.name = :name, i.description = :description, i.available = :available " +
            "where i.id = :id")
    Item update(@Param(value = "id") long id,
                @Param(value = "name") String name,
                @Param(value = "description") String description,
                @Param(value = "available") boolean available);

}
