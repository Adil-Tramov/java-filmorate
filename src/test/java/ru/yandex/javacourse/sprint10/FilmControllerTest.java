package ru.yandex.javacourse.sprint10;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").value("Название фильма не может быть пустым"));
    }

    @Test
    void shouldNotCreateFilmWithInvalidReleaseDate() throws Exception {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"description\":\"Test\",\"releaseDate\":\"2099-01-01\",\"duration\":100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.releaseDate").value("Дата релиза — не раньше 28 декабря 1895 года"));
    }
}