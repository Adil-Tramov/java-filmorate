package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.MpaRatingStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MpaRatingService {
    private final MpaRatingStorage mpaRatingStorage;

    public List<MpaRating> findAll() {
        return mpaRatingStorage.findAll();
    }

    public MpaRating findById(Integer id) {
        Optional<MpaRating> rating = mpaRatingStorage.findById(id);
        if (rating.isEmpty()) {
            return null;
        }
        return rating.get();
    }
}
