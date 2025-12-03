package ru.yandex.practicum.filmorate.storage.db;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD) // ИЗМЕНЕНО
class UserDbStorageTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    private UserDbStorage userStorage;

    @BeforeEach
    void setUp() {
        userStorage = new UserDbStorage(jdbcTemplate);
    }

    @Test
    void create_shouldCreateUser() {
        User user = new User(null, "test@mail.ru", "login", "Name",
                LocalDate.of(2000, 1, 1), null);
        User created = userStorage.create(user);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Name");
    }

    @Test
    void findById_shouldReturnUser() {
        User user = new User(null, "test@mail.ru", "login", "Name",
                LocalDate.of(2000, 1, 1), null);
        User created = userStorage.create(user);

        Optional<User> found = userStorage.findUserById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getLogin()).isEqualTo("login");
    }

    @Test
    void findById_shouldReturnEmptyForNonExistent() {
        Optional<User> found = userStorage.findUserById(999L);
        assertThat(found).isEmpty();
    }

    @Test
    void update_shouldUpdateUser() {
        User user = new User(null, "test@mail.ru", "login", "Name",
                LocalDate.of(2000, 1, 1), null);
        User created = userStorage.create(user);
        created.setName("Updated Name");

        User updated = userStorage.update(created);
        assertThat(updated.getName()).isEqualTo("Updated Name");
    }

    @Test
    void addFriend_shouldAddFriend() {
        User user1 = new User(null, "test1@mail.ru", "login1", "Name1",
                LocalDate.of(2000, 1, 1), null);
        User user2 = new User(null, "test2@mail.ru", "login2", "Name2",
                LocalDate.of(2000, 1, 1), null);

        User created1 = userStorage.create(user1);
        User created2 = userStorage.create(user2);

        userStorage.addFriend(created1.getId(), created2.getId());

        List<User> friends = userStorage.getFriends(created1.getId());
        assertThat(friends).hasSize(1);
        assertThat(friends.get(0).getId()).isEqualTo(created2.getId());
    }

    @Test
    void removeFriend_shouldRemoveFriend() {
        User user1 = new User(null, "test1@mail.ru", "login1", "Name1",
                LocalDate.of(2000, 1, 1), null);
        User user2 = new User(null, "test2@mail.ru", "login2", "Name2",
                LocalDate.of(2000, 1, 1), null);

        User created1 = userStorage.create(user1);
        User created2 = userStorage.create(user2);

        userStorage.addFriend(created1.getId(), created2.getId());
        userStorage.removeFriend(created1.getId(), created2.getId());

        List<User> friends = userStorage.getFriends(created1.getId());
        assertThat(friends).isEmpty();
    }

    @Test
    void getCommonFriends_shouldReturnCommonFriends() {
        User user1 = new User(null, "test1@mail.ru", "login1", "Name1",
                LocalDate.of(2000, 1, 1), null);
        User user2 = new User(null, "test2@mail.ru", "login2", "Name2",
                LocalDate.of(2000, 1, 1), null);
        User user3 = new User(null, "test3@mail.ru", "login3", "Name3",
                LocalDate.of(2000, 1, 1), null);

        User created1 = userStorage.create(user1);
        User created2 = userStorage.create(user2);
        User created3 = userStorage.create(user3);

        userStorage.addFriend(created1.getId(), created3.getId());
        userStorage.addFriend(created2.getId(), created3.getId());

        List<User> common = userStorage.getCommonFriends(created1.getId(), created2.getId());
        assertThat(common).hasSize(1);
        assertThat(common.get(0).getId()).isEqualTo(created3.getId());
    }
}