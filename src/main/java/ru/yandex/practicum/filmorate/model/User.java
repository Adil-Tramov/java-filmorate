package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private long id;
    @Email @NotBlank private String email;
    @NotBlank private String login;
    private String name;
    @Past private LocalDate birthday;
    private final Set<Long> friends = new HashSet<>();
}