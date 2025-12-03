package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {
    @Mock
    private FilmStorage filmStorage;

    @Mock
    private UserService userService;

    @Mock
    private MpaRatingService mpaRatingService;

    @Mock
    private GenreService genreService;

    @InjectMocks
    private FilmService filmService;

    @Test
    void create_shouldValidateMpaAndGenres() {
        MpaRating mpa = new MpaRating(1, "G");
        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1, "Комедия"));

        Film film = new Film(null, "Name", "Desc", LocalDate.now(), 100, mpa, genres, new HashSet<>());

        when(mpaRatingService.findById(1)).thenReturn(mpa);
        when(genreService.findById(1)).thenReturn(new Genre(1, "Комедия"));
        when(filmStorage.create(any())).thenReturn(film);

        filmService.create(film);

        verify(mpaRatingService).findById(1);
        verify(genreService).findById(1);
        verify(filmStorage).create(film);
    }

    @Test
    void addLike_shouldThrowExceptionIfFilmNotFound() {
        when(filmStorage.findFilmById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> filmService.addLike(1L, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Фильм не найден");

        verify(userService, never()).findById(anyLong());
    }
}
