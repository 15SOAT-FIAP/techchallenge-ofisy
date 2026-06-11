package br.com.ofisy.adapters.controllers.user;

import br.com.ofisy.adapters.controllers.user.dto.LoginRequestDTO;
import br.com.ofisy.application.user.login.LoginUseCase;
import br.com.ofisy.interfaces.api.ControllerTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LoginController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class LoginControllerTest extends ControllerTestBase {

    private static final String BASE_URL = "/api/v1/login";
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LoginUseCase loginUseCase;

    @Nested
    @DisplayName("POST /api/v1/login")
    class Login {

        @Test
        @DisplayName("Deve realizar login com sucesso e retornar token")
        void shouldLoginSuccessfullyAndReturnToken() throws Exception {
            var request = new LoginRequestDTO("admin@ofisy.com", "Admin@1234");
            when(loginUseCase.execute(any()))
                    .thenReturn(new LoginUseCase.LoginResult("mocked-jwt-token"));

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("mocked-jwt-token"));
        }

        @Test
        @DisplayName("Deve retornar 400 quando body está ausente")
        void shouldReturn400WhenBodyIsMissing() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 400 quando email está em branco")
        void shouldReturn400WhenEmailIsBlank() throws Exception {
            var body = """
                    {
                        "email": "",
                        "password": "Admin@1234"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 400 quando email é inválido")
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            var body = """
                    {
                        "email": "nao-e-um-email",
                        "password": "Admin@1234"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Deve retornar 400 quando password está em branco")
        void shouldReturn400WhenPasswordIsBlank() throws Exception {
            var body = """
                    {
                        "email": "admin@ofisy.com",
                        "password": ""
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }
    }
}
