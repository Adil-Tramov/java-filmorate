package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        int mpaId = rs.getInt("mpa_rating_id");
        if (!rs.wasNull()) {
            MpaRating mpa = new MpaRating();
            mpa.setId(mpaId);
            mpa.setName(rs.getString("mpa_name"));
            film.setMpa(mpa);
        }

        film.setLikes(new HashSet<>());
        film.setGenres(new HashSet<>());
        return film;
    };

    @Override
    public List<Film> findAll() {
        String sql = "SELECT f.*, mr.name as mpa_name FROM films f LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);

        loadGenresForFilms(films);
        loadLikesForFilms(films);

        return films;
    }

    @Override
    public Optional<Film> findFilmById(Long id) {
        String sql = "SELECT f.*, mr.name as mpa_name FROM films f LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id WHERE f.id = ?";
        try {
            Film film = jdbcTemplate.queryForObject(sql, filmRowMapper, id);
            if (film != null) {
                film.setGenres(getGenresForFilm(id));
                film.setLikes(getLikesForFilm(id));
            }
            return Optional.ofNullable(film);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public Film create(Film film) {
        if (film.getMpa() != null) {
            validateMpaExists(film.getMpa().getId());
        }

        validateGenresExist(film.getGenres());

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating_id) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null);

        String getIdSql = "SELECT id FROM films WHERE name = ? AND description = ?";
        Long id = jdbcTemplate.queryForObject(getIdSql, Long.class, film.getName(), film.getDescription());
        film.setId(id);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> uniqueGenres = new HashSet<>(film.getGenres());
            for (Genre genre : uniqueGenres) {
                String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
                jdbcTemplate.update(genreSql, film.getId(), genre.getId());
            }
            film.setGenres(uniqueGenres);
        }

        return findFilmById(id).orElseThrow(() -> new NotFoundException("Фильм не создан"));
    }

    @Override
    public Film update(Film film) {
        if (!findFilmById(film.getId()).isPresent()) {
            throw new NotFoundException("Фильм не найден с id: " + film.getId());
        }

        if (film.getMpa() != null) {
            validateMpaExists(film.getMpa().getId());
        }

        validateGenresExist(film.getGenres());

        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_rating_id = ? WHERE id = ?";
        jdbcTemplate.update(sql, film.getName(), film.getDescription(), film.getReleaseDate(), film.getDuration(),
                film.getMpa() != null ? film.getMpa().getId() : null, film.getId());

        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, film.getId());

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            Set<Genre> uniqueGenres = new HashSet<>(film.getGenres());
            for (Genre genre : uniqueGenres) {
                String genreSql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
                jdbcTemplate.update(genreSql, film.getId(), genre.getId());
            }
            film.setGenres(uniqueGenres);
        }

        return findFilmById(film.getId()).orElseThrow(() -> new NotFoundException("Фильм не найден"));
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
        String sql = "SELECT f.*, mr.name as mpa_name, COUNT(l.user_id) as like_count " +
                "FROM films f " +
                "LEFT JOIN mpa_ratings mr ON f.mpa_rating_id = mr.id " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa_rating_id, mr.name " +
                "ORDER BY like_count DESC, f.id ASC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);
        loadGenresForFilms(films);
        loadLikesForFilms(films);

        return films;
    }

    private Set<Genre> getGenresForFilm(Long filmId) {
        String sql = "SELECT g.id, g.name FROM genres g JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?";
        List<Genre> genres = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            return genre;
        }, filmId);
        return new HashSet<>(genres);
    }

    private Set<Long> getLikesForFilm(Long filmId) {
        String sql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getLong("user_id"), filmId);
        return new HashSet<>(likes);
    }

    private void loadGenresForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        String filmIds = films.stream().map(f -> f.getId().toString()).collect(Collectors.joining(","));
        String sql = "SELECT fg.film_id, g.id, g.name FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.id " +
                "WHERE fg.film_id IN (" + filmIds + ")";

        Map<Long, Set<Genre>> filmGenres = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            filmGenres.computeIfAbsent(filmId, k -> new HashSet<>()).add(genre);
        });

        films.forEach(f -> f.setGenres(filmGenres.getOrDefault(f.getId(), new HashSet<>())));
    }

    private void loadLikesForFilms(List<Film> films) {
        if (films.isEmpty()) return;

        String filmIds = films.stream().map(f -> f.getId().toString()).collect(Collectors.joining(","));
        String sql = "SELECT film_id, user_id FROM likes WHERE film_id IN (" + filmIds + ")";

        Map<Long, Set<Long>> filmLikes = new HashMap<>();
        jdbcTemplate.query(sql, rs -> {
            Long filmId = rs.getLong("film_id");
            Long userId = rs.getLong("user_id");
            filmLikes.computeIfAbsent(filmId, k -> new HashSet<>()).add(userId);
        });

        films.forEach(f -> f.setLikes(filmLikes.getOrDefault(f.getId(), new HashSet<>())));
    }

    private void validateMpaExists(Integer mpaId) {
        if (mpaId == null) return;
        String sql = "SELECT COUNT(*) FROM mpa_ratings WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, mpaId);
        if (count == null || count == 0) {
            throw new NotFoundException("Рейтинг MPA не найден с id: " + mpaId);
        }
    }

    private void validateGenresExist(Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) return;

        List<Integer> genreIds = genres.stream().map(Genre::getId).collect(Collectors.toList());
        String sql = "SELECT COUNT(*) FROM genres WHERE id IN (" +
                genreIds.stream().map(String::valueOf).collect(Collectors.joining(",")) + ")";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);

        if (count == null || count != genres.size()) {
            throw new NotFoundException("Один или несколько жанров не найдены");
        }
    }
}