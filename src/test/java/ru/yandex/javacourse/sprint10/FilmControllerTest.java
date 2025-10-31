package ru.yandex.javacourse.sprint10;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@AutoConfigureWebMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldNotCreateFilmWithEmptyName() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"description\":\"Test\",\"releaseDate\":\"2000-01-01\",\"duration\":100}"))
                .andExpect(status().is5xxServerError()) // Ожидаем 500 или 400, но ValidationException -> 500
                .andExpect(jsonPath("$.error").value("Название фильма не может быть пустым"));
    }

    @Test
    void shouldNotCreateFilmWithInvalidReleaseDate() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"description\":\"Test\",\"releaseDate\":\"1890-01-01\",\"duration\":100}"))
                .andExpect(status().is5xxServerError()) // Ожидаем 500 или 400
                .andExpect(jsonPath("$.error").value("Дата релиза — не раньше 1895-12-28"));
    }
}