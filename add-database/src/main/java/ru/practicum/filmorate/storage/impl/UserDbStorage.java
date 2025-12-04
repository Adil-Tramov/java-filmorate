package ru.practicum.filmorate.storage.impl;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.practicum.filmorate.model.User;
import ru.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, user.getBirthday() == null ? null : Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) throw new RuntimeException("БД не вернула id");
        user.setId(key.longValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email=?, login=?, name=?, birthday=? WHERE id=?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday() == null ? null : Date.valueOf(user.getBirthday()),
                user.getId());
        return user;
    }

    @Override
    public Optional<User> findUserById(long id) {
        try {
            User user = jdbcTemplate.queryForObject(
                    "SELECT id, email, login, name, birthday FROM users WHERE id=?",
                    (rs, rowNum) -> {
                        User u = new User();
                        u.setId(rs.getLong("id"));
                        u.setEmail(rs.getString("email"));
                        u.setLogin(rs.getString("login"));
                        u.setName(rs.getString("name"));
                        Date d = rs.getDate("birthday");
                        if (d != null) u.setBirthday(d.toLocalDate());
                        return u;
                    }, id);
            if (user != null) {
                Set<Long> friends = new HashSet<>(jdbcTemplate.query(
                        "SELECT friend_id FROM user_friend WHERE user_id=?",
                        (rs, rn) -> rs.getLong("friend_id"), id));
                user.setFriends(friends);
            }
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<User> findAll() {
        List<User> users = jdbcTemplate.query(
                "SELECT id, email, login, name, birthday FROM users",
                (rs, rowNum) -> {
                    User u = new User();
                    u.setId(rs.getLong("id"));
                    u.setEmail(rs.getString("email"));
                    u.setLogin(rs.getString("login"));
                    u.setName(rs.getString("name"));
                    Date d = rs.getDate("birthday");
                    if (d != null) u.setBirthday(d.toLocalDate());
                    return u;
                });
        for (User u : users) {
            Set<Long> friends = new HashSet<>(jdbcTemplate.query(
                    "SELECT friend_id FROM user_friend WHERE user_id=?",
                    (rs, rn) -> rs.getLong("friend_id"), u.getId()));
            u.setFriends(friends);
        }
        return users;
    }

    /* ---------- взаимная дружба ---------- */
    @Override
    public void addFriend(long userId, long friendId) {
        jdbcTemplate.update(
                "MERGE INTO user_friend (user_id, friend_id) KEY(user_id, friend_id) VALUES (?, ?)",
                userId, friendId);
        jdbcTemplate.update(
                "MERGE INTO user_friend (user_id, friend_id) KEY(user_id, friend_id) VALUES (?, ?)",
                friendId, userId);
    }

    @Override
    public void removeFriend(long userId, long friendId) {
        jdbcTemplate.update("DELETE FROM user_friend WHERE user_id=? AND friend_id=?", userId, friendId);
        jdbcTemplate.update("DELETE FROM user_friend WHERE user_id=? AND friend_id=?", friendId, userId);
    }
}
