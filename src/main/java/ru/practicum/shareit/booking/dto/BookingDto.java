package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * TODO Sprint add-bookings.
 */

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDto {
    private Long id;

    @NotBlank(groups = ValidationGroups.Create.class, message = "Дата начала бронирования не может быть пустой")
    private String start;

    @NotBlank(groups = ValidationGroups.Create.class, message = "Дата окончания бронирования не может быть пустой")
    private String end;

    @NotNull(groups = ValidationGroups.Create.class, message = "Не указана вещь")
    private Item item;

    private User booker;

    @NotBlank(groups = ValidationGroups.Update.class, message = "Статус не может быть пустым")
    private Status status;
}