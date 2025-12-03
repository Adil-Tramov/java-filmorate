package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FilmService {
    private final FilmStorage filmStorage;
    private final GenreService genreService;
    private final MpaRatingService mpaRatingService;
    private final UserService userService;

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        return filmStorage.findFilmById(id)
                .orElseThrow(() -> new NotFoundException("Фильм не найден с id: " + id));
    }

    public Film create(Film film) {
        if (film.getMpa() != null) {
            mpaRatingService.findById(film.getMpa().getId());
        }

        if (film.getGenres() != null) {
            film.getGenres().forEach(genre -> genreService.findById(genre.getId()));
        }

        return filmStorage.create(film);
    }

    public Film update(Film film) {
        findById(film.getId());
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        findById(filmId);
        userService.findById(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        findById(filmId);
        userService.findById(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getMostPopularFilms(count);
    }
}
