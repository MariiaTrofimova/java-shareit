package ru.practicum.shareit.item.dto;

import lombok.Builder;
import lombok.Data;
import ru.practicum.shareit.booking.dto.BookingForItemsOutDto;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ItemBookingCommentsDto {
    private Long id;
    private String name;
    private String description;
    private Boolean available;
    private Long requestId;
    private BookingForItemsOutDto lastBooking;
    private BookingForItemsOutDto nextBooking;
    private final List<CommentDto> comments = new ArrayList<>();

    public void addComment(CommentDto comment) {
        comments.add(comment);
    }
}
