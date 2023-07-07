package ru.practicum.shareit.item.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.enums.Status;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.error.exception.NotFoundException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.ItemBookingCommentsDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.mapper.CommentMapper;
import ru.practicum.shareit.item.mapper.ItemMapper;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.CommentRepository;
import ru.practicum.shareit.item.repository.ItemRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserRepository;
import ru.practicum.shareit.validation.Validation;

import javax.validation.ValidationException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@AllArgsConstructor
public class ItemServiceImpl implements ItemService {
    private static final Sort SORT = Sort.by(Sort.Direction.DESC, "created");

    private final ItemRepository repository;
    private final UserRepository userRepo;
    private final BookingRepository bookingRepo;
    private final CommentRepository commentRepo;

    @Override
    public List<ItemBookingCommentsDto> findAllByUserId(long userId, int from, int size) {
        userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
        List<Item> items;
        PageRequest page = PageRequest.of(from / size, size);
        items = repository.findByOwnerId(userId, page).getContent();

        if (items.isEmpty()) {
            return Collections.emptyList();
        }
        Map<Long, ItemBookingCommentsDto> itemsWithIds = new HashMap<>();
        items.forEach(item -> itemsWithIds.put(item.getId(), ItemMapper.toItemBookingCommentsDto(item)));
        addCommentsToItems(itemsWithIds);
        addBookingDatesToItems(itemsWithIds);
        return new ArrayList<>(itemsWithIds.values());
    }

    @Override
    public ItemBookingCommentsDto findById(long userId, long itemId) {
        checkUser(userId);
        Item item = repository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с id %d не найдена", itemId)));
        ItemBookingCommentsDto itemDto = ItemMapper.toItemBookingCommentsDto(item);
        long ownerId = item.getOwner().getId();
        if (ownerId == userId) {
            addBookingsToItem(itemDto);
        }
        addCommentsToItem(itemDto);
        return itemDto;
    }

    @Override
    public List<ItemDto> findByText(String text, int from, int size) {
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        PageRequest page = PageRequest.of(from / size, size);
        List<Item> items = repository.searchWithPaging(text.toLowerCase(), page).getContent();
        return items.stream().map(ItemMapper::toItemDto).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public ItemDto add(long userId, ItemDto itemDto) {
        User owner = checkUser(userId);
        Item item = ItemMapper.toItem(itemDto, owner);
        item = repository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Transactional
    @Override
    public ItemDto patch(long userId, long itemId, ItemDto itemDto) {
        Item item = checkOwner(userId, itemId);
        String newName = itemDto.getName();
        String newDescription = itemDto.getDescription();
        Boolean newAvailable = itemDto.getAvailable();

        if (newName != null) {
            Validation.checkNotBlank(newName, "Название");
            item.setName(newName);
        }
        if (newDescription != null) {
            Validation.checkNotBlank(newDescription, "Описание");
            item.setDescription(newDescription);
        }
        if (newAvailable != null) {
            item.setAvailable(newAvailable);
        }
        item = repository.save(item);
        return ItemMapper.toItemDto(item);
    }

    @Transactional
    @Override
    public void delete(long userId, long itemId) {
        checkOwner(userId, itemId);
        repository.deleteById(itemId);
    }

    @Transactional
    @Override
    public CommentDto addComment(Long userId, long itemId, CommentDto commentDto) {
        User author = checkUser(userId);
        Item item = checkItem(itemId);
        checkBooker(userId, itemId);
        Comment comment = CommentMapper.toComment(commentDto);
        comment.setAuthor(author);
        comment.setItem(item);
        comment = commentRepo.save(comment);
        return CommentMapper.toCommentDto(comment);
    }

    private User checkUser(Long userId) {
        return userRepo.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", userId)));
    }

    private Item checkItem(Long itemId) {
        return repository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(String.format("Вещь с id %d не найдена", itemId)));
    }

    private Item checkOwner(long userId, long itemId) {
        checkUser(userId);
        Item item = checkItem(itemId);
        long ownerId = item.getOwner().getId();
        if (ownerId != userId) {
            log.warn("Пользователь с id {} не владеет вещью с id {}", userId, itemId);
            throw new NotFoundException(String.format("Пользователь с id %d не владеет вещью с id %d", userId, itemId));
        }
        return item;
    }

    private void checkBooker(Long userId, long itemId) {
        List<Booking> bookingsItemByUser = bookingRepo
                .findByBookerIdAndItemIdAndStatusAndStartIsBefore(userId, itemId, Status.APPROVED, Instant.now());
        if (bookingsItemByUser.isEmpty()) {
            log.warn("Пользователь с id {} не арендовал вещь с id {}", userId, itemId);
            throw new ValidationException(
                    String.format("Пользователь с id %s не арендовал вещь с id %s", userId, itemId));
        }
    }

    private void addCommentsToItem(ItemBookingCommentsDto item) {
        commentRepo.findAllByItemId(item.getId(), SORT)
                .forEach(comment -> item.addComment(CommentMapper.toCommentDto(comment)));
    }

    private void addBookingsToItem(ItemBookingCommentsDto itemDto) {
        List<Booking> bookings = bookingRepo.findByItemIdAndStatusOrStatusOrderByStartAsc(itemDto.getId(),
                Status.APPROVED, Status.WAITING);
        if (bookings.isEmpty()) {
            return;
        }
        addLastAndNextBookings(bookings, itemDto);
    }

    private void addBookingDatesToItems(Map<Long, ItemBookingCommentsDto> itemsWithId) {
        Map<Long, List<Booking>> bookings = new HashMap<>();
        List<Long> itemIds = new ArrayList<>(itemsWithId.keySet());

        List<Booking> bookingsList = bookingRepo.findByItemIdInAndStatusOrStatusOrderByStartAsc(itemIds,
                Status.APPROVED, Status.WAITING);

        bookingsList.forEach(booking -> bookings.computeIfAbsent(booking.getItem().getId(),
                key -> new ArrayList<>()).add(booking));

        bookings.forEach((key, value) -> addLastAndNextBookings(value, itemsWithId.get(key)));
    }

    private void addLastAndNextBookings(List<Booking> bookings, ItemBookingCommentsDto itemDto) {
        Booking lastBooking;
        Booking nextBooking = null;
        Instant now = Instant.now();

        if (bookings.get(0).getStart().isAfter(now)) {
            itemDto.setNextBooking(BookingMapper.toBookingForItemsOutDto(bookings.get(0)));
            return;
        } else {
            lastBooking = bookings.get(0);
        }

        for (int i = 1; i < bookings.size(); i++) {
            if (bookings.get(i).getStart().isAfter(now)) {
                lastBooking = bookings.get(i - 1);
                nextBooking = bookings.get(i);
                break;
            }
        }
        itemDto.setLastBooking(BookingMapper.toBookingForItemsOutDto(lastBooking));
        if (nextBooking != null) {
            itemDto.setNextBooking(BookingMapper.toBookingForItemsOutDto(nextBooking));
        }
    }

    private void addCommentsToItems(Map<Long, ItemBookingCommentsDto> itemsWithId) {
        List<Long> itemIds = new ArrayList<>(itemsWithId.keySet());

        commentRepo.findAllByItemIdIn(itemIds, SORT)
                .forEach(comment -> itemsWithId.get(comment.getItem().getId())
                        .addComment(CommentMapper.toCommentDto(comment)));

    }
}