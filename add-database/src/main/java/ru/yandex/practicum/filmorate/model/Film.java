package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Positive;
import javax.validation.constraints.FutureOrPresent;
import java.time.LocalDate;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Film {
    private Long id;

    @NotBlank
    private String name;

    @Size(max = 200)
    private String description;

    @FutureOrPresent
    private LocalDate releaseDate;

    @Positive
    private Integer duration;

    private MpaRating mpa;

    private Set<Genre> genres;

    private Set<Long> likes;
}