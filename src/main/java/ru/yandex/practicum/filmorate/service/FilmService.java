package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class FilmService {
    private final FilmStorage storage;

    @Autowired
    public FilmService(FilmStorage storage) { this.storage = storage; }

    public void addLike(long filmId, long userId) {
        Film film = get(filmId);
        film.getLikes().add(userId);
    }

    public void removeLike(long filmId, long userId) {
        Film film = get(filmId);
        if (!film.getLikes().remove(userId)) {
            throw new NotFoundException("Лайк не найден");
        }
    }

    public Collection<Film> popular(int count) {
        return storage.findAll()
                .stream()
                .sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    private Film get(long id) {
        return storage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }
}