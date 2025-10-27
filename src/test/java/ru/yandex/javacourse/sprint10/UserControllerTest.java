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
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldNotCreateUserWithInvalidEmail() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalid-email\",\"login\":\"login\",\"name\":\"name\",\"birthday\":\"2000-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Электронная почта должна быть валидной"));
    }

    @Test
    void shouldNotCreateUserWithFutureBirthday() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"valid@example.com\",\"login\":\"login\",\"name\":\"name\",\"birthday\":\"2099-01-01\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.birthday").value("Дата рождения не может быть в будущем"));
    }
}
