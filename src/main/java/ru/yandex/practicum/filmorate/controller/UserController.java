package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserStorage storage;
    private final UserService service;

    @Autowired
    public UserController(final UserStorage storage, final UserService service) {
        this.storage = storage;
        this.service = service;
    }

    @GetMapping
    public Collection<User> all() {
        return storage.findAll();
    }

    @GetMapping("/{id}")
    public User get(@PathVariable final long id) {
        return storage.findById(id)
                .orElseThrow(() -> new ValidationException("Пользователь не найден"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody final User user) {
        return storage.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody final User user) {
        return storage.update(user);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable final long id,
                          @PathVariable final long friendId) {
        service.addFriend(id, friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable final long id,
                             @PathVariable final long friendId) {
        service.removeFriend(id, friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> friends(@PathVariable final long id) {
        return service.friends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> common(@PathVariable final long id,
                                   @PathVariable final long otherId) {
        return service.commonFriends(id, otherId);
    }
}