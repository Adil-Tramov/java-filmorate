package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    private Long id;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String login;

    private String name;

    @Past(message = "Дата рождения должна быть в прошлом")
    private LocalDate birthday;

    @JsonIgnore
    private Set<Long> friends;
}