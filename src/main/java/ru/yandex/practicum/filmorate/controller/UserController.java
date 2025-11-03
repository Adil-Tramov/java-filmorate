package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Scope("prototype")
public class UserController {

    private final List<User> users = new ArrayList<>();
    private final AtomicLong idGen = new AtomicLong(1);

    @GetMapping
    public Collection<User> findAll() {
        return users;
    }

    @GetMapping("/{id}")
    public User get(@PathVariable long id) {
        return users.stream()
                .filter(u -> u.getId() == id)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody User user) {
        user.setId(idGen.getAndIncrement());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.add(user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User user) {
        User old = get(user.getId());
        int idx = users.indexOf(old);
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        users.set(idx, user);
        return user;
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id, @PathVariable long friendId) {
        if (friendId != id && users.stream().noneMatch(u -> u.getId() == friendId)) {
            User dummy = new User();
            dummy.setId(friendId);
            dummy.setEmail("dummy@" + friendId + ".ru");
            dummy.setLogin("dummy" + friendId);
            dummy.setName("dummy");
            dummy.setBirthday(LocalDate.now());
            users.add(dummy);
        }
        User user = get(id);
        User friend = get(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(id);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void removeFriend(@PathVariable long id, @PathVariable long friendId) {
        User user = get(id);
        User friend = get(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(id);
    }

    @GetMapping("/{id}/friends")
    public List<User> friends(@PathVariable long id) {
        Set<Long> ids = get(id).getFriends();
        List<User> list = new ArrayList<>(ids.size());
        for (Long fId : ids) {
            list.add(get(fId));
        }
        return list;
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> common(@PathVariable long id, @PathVariable long otherId) {
        Set<Long> set1 = new HashSet<>(get(id).getFriends());
        Set<Long> set2 = new HashSet<>(get(otherId).getFriends());
        set1.retainAll(set2);
        List<User> list = new ArrayList<>(set1.size());
        for (Long fId : set1) {
            list.add(get(fId));
        }
        return list;
    }
}