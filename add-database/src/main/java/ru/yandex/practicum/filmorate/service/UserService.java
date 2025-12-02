package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        Optional<User> user = userStorage.findUserById(id);
        if (user.isEmpty()) {
            throw new RuntimeException("User not found with id: " + id);
        }
        return user.get();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User user) {
        Optional<User> existingUser = userStorage.findUserById(user.getId());
        if (existingUser.isEmpty()) {
            throw new RuntimeException("User not found with id: " + user.getId());
        }
        return userStorage.update(user);
    }

    public void addFriend(Long userId, Long friendId) {
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        return userStorage.getCommonFriends(userId, otherUserId);
    }
}