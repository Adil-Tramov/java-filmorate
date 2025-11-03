package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;

    @Test
    void createFilm() throws Exception {
        Film f = film("Name", "Description", LocalDate.of(2000,1,1), 100);
        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(f)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void validationReleaseDate() throws Exception {
        Film f = film("Old", "Too old", LocalDate.of(1894,12,27), 60);
        mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(f)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void likesAndPopular() throws Exception {
        // Сначала создаем пользователей
        long user1 = createUser(user("user1@mail.ru", "user1", "User One", LocalDate.of(1990,1,1)));
        long user2 = createUser(user("user2@mail.ru", "user2", "User Two", LocalDate.of(1990,2,2)));
        long user3 = createUser(user("user3@mail.ru", "user3", "User Three", LocalDate.of(1990,3,3)));

        long film1 = createFilm(film("f1", "d1", LocalDate.of(2001,1,1), 90));
        long film2 = createFilm(film("f2", "d2", LocalDate.of(2002,2,2), 95));
        long film3 = createFilm(film("f3", "d3", LocalDate.of(2003,3,3), 100));

        like(film3, user1); like(film3, user2); like(film3, user3);
        like(film2, user1);

        mvc.perform(get("/films/popular?count=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value((int)film3))
                .andExpect(jsonPath("$[1].id").value((int)film2));

        unlike(film3, user3);

        mvc.perform(get("/films/popular?count=1"))
                .andExpect(jsonPath("$[0].id").value((int)film3));
    }

    @Test
    void likeUnknownFilm() throws Exception {
        long user1 = createUser(user("test@mail.ru", "testuser", "Test User", LocalDate.of(1990,1,1)));
        mvc.perform(put("/films/{id}/like/{userId}", 9999, user1))
                .andExpect(status().isNotFound());
    }

    private Film film(String name, String desc, LocalDate date, int duration) {
        Film f = new Film();
        f.setName(name);
        f.setDescription(desc);
        f.setReleaseDate(date);
        f.setDuration(duration);
        return f;
    }

    private long createFilm(Film f) throws Exception {
        String resp = mvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(f)))
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(resp).get("id").asLong();
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
                .andReturn().getResponse().getContentAsString();
        return mapper.readTree(resp).get("id").asLong();
    }

    private void like(long filmId, long userId) throws Exception {
        mvc.perform(put("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
    }

    private void unlike(long filmId, long userId) throws Exception {
        mvc.perform(delete("/films/{id}/like/{userId}", filmId, userId))
                .andExpect(status().isOk());
    }
}