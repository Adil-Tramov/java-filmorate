package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private long id;
    @Email @NotBlank private String email;
    @NotBlank @Pattern(regexp = "^\\S+$") private String login;
    private String name;
    @PastOrPresent private LocalDate birthday;
    private final Set<Long> friends = new HashSet<>();
}