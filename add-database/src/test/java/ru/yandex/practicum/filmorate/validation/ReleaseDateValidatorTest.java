package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class ReleaseDateValidatorTest {
    private final ReleaseDateValidator validator = new ReleaseDateValidator();
    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @Test
    void isValid_shouldReturnTrueForDateAfter1895() {
        assertThat(validator.isValid(LocalDate.of(1896, 1, 1), context)).isTrue();
    }

    @Test
    void isValid_shouldReturnTrueFor1895_12_28() {
        assertThat(validator.isValid(LocalDate.of(1895, 12, 28), context)).isTrue();
    }

    @Test
    void isValid_shouldReturnFalseForDateBefore1895() {
        assertThat(validator.isValid(LocalDate.of(1894, 1, 1), context)).isFalse();
    }

    @Test
    void isValid_shouldReturnTrueForNullDate() {
        assertThat(validator.isValid(null, context)).isTrue();
    }
}