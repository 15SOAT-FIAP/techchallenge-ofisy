package br.com.ofisy.interfaces.api.user;

import br.com.ofisy.application.user.dto.LoginRequestDTO;
import br.com.ofisy.interfaces.api.ControllerTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = LoginController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class LoginControllerTest extends ControllerTestBase {

    public static final String BASE_URL = "/api/v1/login";
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Test
    @DisplayName("Deve realizar login com sucesso e retornar token")
    void shouldLoginSuccessfullyAndReturnToken() throws Exception {
        LoginRequestDTO request = new LoginRequestDTO("admin@ofisy.com", "Admin@1234");
        Authentication auth = new UsernamePasswordAuthenticationToken("admin@ofisy.com", null, List.of());

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generateToken("admin@ofisy.com")).thenReturn("mocked-jwt-token");

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

    private final ObjectMapper objectMapper = new ObjectMapper();
}
