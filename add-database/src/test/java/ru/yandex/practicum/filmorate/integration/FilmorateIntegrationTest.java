package ru.yandex.practicum.filmorate.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {"/schema.sql", "/data.sql"}, executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
class FilmorateIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullScenario_shouldCreateUserAndFilmAndAddLike() throws Exception {
        User user = new User(null, "test@mail.ru", "login", "Name",
                LocalDate.of(2000, 1, 1), null);

        MvcResult userResult = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andReturn();

        User createdUser = objectMapper.readValue(userResult.getResponse().getContentAsString(), User.class);

        MpaRating mpa = new MpaRating(1, "G");
        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1, "Комедия"));

        Film film = new Film(null, "Film", "Description",
                LocalDate.of(2000, 1, 1), 100, mpa, genres, new HashSet<>());

        MvcResult filmResult = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andReturn();

        Film createdFilm = objectMapper.readValue(filmResult.getResponse().getContentAsString(), Film.class);

        mockMvc.perform(put("/films/" + createdFilm.getId() + "/like/" + createdUser.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/films/popular?count=1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(createdFilm.getId()))
                .andExpect(jsonPath("$[0].likes.length()").value(1));

        mockMvc.perform(delete("/films/" + createdFilm.getId() + "/like/" + createdUser.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void friendshipScenario_shouldAddAndRemoveFriend() throws Exception {
        User user1 = new User(null, "test1@mail.ru", "login1", "Name1",
                LocalDate.of(2000, 1, 1), null);
        User user2 = new User(null, "test2@mail.ru", "login2", "Name2",
                LocalDate.of(2000, 1, 1), null);

        MvcResult result1 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user1)))
                .andReturn();

        MvcResult result2 = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user2)))
                .andReturn();

        User createdUser1 = objectMapper.readValue(result1.getResponse().getContentAsString(), User.class);
        User createdUser2 = objectMapper.readValue(result2.getResponse().getContentAsString(), User.class);

        mockMvc.perform(put("/users/" + createdUser1.getId() + "/friends/" + createdUser2.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/" + createdUser1.getId() + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        mockMvc.perform(delete("/users/" + createdUser1.getId() + "/friends/" + createdUser2.getId()))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/" + createdUser1.getId() + "/friends"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }
}