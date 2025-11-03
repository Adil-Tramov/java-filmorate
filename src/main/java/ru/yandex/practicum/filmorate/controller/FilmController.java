package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.Collection;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage storage;
    private final FilmService service;

    @Autowired
    public FilmController(final FilmStorage storage, final FilmService service) {
        this.storage = storage;
        this.service = service;
    }

    @GetMapping
    public Collection<Film> all() {
        return storage.findAll();
    }

    @GetMapping("/{id}")
    public Film get(@PathVariable final long id) {
        return storage.findById(id)
                .orElseThrow(() -> new NotFoundException("Фильм не найден"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Film create(@Valid @RequestBody final Film film) {
        return storage.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody final Film film) {
        return storage.update(film);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable final long id,
                        @PathVariable final long userId) {
        service.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable final long id,
                           @PathVariable final long userId) {
        service.removeLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> popular(@RequestParam(defaultValue = "10") final int count) {
        return service.popular(count);
    }
}