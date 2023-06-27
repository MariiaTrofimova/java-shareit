package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class CommentMapper {
    private static final ZoneOffset ZONE_OFFSET = OffsetDateTime.now().getOffset();

    public static CommentDto toCommentDto(Comment comment) {
        LocalDateTime created = LocalDateTime.ofInstant(comment.getCreated(), ZONE_OFFSET);
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorName(comment.getAuthor().getName())
                .created(created)
                .build();
    }

    public static Comment toComment(CommentDto commentDto) {
        Comment comment = new Comment();
        comment.setText(commentDto.getText());
        return comment;
    }
}