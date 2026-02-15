package ru.yandex.practicum.filmorate.dal;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Likes;
import ru.yandex.practicum.filmorate.storage.LikesStorage;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component("db-likes")
public class LikesDbStorage extends BaseDbStorage<Likes> implements LikesStorage {
    private static final String FIND_LIKES_ON_FILM_QUERY = "SELECT * FROM likes_on_films WHERE film_id = ?";
    private static final String INSERT_QUERY = "INSERT INTO likes_on_films (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_QUERY = "DELETE FROM likes_on_films WHERE film_id = ? AND user_id = ?";
    private static final String FIND_LIKES_COUNT_FOR_FILMS_QUERY =
            "SELECT film_id, COUNT(user_id) as likes_count FROM likes_on_films " +
                    "WHERE film_id IN (%s) GROUP BY film_id";

    public LikesDbStorage(JdbcTemplate jdbc, RowMapper<Likes> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Likes> getLikesOnFilm(long filmId) {
        return findMany(
                FIND_LIKES_ON_FILM_QUERY,
                filmId
        );
    }

    @Override
    public void addLikeOnFilm(Long filmId, Long userId) {
        update(
                INSERT_QUERY,
                filmId,
                userId
        );
    }

    @Override
    public void removeLikeOnFilm(Long filmId, Long userId) {
        update(
                DELETE_QUERY,
                filmId,
                userId
        );
    }

    @Override
    public Map<Long, Integer> getLikesCountForFilms(Set<Long> filmIds) {
        if (filmIds == null || filmIds.isEmpty()) {
            return Map.of();
        }

        String inClause = String.join(",", Collections.nCopies(filmIds.size(), "?"));
        String sql = String.format(FIND_LIKES_COUNT_FOR_FILMS_QUERY, inClause);

        log.debug("Выполняется запрос для получения количества лайков для {} фильмов", filmIds.size());

        return jdbc.query(sql, ps -> {
            int i = 1;
            for (Long filmId : filmIds) {
                ps.setLong(i++, filmId);
            }
        }, (rs, rowNum) -> {
            long filmId = rs.getLong("film_id");
            int likesCount = rs.getInt("likes_count");
            return new AbstractMap.SimpleEntry<>(filmId, likesCount);
        }).stream().collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}


