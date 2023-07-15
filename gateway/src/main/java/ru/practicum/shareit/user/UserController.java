package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.shareit.user.dto.UserDto;

import javax.validation.Valid;
import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static ru.practicum.shareit.validation.ValidationGroups.Create;
import static ru.practicum.shareit.validation.ValidationGroups.Update;

@RestController
@RequestMapping(path = "/users")
@Validated
@RequiredArgsConstructor
public class UserController {
    private final UserClient userClient;

    @GetMapping
    public ResponseEntity<Object> findAll() {
        return userClient.findUsers();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> findById(@PathVariable long id) {
        return userClient.findUserById(id);
    }

    @PostMapping(consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Validated(Create.class)
    public ResponseEntity<Object> add(@Valid @RequestBody UserDto userDto) {
        return userClient.addUser(userDto);
    }

    @PatchMapping(value = "/{id}", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @Validated(Update.class)
    public ResponseEntity<Object> patch(@Valid @RequestBody UserDto userDto,
                                        @PathVariable("id") long id) {
        return userClient.patchUser(id, userDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> delete(@PathVariable long id) {
        return userClient.deleteUser(id);
    }
}