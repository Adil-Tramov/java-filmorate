package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(FilmController.class)
class FilmControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FilmService filmService;

    @Test
    void create_shouldReturn201WhenFilmIsValid() throws Exception {
        MpaRating mpa = new MpaRating(1, "G");
        Set<Genre> genres = new HashSet<>();
        genres.add(new Genre(1, "Комедия"));

        Film film = new Film(null, "Name", "Description", LocalDate.of(2000, 1, 1),
                100, mpa, genres, new HashSet<>());

        Film created = new Film(1L, "Name", "Description", LocalDate.of(2000, 1, 1),
                100, mpa, genres, new HashSet<>());

        when(filmService.create(any(Film.class))).thenReturn(created);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.mpa.id").value(1));
    }

    @Test
    void create_shouldReturn400WhenReleaseDateIsTooOld() throws Exception {
        MpaRating mpa = new MpaRating(1, "G");
        Film film = new Film(null, "Name", "Description", LocalDate.of(1890, 1, 1),
                100, mpa, new HashSet<>(), new HashSet<>());

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMostPopular_shouldReturnFilms() throws Exception {
        List<Film> popularFilms = Arrays.asList(
                new Film(1L, "Film1", "Desc1", LocalDate.now(), 100,
                        new MpaRating(1, "G"), new HashSet<>(), new HashSet<>()),
                new Film(2L, "Film2", "Desc2", LocalDate.now(), 100,
                        new MpaRating(2, "PG"), new HashSet<>(), new HashSet<>())
        );

        when(filmService.getMostPopularFilms(10)).thenReturn(popularFilms);

        mockMvc.perform(get("/films/popular"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }
}