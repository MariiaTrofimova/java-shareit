package ru.practicum.shareit.item.mapper;

import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.model.Comment;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class CommentMapper {
    public static CommentDto toCommentDto(Comment comment) {
        LocalDateTime created = LocalDateTime.ofInstant(comment.getCreated(), ZoneOffset.of("+03:00"));
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
