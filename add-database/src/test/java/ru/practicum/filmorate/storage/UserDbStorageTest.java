package ru.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.practicum.filmorate.model.User;
import ru.practicum.filmorate.storage.impl.UserDbStorage;
import java.time.LocalDate;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
public class UserDbStorageTest {
    @Autowired
    private UserDbStorage userDbStorage;

    @Test
    void shouldCreateAndFindUser() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("test");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2000,1,1));

        userDbStorage.create(user);

        Optional<User> found = userDbStorage.findUserById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@mail.com");
    }
}

