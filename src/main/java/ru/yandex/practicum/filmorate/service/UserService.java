package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserStorage storage;

    @Autowired
    public UserService(final UserStorage storage) {
        this.storage = storage;
    }

    public void addFriend(final long userId, final long friendId) {
        User user = get(userId);
        User friend = get(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(final long userId, final long friendId) {
        User user = get(userId);
        User friend = get(friendId);
        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public Collection<User> friends(final long userId) {
        return get(userId).getFriends()
                .stream()
                .map(storage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public Collection<User> commonFriends(final long userId, final long otherId) {
        Set<Long> userFriends = get(userId).getFriends();
        Set<Long> otherFriends = get(otherId).getFriends();
        return userFriends.stream()
                .filter(otherFriends::contains)
                .map(storage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    private User get(final long id) {
        return storage.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }
}