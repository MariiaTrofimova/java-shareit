package ru.practicum.shareit.booking.enums;

import com.fasterxml.jackson.annotation.JsonEnumDefaultValue;

public enum State {
    ALL,
    CURRENT,
    PAST,
    FUTURE,
    WAITING,
    REJECTED,
    @JsonEnumDefaultValue
    UNKNOWN
}
