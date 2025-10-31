package ru.yandex.javacourse.sprint10;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldNotCreateFilmWithEmptyName() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"Test\",\"releaseDate\":\"2000-01-01\",\"duration\":100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Название фильма не может быть пустым"));
    }

    @Test
    void shouldNotCreateFilmWithInvalidReleaseDate() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"description\":\"Test\",\"releaseDate\":\"1890-01-01\",\"duration\":100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Дата релиза — не раньше 1895-12-28"));
    }

    @Test
    void shouldNotCreateFilmWithDescriptionTooLong() throws Exception {
        String longDesc = "a".repeat(201);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"description\":\"" + longDesc + "\",\"releaseDate\":\"2000-01-01\",\"duration\":100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.description").value("Максимальная длина описания — 200 символов"));
    }

    @Test
    void shouldNotCreateFilmWithNegativeDuration() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"description\":\"Test\",\"releaseDate\":\"2000-01-01\",\"duration\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.duration").value("Продолжительность фильма должна быть положительным числом"));
    }

    @Test
    void shouldReturnBadRequestOnEmptyBody() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Тело запроса не может быть пустым"));
    }
}