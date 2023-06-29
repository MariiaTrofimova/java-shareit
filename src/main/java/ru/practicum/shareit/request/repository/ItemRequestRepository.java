package ru.practicum.shareit.request.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;

@Repository
public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long>,
        CrudRepository<ItemRequest, Long>,
        PagingAndSortingRepository<ItemRequest, Long> {
    List<ItemRequest> findByRequestorId(long requestorId);

    Page<ItemRequest> findByRequestorIdNot(long requestorId, Pageable page);

    List<ItemRequest> findByRequestorIdNot(long requestorId, Sort sort);

}
