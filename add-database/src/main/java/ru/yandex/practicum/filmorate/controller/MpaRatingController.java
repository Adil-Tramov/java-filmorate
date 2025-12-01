package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.MpaRatingService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@RequiredArgsConstructor
public class MpaRatingController {
    private final MpaRatingService mpaRatingService;

    @GetMapping
    public List<MpaRating> findAll() {
        return mpaRatingService.findAll();
    }

    @GetMapping("/{id}")
    public MpaRating findById(@PathVariable Integer id) {
        MpaRating rating = mpaRatingService.findById(id);
        if (rating == null) {
            throw new RuntimeException("MPA rating not found with id: " + id);
        }
        return rating;
    }
}
