package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(UserController.class)
class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void create_shouldReturn201WhenUserIsValid() throws Exception {
        User user = new User(null, "test@mail.ru", "login", "Name",
                LocalDate.of(2000, 1, 1), null);
        User created = new User(1L, "test@mail.ru", "login", "Name",
                LocalDate.of(2000, 1, 1), null);

        when(userService.create(any(User.class))).thenReturn(created);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    void create_shouldReturn400WhenEmailIsInvalid() throws Exception {
        User user = new User(null, "invalid-email", "login", "Name",
                LocalDate.of(2000, 1, 1), null);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void findById_shouldReturn404WhenUserNotFound() throws Exception {
        when(userService.findById(anyLong())).thenThrow(new NotFoundException("User not found"));

        mockMvc.perform(get("/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Объект не найден"));
    }

    @Test
    void getFriends_shouldReturnFriendsList() throws Exception {
        List<User> friends = Arrays.asList(
                new User(2L, "friend1@mail.ru", "friend1", "Friend1",
                        LocalDate.of(2000, 1, 1), null),
                new User(3L, "friend2@mail.ru", "friend2", "Friend2",
                        LocalDate.of(2000, 1, 1), null)
        );

        when(userService.getFriends(anyLong())).thenReturn(friends);

        mockMvc.perform(get("/users/1/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}