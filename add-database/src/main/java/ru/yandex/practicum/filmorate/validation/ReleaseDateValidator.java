package ru.yandex.practicum.filmorate.validation;

import ru.yandex.practicum.filmorate.annotation.ReleaseDate;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class ReleaseDateValidator implements ConstraintValidator<ReleaseDate, LocalDate> {
    private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        return value == null || !value.isBefore(MIN_DATE);
    }
}