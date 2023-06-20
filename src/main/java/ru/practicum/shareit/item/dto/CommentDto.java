package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    Long Id;
    @NotBlank(message = "Текст комментария не может быть пустым")
    String text;
    String authorName;
    LocalDateTime created;
}
