package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.enums.Status;

import java.time.Instant;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long>, CrudRepository<Booking, Long>,
        PagingAndSortingRepository<Booking, Long> {

    List<Booking> findByItemIdAndStatusOrStatusOrderByStartAsc(Long id, Status status, Status status1);

    List<Booking> findByItemIdInAndStatusOrStatusOrderByStartAsc(List<Long> itemIds, Status status, Status status1);

    @Query("select b from Booking b where (b.item.id = :itemId) and " +
            "(b.status = :status) and " +
            "(b.start between :start and :end " +
            "OR b.end between :start and :end " +
            "OR b.start <= :start AND b.end >= :end)")
    List<Booking> findBookingsAtSameTime(@Param(value = "itemId") long itemId,
                                         @Param(value = "status") Status status,
                                         @Param(value = "start") Instant start,
                                         @Param(value = "end") Instant end);

    List<Booking> findByBookerIdAndItemIdAndStatusAndStartIsBefore(Long userId, long itemId, Status status, Instant now);

    List<Booking> findByItemOwnerId(Long userId, Sort sort);

    List<Booking> findByItemOwnerIdAndEndIsBefore(Long userId, Instant now, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsAfter(Long userId, Instant now, Sort sort);

    List<Booking> findByItemOwnerIdAndStartIsBeforeAndEndIsAfter(Long userId, Instant now, Instant now1, Sort sort);

    List<Booking> findByItemOwnerIdAndStatus(Long userId, Status status, Sort sort);

    List<Booking> findByBookerId(Long userId, Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Long userId, Instant now, Sort sort);

    List<Booking> findByBookerIdAndStartIsAfter(Long userId, Instant now, Sort sort);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long userId, Instant now, Instant now1, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long userId, Status status, Sort sort);
}