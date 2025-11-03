package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationTest {

    @Test
    void filmReleaseDateBoundary() {
        Film f = new Film();
        f.setName("n");
        f.setDescription("d");
        f.setDuration(100);
        f.setReleaseDate(LocalDate.of(1894, 12, 27));

        assertThatThrownBy(() -> validateFilm(f))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("28 декабря 1895");
    }

    @Test
    void userLoginWithSpace() {
        User u = new User();
        u.setEmail("a@b.ru");
        u.setLogin("login with space");
        u.setBirthday(LocalDate.of(2000, 1, 1));

        assertThatThrownBy(() -> validateUser(u))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("пробелы");
    }

    private void validateFilm(final Film f) {
        if (f.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895");
        }
    }

    private void validateUser(final User u) {
        if (u.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
    }
}
