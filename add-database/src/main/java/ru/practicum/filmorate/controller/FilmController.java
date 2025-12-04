package ru.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.filmorate.model.Film;
import ru.practicum.filmorate.storage.FilmStorage;

import java.util.List;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmStorage filmStorage;

    public FilmController(FilmStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    @GetMapping
    public List<Film> getAll() {
        return filmStorage.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Film> getById(@PathVariable long id) {
        return filmStorage.findFilmById(id)
                .map(f -> new ResponseEntity<Film>(f, HttpStatus.OK))
                .orElse(new ResponseEntity<Film>(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<Film> create(@RequestBody Film film) {
        Film f = filmStorage.create(film);
        return new ResponseEntity<Film>(f, HttpStatus.CREATED);
    }

    @PutMapping
    public ResponseEntity<Film> update(@RequestBody Film film) {
        filmStorage.update(film);
        return new ResponseEntity<Film>(film, HttpStatus.OK);
    }

    @PutMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> addLike(@PathVariable long id, @PathVariable long userId) {
        filmStorage.addLike(id, userId);
        return new ResponseEntity<Void>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public ResponseEntity<Void> removeLike(@PathVariable long id, @PathVariable long userId) {
        filmStorage.removeLike(id, userId);
        return new ResponseEntity<Void>(HttpStatus.NO_CONTENT);
    }
}

