package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * TODO Sprint add-controllers.
 */
@Data
@Builder
public class ItemDto {
    private Long id;
    @NotBlank(message = "Название не может быть пустым")
    private String name;
    @NotBlank(message = "Описание не может быть пустым")
    @Size(max = 200, message = "Длина описания должна до 200 символов")
    private String description;
    @NotNull(message = "Поле доступности вещи не может быть пустым")
    private Boolean available;
    private Long requestId;
}
