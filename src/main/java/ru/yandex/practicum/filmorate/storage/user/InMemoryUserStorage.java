package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();
    private long counter = 1;

    @Override
    public User create(User user) {
        validate(user);
        user.setId(counter++);
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public User update(User user) {
        validate(user);
        if (!users.containsKey(user.getId())) {
            throw new NotFoundException("Пользователь не найден");
        }
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public Collection<User> findAll() { return users.values(); }

    @Override
    public Optional<User> findById(long id) { return Optional.ofNullable(users.get(id)); }

    @Override
    public void delete(long id) { users.remove(id); }

    private void validate(User u) {
        if (u.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может содержать пробелы");
        }
        if (u.getName() == null || u.getName().isBlank()) {
            u.setName(u.getLogin());
        }
        if (u.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("День рождения не может быть в будущем");
        }
    }
}