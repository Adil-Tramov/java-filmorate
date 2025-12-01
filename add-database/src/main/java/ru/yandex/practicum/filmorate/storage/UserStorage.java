package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import java.util.Optional;

public interface UserStorage {
    List<User> findAll();
    Optional<User> findUserById(Long id);
    User create(User user);
    User update(User user);
    void delete(Long id);
    void addFriend(Long userId, Long friendId);
    void removeFriend(Long userId, Long friendId);
    List<User> getFriends(Long userId);
    List<User> getCommonFriends(Long userId, Long otherUserId);
}
