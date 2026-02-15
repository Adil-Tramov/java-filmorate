package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenresStorage;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Component("db-genres")
public class GenresDbStorage extends BaseDbStorage<Genre> implements GenresStorage {
    private static final String FIND_All_QUERY = "SELECT * FROM genres g";
    private static final String FIND_QUERY = "SELECT * FROM genres g WHERE id = ?";
    private static final String FIND_FILM_GENRES_QUERY = "SELECT * FROM genres g " +
            "WHERE id IN (SELECT genre_id FROM genres_on_films gof WHERE gof.film_id = ?)";
    private static final String INSERT_GENRES_QUERY = "INSERT INTO genres_on_films (genre_id, film_id) VALUES (?, ?)";
    private static final String FIND_ALL_EXISTING_IDS_QUERY = "SELECT id FROM genres WHERE id IN (%s)";

    public GenresDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Genre> findAll() {
        return findMany(FIND_All_QUERY);
    }

    @Override
    public Optional<Genre> find(Long id) {
        return findOne(
                FIND_QUERY,
                id
        );
    }

    @Override
    public Collection<Genre> getGenresForFilm(long filmId) {
        return findMany(
                FIND_FILM_GENRES_QUERY,
                filmId
        );
    }

    @Override
    public void setGenresForFilm(long filmId, Collection<Genre> genres) {
        genres.forEach(genre -> {
            update(
                    INSERT_GENRES_QUERY,
                    genre.getId(),
                    filmId
            );
        });
    }

    @Override
    public Set<Long> findAllExistingIds(Set<Long> genreIds) {
        if (genreIds == null || genreIds.isEmpty()) {
            return Set.of();
        }

        String inClause = String.join(",", Collections.nCopies(genreIds.size(), "?"));
        String sql = String.format(FIND_ALL_EXISTING_IDS_QUERY, inClause);

        log.debug("Выполняется запрос для проверки существования {} жанров", genreIds.size());

        return jdbc.query(sql, ps -> {
                    int i = 1;
                    for (Long genreId : genreIds) {
                        ps.setLong(i++, genreId);
                    }
                }, (rs, rowNum) -> rs.getLong("id"))
                .stream()
                .collect(Collectors.toSet());
    }
}