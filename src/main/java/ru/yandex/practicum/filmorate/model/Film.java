package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private long id;
    @NotBlank private String name;
    @Size(max = 200) private String description;
    private LocalDate releaseDate;
    @Positive private int duration;
    private final Set<Long> likes = new HashSet<>();
}