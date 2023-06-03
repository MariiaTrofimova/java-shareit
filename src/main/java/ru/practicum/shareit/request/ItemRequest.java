package ru.practicum.shareit.request;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.user.model.User;

import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * TODO Sprint add-item-requests.
 */
@Data
@Builder
public class ItemRequest {
    Long id;
    @Size(min = 1, max = 200, message = "Длина описания должна быть от 1 до 200 символов")
    String description;
    User requestor;
    LocalDateTime created;
}
