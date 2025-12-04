package ru.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.practicum.filmorate.model.Genre;
import ru.practicum.filmorate.storage.impl.GenreDbStorage;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
public class GenreDbStorageTest {

    @Autowired
    private GenreDbStorage genreDbStorage;

    @Test
    void shouldFindAllGenres() {
        List<Genre> genres = genreDbStorage.findAll();
        assertThat(genres).hasSize(6);
    }

    @Test
    void shouldFindGenreById() {
        Optional<Genre> genre = genreDbStorage.findById(1);
        assertThat(genre).isPresent();
        assertThat(genre.get().getName()).isEqualTo("Комедия");
    }
}
