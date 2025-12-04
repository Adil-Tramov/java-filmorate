package ru.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.filmorate.model.Mpa;
import ru.practicum.filmorate.storage.MpaStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final MpaStorage mpaStorage;

    public MpaController(MpaStorage mpaStorage) {
        this.mpaStorage = mpaStorage;
    }

    @GetMapping
    public List<Mpa> getAll() {
        return mpaStorage.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mpa> getById(@PathVariable int id) {
        return mpaStorage.findById(id)
                .map(mpa -> new ResponseEntity<Mpa>(mpa, HttpStatus.OK))
                .orElse(new ResponseEntity<Mpa>(HttpStatus.NOT_FOUND));
    }
}

