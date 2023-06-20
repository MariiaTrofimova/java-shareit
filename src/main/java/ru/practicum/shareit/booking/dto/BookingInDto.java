package ru.practicum.shareit.booking.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.constraints.FutureOrPresent;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-bookings.
 */

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingInDto {
    private Long id;

    @NotNull(groups = ValidationGroups.Create.class, message = "Дата начала бронирования не может быть пустой")
    @FutureOrPresent(message = "Дата начала бронирования не может быть в прошлом")
    private LocalDateTime start;

    @NotNull(groups = ValidationGroups.Create.class, message = "Дата окончания бронирования не может быть пустой")
    @FutureOrPresent(message = "Дата окончания бронирования не может быть в прошлом")
    private LocalDateTime end;

    @NotNull(groups = ValidationGroups.Create.class, message = "Не указана вещь")
    private Long itemId;

    @NotBlank(groups = ValidationGroups.Update.class, message = "Статус не может быть пустым")
    private String status;
}