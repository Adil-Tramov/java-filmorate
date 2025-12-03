package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage implements GenreStorage {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Genre> genreRowMapper = (rs, rowNum) -> {
        Genre genre = new Genre();
        genre.setId(rs.getInt("id"));
        genre.setName(rs.getString("name"));
        return genre;
    };

    @Override
    public List<Genre> findAll() {
        String sql = "SELECT * FROM genres ORDER BY id";
        return jdbcTemplate.query(sql, genreRowMapper);
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        String sql = "SELECT * FROM genres WHERE id = ?";
        try {
            Genre genre = jdbcTemplate.queryForObject(sql, genreRowMapper, id);
            return Optional.ofNullable(genre);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
}
