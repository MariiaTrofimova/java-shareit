package ru.practicum.shareit.validation;

import lombok.extern.slf4j.Slf4j;

import javax.validation.ValidationException;
import java.util.Optional;

@Slf4j
public class Validation {
    public static void checkPagingParams(int from, Optional<Integer> sizeOptional) {
        if (sizeOptional.isEmpty()) {
            if (from != 0) {
                log.warn("Некорректные параметры запроса");
                throw new ValidationException("Индекс первого элемента не может быть отрицательным");
            }
        } else {
            int size = sizeOptional.get();
            if (size <= 0) {
                log.warn("Количество элементов для отображения должно быть положительным");
                throw new ValidationException("Количество элементов для отображения должно быть положительным");
            }
        }
    }

    public static void checkNotBlank(String s, String parameterName) {
        if (s.isBlank()) {
            log.warn("{} не может быть пустым", parameterName);
            throw new ValidationException(String.format("%s не может быть пустым", parameterName));
        }
    }
}
