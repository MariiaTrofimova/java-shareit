package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.enums.Status;

import java.time.Instant;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, CrudRepository<Booking, Long>,
        PagingAndSortingRepository<Booking, Long> {
    List<Booking> findByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findByBookerIdAndEndIsBeforeOrderByStartDesc(Long bookerId, Instant end);

    List<Booking> findByBookerIdAndStartIsAfterOrderByStartDesc(Long userId, Instant now);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfterOrderByStartDesc(Long userId, Instant now, Instant now1);

    List<Booking> findByBookerIdAndStatusOrderByStartDesc(Long userId, Status status);

    List<Booking> findByItemIdInOrderByStartDesc(List<Long> itemIds);

    List<Booking> findByItemIdInAndEndIsBeforeOrderByStartDesc(List<Long> itemIds, Instant now);

    List<Booking> findByItemIdInAndStartIsAfterOrderByStartDesc(List<Long> itemIds, Instant now);

    List<Booking> findByItemIdInAndStartIsBeforeAndEndIsAfterOrderByStartDesc(List<Long> itemIds, Instant now, Instant now1);

    List<Booking> findByItemIdInAndStatusOrderByStartDesc(List<Long> itemIds, Status status);

    List<Booking> findByItemIdAndStatusOrStatusOrderByStartAsc(Long id, Status status, Status status1);

    List<Booking> findByItemIdInAndStatusOrStatusOrderByStartAsc(List<Long> itemIds, Status status, Status status1);

    List<Booking> findByBookerIdAndStatusOrderByStart(Long userId, Status status);
}