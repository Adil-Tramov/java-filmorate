package ru.yandex.javacourse.sprint10.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.javacourse.sprint10.exception.ValidationException;
import ru.yandex.javacourse.sprint10.model.User;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final List<User> users = new ArrayList<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @PostMapping
    public User createUser(@Valid @RequestBody User user) {
        user.setId(idGenerator.getAndIncrement());
        users.add(user);
        log.info("Создан пользователь: {}", user.getLogin());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                log.info("Обновлён пользователь с id={}", user.getId());
                return user;
            }
        }
        throw new ValidationException("Пользователь с id=" + user.getId() + " не найден");
    }

    @GetMapping
    public List<User> getAllUsers() {
        return new ArrayList<>(users);
    }
}
