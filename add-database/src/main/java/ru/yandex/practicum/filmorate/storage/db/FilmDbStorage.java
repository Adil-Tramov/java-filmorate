package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Film> filmRowMapper = new RowMapper<Film>() {
        @Override
        public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getInt("mpa_rating_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);

            return film;
        }
    };

    @Override
    public List<Film> findAll() {
        String sql = "SELECT f.*, mr.name as mpa_name FROM films f LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        for (Film film : films) {
            Set<Genre> genres = getGenresForFilm(film.getId());
            film.setGenres(genres);

            Set<Long> likes = getLikesForFilm(film.getId());
            film.setLikes(likes);
        }

        return films;
    }

    @Override
    public Optional<Film> findFilmById(Long id) {
        String sql = "SELECT f.*, mr.name as mpa_name FROM films f LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            if (film != null) {
                Set<Genre> genres = getGenresForFilm(film.getId());
                film.setGenres(genres);

                Set<Long> likes = getLikesForFilm(film.getId());
                film.setLikes(likes);
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId());

        String getIdSql = "SELECT id FROM films WHERE name = ? AND description = ?";
        Long id = jdbcTemplate.queryForObject(getIdSql, Long.class, film.getName(), film.getDescription());
        film.setId(id);

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
                jdbcTemplate.update(genreSql, film.getId(), genre.getId());
            }
        }

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(), film.getMpa().getId(), film.getId());

        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, film.getId());

        if (film.getGenres() != null) {
            for (Genre genre : film.getGenres()) {
                String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
                jdbcTemplate.update(genreSql, film.getId(), genre.getId());
            }
        }

        return film;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getMostPopularFilms(int count) {
        String sql = "SELECT f.*, mr.name as mpa_name, COUNT(l.user_id) as like_count FROM films f LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id LEFT JOIN likes l ON f.id = l.film_id GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, mr.name ORDER BY like_count DESC LIMIT ?";
        List<Film> films = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film();
            film.setId(rs.getLong("id"));
            film.setName(rs.getString("name"));
            film.setDescription(rs.getString("description"));
            film.setReleaseDate(rs.getDate("release_date").toLocalDate());
            film.setDuration(rs.getInt("duration"));

            MpaRating mpa = new MpaRating();
            mpa.setId(rs.getInt("mpa_rating_id"));
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);

            return film;
        }, count);

        for (Film film : films) {
            Set<Genre> genres = getGenresForFilm(film.getId());
            film.setGenres(genres);

            Set<Long> likes = getLikesForFilm(film.getId());
            film.setLikes(likes);
        }

        return films;
    }

    private Set<Genre> getGenresForFilm(Long filmId) {
        String sql = "SELECT g.id, g.name FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, filmId).stream().collect(Collectors.toSet());
    }

    private Set<Long> getLikesForFilm(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"), filmId).stream().collect(Collectors.toSet());
    }
}