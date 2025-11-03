package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void createUser() throws Exception {
        User u = user("a@b.ru", "login", "name", LocalDate.of(2000,1,1));
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(u)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createUserFailValidation() throws Exception {
        User u = user("", "login", "name", LocalDate.of(2000,1,1));
        mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(u)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void friendsFlow() throws Exception {
        long id1 = createUser(user("u1@mail.ru", "u1", "U-One", LocalDate.of(1995,1,1)));
        long id2 = createUser(user("u2@mail.ru", "u2", "U-Two", LocalDate.of(1995,2,2)));
        long id3 = createUser(user("u3@mail.ru", "u3", "U-Three", LocalDate.of(1995,3,3)));

        mvc.perform(put("/users/{id}/friends/{friendId}", id1, id2))
                .andExpect(status().isOk());

        mvc.perform(get("/users/{id}/friends", id1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value((int)id2));

        mvc.perform(put("/users/{id}/friends/{friendId}", id3, id2));

        mvc.perform(get("/users/{id}/friends/common/{otherId}", id1, id3))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value((int)id2));

        mvc.perform(delete("/users/{id}/friends/{friendId}", id1, id2))
                .andExpect(status().isOk());

        mvc.perform(get("/users/{id}/friends", id1))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getUnknownUser() throws Exception {
        mvc.perform(get("/users/{id}", 9999))
                .andExpect(status().isNotFound());
    }

    private User user(String email, String login, String name, LocalDate birthday) {
        User u = new User();
        u.setEmail(email);
        u.setLogin(login);
        u.setName(name);
        u.setBirthday(birthday);
        return u;
    }

    private long createUser(User u) throws Exception {
        String resp = mvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(u)))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return mapper.readTree(resp).get("id").asLong();
    }
}