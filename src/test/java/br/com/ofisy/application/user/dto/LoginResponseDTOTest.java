package br.com.ofisy.application.user.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginResponseDTOTest {

    @Test
    @DisplayName("Deve criar response com token")
    void shouldCreateResponseWithToken() {
        LoginResponseDTO dto = new LoginResponseDTO("token-valido-mockado");

        assertThat(dto.token()).isEqualTo("token-valido-mockado");
    }

    @Test
    @DisplayName("Deve criar response com token nulo")
    void shouldCreateResponseWithNullToken() {
        LoginResponseDTO dto = new LoginResponseDTO(null);

        assertThat(dto.token()).isNull();
    }
}