package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserService userService;

    @Test
    void create_shouldSetNameToLoginIfNameIsEmpty() {
        User user = new User(null, "test@mail.ru", "login", "",
                LocalDate.of(2000, 1, 1), null);

        when(userStorage.create(user)).thenReturn(user);

        User result = userService.create(user);

        assertThat(result.getName()).isEqualTo("login");
        verify(userStorage).create(user);
    }

    @Test
    void findById_shouldThrowExceptionIfUserNotFound() {
        when(userStorage.findUserById(anyLong())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь не найден");
    }

    @Test
    void getCommonFriends_shouldReturnCommonFriends() {
        User user1 = new User(1L, "test1@mail.ru", "login1", "Name1",
                LocalDate.of(2000, 1, 1), null);
        User user2 = new User(2L, "test2@mail.ru", "login2", "Name2",
                LocalDate.of(2000, 1, 1), null);
        User commonFriend = new User(3L, "test3@mail.ru", "login3", "Name3",
                LocalDate.of(2000, 1, 1), null);

        when(userStorage.getCommonFriends(1L, 2L)).thenReturn(Arrays.asList(commonFriend));

        List<User> result = userService.getCommonFriends(1L, 2L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(3L);
    }
}