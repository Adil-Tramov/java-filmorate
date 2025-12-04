package ru.practicum.filmorate.storage.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.practicum.filmorate.model.Genre;
import ru.practicum.filmorate.storage.GenreStorage;
import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> findAll() {
        return jdbcTemplate.query(
                "SELECT id, name FROM genre ORDER BY id",
                (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name"))
        );
    }

    @Override
    public Optional<Genre> findById(int id) {
        try {
            Genre genre = jdbcTemplate.queryForObject(
                    "SELECT id, name FROM genre WHERE id = ?",
                    (rs, rowNum) -> new Genre(rs.getInt("id"), rs.getString("name")),
                    id
            );
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
