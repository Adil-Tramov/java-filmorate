package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private final RowMapper<User> userRowMapper = (rs, rowNum) -> {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setEmail(rs.getString("email"));
        user.setLogin(rs.getString("login"));
        user.setName(rs.getString("name"));
        user.setBirthday(rs.getDate("birthday").toLocalDate());
        return user;
    };

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public Optional<User> findUserById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, userRowMapper, id);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday());

        String getIdSql = "SELECT id FROM users WHERE email = ? AND login = ?";
        Long id = jdbcTemplate.queryForObject(getIdSql, Long.class, user.getEmail(), user.getLogin());
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int rowsAffected = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(),
                user.getName(), user.getBirthday(), user.getId());

        if (rowsAffected == 0) {
            throw new NotFoundException("Пользователь не найден с id: " + user.getId());
        }

        return user;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM users WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        findUserById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден с id: " + userId));
        findUserById(friendId).orElseThrow(() ->
                new NotFoundException("Пользователь не найден с id: " + friendId));

        String sql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
        jdbcTemplate.update(sql, friendId, userId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friendships WHERE (user_id = ? AND friend_id = ?) " +
                "OR (user_id = ? AND friend_id = ?)";
        jdbcTemplate.update(sql, userId, friendId, friendId, userId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        String sql = "SELECT u.* FROM users u JOIN friendships f ON u.id = f.friend_id WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherUserId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.id = f1.friend_id " +
                "JOIN friendships f2 ON u.id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, userRowMapper, userId, otherUserId);
    }
}