package br.com.ofisy.infrastructure.config.security;

import br.com.ofisy.shared.jwt.JwtProperties;
import br.com.ofisy.shared.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
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

    public static final String TEST_USER_PRINCIPAL_EMAIL = "joao@ofisy.com";
    public static final String SECRET = "1111111111111111111111111111111111111111111111111111111111";
    public static final long EXPIRATION = 86400000L;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        lenient().when(jwtProperties.getSecret()).thenReturn(SECRET);
        lenient().when(jwtProperties.getExpiration()).thenReturn(EXPIRATION);
    }

    @Test
    @DisplayName("Deve gerar token JWT válido")
    void shouldGenerateValidToken() {
        String token = jwtService.generateToken(TEST_USER_PRINCIPAL_EMAIL);
        assertThat(token).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("Deve extrair email do token corretamente")
    void shouldExtractEmailFromToken() {
        String email = TEST_USER_PRINCIPAL_EMAIL;
        String token = jwtService.generateToken(email);

        String extractedEmail = jwtService.extractEmail(token);
        assertThat(extractedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("Deve validar token válido")
    void shouldValidateValidToken() {
        String token = jwtService.generateToken(TEST_USER_PRINCIPAL_EMAIL);

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
        String token1 = jwtService.generateToken(TEST_USER_PRINCIPAL_EMAIL);
        String token2 = jwtService.generateToken("maria@ofisy.com");

        assertThat(token1).isNotEqualTo(token2);
    }

    @Test
    @DisplayName("Deve invalidar token expirado")
    void shouldInvalidateExpiredToken() {
        when(jwtProperties.getExpiration()).thenReturn(-1000L);
        String expiredToken = jwtService.generateToken(TEST_USER_PRINCIPAL_EMAIL);
        boolean isValid = jwtService.isValidToken(expiredToken);
        assertThat(isValid).isFalse();
    }

}