package ru.practicum.filmorate.storage;

import ru.practicum.filmorate.model.Genre;
import java.util.List;
import java.util.Optional;

public interface GenreStorage {

    List<Genre> findAll();

    Optional<Genre> findById(int id);
}

