package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();
    private long counter = 1;
    private static final LocalDate CINEMA_BIRTHDAY = LocalDate.of(1895, 12, 28);

    @Override
    public Film create(Film film) {
        validate(film);
        film.setId(counter++);
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film film) {
        validate(film);
        if (!films.containsKey(film.getId())) {
            throw new NotFoundException("Фильм не найден");
        }
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public Collection<Film> findAll() { return films.values(); }

    @Override
    public Optional<Film> findById(long id) { return Optional.ofNullable(films.get(id)); }

    @Override
    public void delete(long id) { films.remove(id); }

    private void validate(Film f) {
        if (f.getReleaseDate().isBefore(CINEMA_BIRTHDAY)) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895");
        }
    }
}
