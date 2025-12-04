package ru.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.filmorate.model.Genre;
import ru.practicum.filmorate.storage.GenreStorage;

import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {

    private final GenreStorage genreStorage;

    public GenreController(GenreStorage genreStorage) {
        this.genreStorage = genreStorage;
    }

    @GetMapping
    public List<Genre> getAll() {
        return genreStorage.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Genre> getById(@PathVariable int id) {
        return genreStorage.findById(id)
                .map(g -> new ResponseEntity<Genre>(g, HttpStatus.OK))
                .orElse(new ResponseEntity<Genre>(HttpStatus.NOT_FOUND));
    }
}



