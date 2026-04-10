package br.com.ofisy.application.auth.service;

import br.com.ofisy.infrastructure.config.auth.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    @Mock private JwtProperties jwtProperties;

    @InjectMocks private JwtService jwtService;

    private static final String SECRET = "1111111111111111111111111111111111111111111111111111111111";
    private static final long EXPIRATION = 86400000L;

    private void mockProperties(long expiration) {
        when(jwtProperties.getSecret()).thenReturn(SECRET);
        when(jwtProperties.getExpiration()).thenReturn(expiration);
    }

    @Test
    @DisplayName("Deve gerar token JWT válido")
    void shouldGenerateValidToken() {
        mockProperties(EXPIRATION);
        String token = jwtService.generateToken("joao@ofisy.com");
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Deve extrair email do token corretamente")
    void shouldExtractEmailFromToken() {
        String email = "joao@ofisy.com";
        mockProperties(EXPIRATION);
        String token = jwtService.generateToken(email);

        String extractedEmail = jwtService.extractEmail(token);
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("Deve validar token válido")
    void shouldValidateValidToken() {
        mockProperties(EXPIRATION);
        String token = jwtService.generateToken("joao@ofisy.com");

        boolean isValid = jwtService.isValidToken(token);

        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("Deve invalidar token corrompido")
    void shouldInvalidateCorruptedToken() {
        boolean isValid = jwtService.isValidToken("token.invalido.aqui");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar token vazio")
    void shouldInvalidateEmptyToken() {
        boolean isValid = jwtService.isValidToken("");

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve gerar tokens diferentes para emails diferentes")
    void shouldGenerateDifferentTokensForDifferentEmails() {
        mockProperties(EXPIRATION);
        String token1 = jwtService.generateToken("joao@ofisy.com");
        String token2 = jwtService.generateToken("maria@ofisy.com");

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("Deve invalidar token expirado")
    void shouldInvalidateExpiredToken() {
        mockProperties(-1000L);

        String expiredToken = jwtService.generateToken("joao@ofisy.com");
        boolean isValid = jwtService.isValidToken(expiredToken);

        assertThat(isValid).isFalse();
    }
}