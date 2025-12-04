package ru.practicum.filmorate.storage.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.practicum.filmorate.model.Mpa;
import ru.practicum.filmorate.storage.MpaStorage;
import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Mpa> findAll() {
        return jdbcTemplate.query(
                "SELECT id, name FROM mpa ORDER BY id",
                (rs, rowNum) -> new Mpa(rs.getInt("id"), rs.getString("name"))
        );
    }

    @Override
    public Optional<Mpa> findById(int id) {
        try {
            Mpa mpa = jdbcTemplate.queryForObject(
                    "SELECT id, name FROM mpa WHERE id = ?",
                    (rs, rowNum) -> new Mpa(rs.getInt("id"), rs.getString("name")),
                    id
            );
            return Optional.ofNullable(mpa);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
