package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Builder
public class CommentDto {
    Long Id;
    @NotBlank(message = "Текст комментария не может быть пустым")
    @Size(max = 512, message = "Текст комментария не может быть больше 512 символов")
    String text;
    String authorName;
    LocalDateTime created;
}
