package ru.practicum.shareit.request.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.validation.ValidationGroups;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO Sprint add-item-requests.
 */

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ItemRequestDto {
    long id;

    @NotBlank(groups = ValidationGroups.Create.class, message = "Описание не может быть пустым")
    @Size(max = 200, message = "Длина описания должна до 200 символов")
    private String description;
    private LocalDateTime created;

    private final List<ItemDto> items = new ArrayList<>();

    public void addAllItems(List<ItemDto> itemsToAdd) {
        items.addAll(itemsToAdd);
    }

}