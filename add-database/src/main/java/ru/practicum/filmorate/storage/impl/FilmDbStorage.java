package ru.practicum.filmorate.storage.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.filmorate.model.Film;
import ru.practicum.filmorate.model.Genre;
import ru.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate,
                         MpaDbStorage mpaStorage,
                         GenreDbStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO film (name, description, release_date, duration, mpa_id) VALUES (?,?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, film.getReleaseDate() == null ? null : Date.valueOf(film.getReleaseDate()));
            if (film.getDuration() == null) ps.setObject(4, null);
            else ps.setInt(4, film.getDuration());
            ps.setObject(5, film.getMpa() == null ? null : film.getMpa().getId());
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) throw new RuntimeException("БД не вернула id");
        film.setId(key.longValue());
        saveFilmGenres(film);
        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE film SET name=?, description=?, release_date=?, duration=?, mpa_id=? WHERE id=?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate() == null ? null : Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa() == null ? null : film.getMpa().getId(),
                film.getId());
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id=?", film.getId());
        saveFilmGenres(film);
        return film;
    }

    @Override
    public Optional<Film> findFilmById(long id) {
        try {
            Film film = jdbcTemplate.queryForObject(
                    "SELECT id, name, description, release_date, duration, mpa_id FROM film WHERE id=?",
                    (rs, rowNum) -> {
                        Film f = new Film();
                        f.setId(rs.getLong("id"));
                        f.setName(rs.getString("name"));
                        f.setDescription(rs.getString("description"));
                        Date d = rs.getDate("release_date");
                        if (d != null) f.setReleaseDate(d.toLocalDate());
                        int dur = rs.getInt("duration");
                        if (!rs.wasNull()) f.setDuration(dur);
                        int mpaId = rs.getInt("mpa_id");
                        if (!rs.wasNull()) mpaStorage.findById(mpaId).ifPresent(f::setMpa);
                        return f;
                    }, id);
            if (film != null) {
                List<Genre> genres = jdbcTemplate.query(
                        "SELECT g.id, g.name FROM genre g JOIN film_genre fg ON g.id = fg.genre_id WHERE fg.film_id=? ORDER BY g.id",
                        (rs, rn) -> new Genre(rs.getInt("id"), rs.getString("name")),
                        id);
                film.setGenres(new LinkedHashSet<>(genres));

                Set<Long> likes = new HashSet<>(jdbcTemplate.query(
                        "SELECT user_id FROM film_like WHERE film_id=?",
                        (rs, rn) -> rs.getLong("user_id"), id));
                film.setLikes(likes);
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> findAll() {
        List<Long> ids = jdbcTemplate.query("SELECT id FROM film", (rs, rn) -> rs.getLong("id"));
        return ids.stream()
                .map(id -> findFilmById(id).orElse(null))
                .filter(f -> f != null)
                .collect(Collectors.toList());
    }

    @Override
    public void addLike(long filmId, long userId) {
        jdbcTemplate.update("MERGE INTO film_like (film_id, user_id) KEY(film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbcTemplate.update("DELETE FROM film_like WHERE film_id=? AND user_id=?", filmId, userId);
    }

    private void saveFilmGenres(Film film) {
        if (film.getId() == null || film.getGenres() == null) return;
        LinkedHashSet<Genre> unique = new LinkedHashSet<>(film.getGenres());
        for (Genre g : unique) {
            jdbcTemplate.update("INSERT INTO film_genre (film_id, genre_id) VALUES (?, ?)", film.getId(), g.getId());
        }
    }
}