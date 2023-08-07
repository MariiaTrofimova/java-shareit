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
    UNKNOWN;

    public static State from(String stateParam) {
        for (State state : values()) {
            if (state.name().equalsIgnoreCase(stateParam)) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown state: " + stateParam);
    }
}
