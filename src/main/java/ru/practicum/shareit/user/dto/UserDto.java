package ru.practicum.shareit.user.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import static ru.practicum.shareit.validation.ValidationGroups.Create;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private Long id;

    @NotBlank(groups = Create.class, message = "Имя не может быть пустым")
    private String name;

    @NotBlank(groups = Create.class, message = "E-mail не может быть пустым")
    @Email(message = "Введен некорректный e-mail")
    private String email;
}
