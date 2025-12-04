package ru.practicum.filmorate.storage;

import ru.practicum.filmorate.model.User;
import java.util.List;
import java.util.Optional;

public interface UserStorage {

    User create(User user);

    User update(User user);

    Optional<User> findUserById(long id);

    List<User> findAll();

    void addFriend(long userId, long friendId);

    void removeFriend(long userId, long friendId);
}

