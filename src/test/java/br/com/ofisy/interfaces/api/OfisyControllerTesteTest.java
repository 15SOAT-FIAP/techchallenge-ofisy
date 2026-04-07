package br.com.ofisy.interfaces.api;

import br.com.ofisy.interfaces.api.OfisyControllerTeste;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OfisyControllerTeste.class)
class OfisyControllerTesteTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    void getGreetingMessage_shouldReturnWelcomeMessage() throws Exception {
        mockMvc.perform(get("/api/teste/hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Bem vindo ao Ofisy!"));
    }
}