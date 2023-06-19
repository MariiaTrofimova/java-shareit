package ru.practicum.shareit.booking.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.booking.Booking;

public interface BookingRepository  extends JpaRepository<Booking, Long> {
}
