package ru.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.practicum.filmorate.model.Mpa;
import ru.practicum.filmorate.storage.impl.MpaDbStorage;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
public class MpaDbStorageTest {

    @Autowired
    private MpaDbStorage mpaDbStorage;

    @Test
    void shouldFindAllMpa() {
        List<Mpa> all = mpaDbStorage.findAll();
        assertThat(all).hasSize(5);
    }

    @Test
    void shouldFindMpaById() {
        Optional<Mpa> mpa = mpaDbStorage.findById(1);
        assertThat(mpa).isPresent();
        assertThat(mpa.get().getName()).isEqualTo("G");
    }
}
