package ru.yandex.javacourse.sprint10.model;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.Objects;

@Data
public class User {
    private Integer id;

    @Email(message = "Электронная почта должна быть валидной")
    @NotBlank(message = "Электронная почта не может быть пустой")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "^[^\\s]+$", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @Past(message = "Дата рождения не может быть в будущем")
    @NotNull(message = "Дата рождения не может быть пустой")
    private LocalDate birthday;

    public String getName() {
        if (name == null || name.isBlank()) {
            return login;
        }
        return name;
    }
}
