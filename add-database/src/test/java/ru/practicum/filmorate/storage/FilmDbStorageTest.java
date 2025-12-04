package ru.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.practicum.filmorate.model.Film;
import ru.practicum.filmorate.model.Genre;
import ru.practicum.filmorate.model.Mpa;
import ru.practicum.filmorate.storage.impl.FilmDbStorage;
import ru.practicum.filmorate.storage.impl.GenreDbStorage;
import ru.practicum.filmorate.storage.impl.MpaDbStorage;
import java.time.LocalDate;
import java.util.Set;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
public class FilmDbStorageTest {

    @Autowired
    private FilmDbStorage filmDbStorage;

    @Test
    void shouldCreateAndFindFilm() {
        Film f = new Film();
        f.setName("Test film");
        f.setDescription("desc");
        f.setReleaseDate(LocalDate.of(2000, 1, 1));
        f.setDuration(120);
        f.setMpa(new Mpa(1, null));
        f.setGenres(Set.of(new Genre(1, null)));

        filmDbStorage.create(f);

        assertThat(f.getId()).isNotNull();

        Film loaded = filmDbStorage.findFilmById(f.getId()).orElseThrow();
        assertThat(loaded.getName()).isEqualTo("Test film");
        assertThat(loaded.getGenres()).hasSize(1);
        assertThat(loaded.getMpa().getId()).isEqualTo(1);
    }
}
