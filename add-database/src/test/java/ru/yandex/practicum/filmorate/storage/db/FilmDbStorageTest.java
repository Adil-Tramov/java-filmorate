package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD) // ИЗМЕНЕНО
class FilmDbStorageTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private FilmDbStorage filmStorage;

    @BeforeEach
    void setUp() {
        filmStorage = new FilmDbStorage(jdbcTemplate);
    }

    @Test
    void create_shouldCreateFilm() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Test Film");
        assertThat(created.getMpa().getId()).isEqualTo(1);
        assertThat(created.getGenres()).hasSize(2);
    }

    @Test
    void findById_shouldReturnFilmWithGenresAndMpa() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        Optional<Film> found = filmStorage.findFilmById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getMpa().getId()).isEqualTo(1);
        assertThat(found.get().getGenres()).hasSize(2);
        assertThat(found.get().getLikes()).isEmpty();
    }

    @Test
    void addLike_shouldAddLike() {
        Film film = createTestFilm();
        Film created = filmStorage.create(film);

        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "test@mail.ru", "login", "Name", LocalDate.of(2000, 1, 1));
        Long userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, "test@mail.ru");

        filmStorage.addLike(created.getId(), userId);

        Set<Long> likes = filmStorage.findFilmById(created.getId()).get().getLikes();
        assertThat(likes).contains(userId);
    }

    @Test
    void getMostPopularFilms_shouldReturnFilmsOrderedByLikes() {
        Film film1 = createTestFilm();
        Film film2 = createTestFilm();
        film2.setName("Second Film");

        Film created1 = filmStorage.create(film1);
        Film created2 = filmStorage.create(film2);

        // Добавляем лайки film1
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "test1@mail.ru", "login1", "Name1", LocalDate.of(2000, 1, 1));
        jdbcTemplate.update("INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)",
                "test2@mail.ru", "login2", "Name2", LocalDate.of(2000, 1, 1));
        Long user1Id = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, "test1@mail.ru");
        Long user2Id = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = ?", Long.class, "test2@mail.ru");

        filmStorage.addLike(created1.getId(), user1Id);
        filmStorage.addLike(created1.getId(), user2Id);

        List<Film> popular = filmStorage.getMostPopularFilms(10);
        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getId()).isEqualTo(created1.getId());
    }

    private Film createTestFilm() {
        MpaRating mpa = new MpaRating(1, "G");
        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1, "Комедия"));
        genres.add(new Genre(2, "Драма"));

        return new Film(null, "Test Film", "Description",
                LocalDate.of(2000, 1, 1), 120, mpa, genres, new HashSet<>());
    }
}
