package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilmService {
    private final FilmStorage filmStorage;

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long id) {
        Optional<Film> film = filmStorage.findFilmById(id);
        if (film.isEmpty()) {
            throw new RuntimeException("Film not found with id: " + id);
        }
        return film.get();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        Optional<Film> existingFilm = filmStorage.findFilmById(film.getId());
        if (existingFilm.isEmpty()) {
            throw new RuntimeException("Film not found with id: " + film.getId());
        }
        return filmStorage.update(film);
    }

    public void addLike(Long filmId, Long userId) {
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getMostPopularFilms(int count) {
        return filmStorage.getMostPopularFilms(count);
    }
}
