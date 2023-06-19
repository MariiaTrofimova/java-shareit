package ru.practicum.shareit.booking.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.enums.Status;

import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long>{
    List<Booking> findByBookerId(Long bookerId, Sort sort);

    List<Booking> findByBookerIdAndEndIsBefore(Long bookerId, LocalDateTime end, Sort sort);

    List<Booking> findByBookerIdAndStartIsAfter(Long userId, LocalDateTime now, Sort sort);

    List<Booking> findByBookerIdAndStartIsBeforeAndEndIsAfter(Long userId, LocalDateTime now, LocalDateTime now1, Sort sort);

    List<Booking> findByBookerIdAndStatus(Long userId, Status status, Sort sort);

    List<Booking> findByItemIdIn(List<Long> itemIds, Sort sort);

    List<Booking> findByItemIdInAndEndIsBefore(List<Long> itemIds, LocalDateTime now, Sort sort);

    List<Booking> findByItemIdInAndStartIsAfter(List<Long> itemIds, LocalDateTime now, Sort sort);

    List<Booking> findByItemIdInAndStartIsBeforeAndEndIsAfter(List<Long> itemIds, LocalDateTime now, LocalDateTime now1, Sort sort);

    List<Booking> findByItemIdInAndStatus(List<Long> itemIds, Status status, Sort sort);

    @Modifying
    @Query("update Booking b set b.status = :status where b.id = :id")
    Booking updateStatus(@Param(value = "id") long id, @Param(value = "status") Status status);
}
