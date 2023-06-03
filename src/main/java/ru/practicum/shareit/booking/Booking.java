package ru.practicum.shareit.booking;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * TODO Sprint add-bookings.
 */
@Data
@Builder
public class Booking {
    private Long id;
    @NotNull(message = "Дата начала аренды не может быть пустой")
    private LocalDate start;
    @NotNull(message = "Дата окончания аренды не может быть пустой")
    private LocalDate end;
    private Item item;
    private User booker;
    private Status status;
}
