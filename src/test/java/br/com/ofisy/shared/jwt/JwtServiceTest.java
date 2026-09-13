package br.com.ofisy.shared.jwt;

import br.com.ofisy.config.jwt.JwtProperties;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    public static final String TEST_USER_PRINCIPAL_EMAIL = "joao@ofisy.com";
    public static final String SECRET = "1111111111111111111111111111111111111111111111111111111111";
    public static final long EXPIRATION = 86400000L;
    public static final String AUTH_LAMBDA_ISSUER = "techchallenge-ofisy-auth";
    public static final String CUSTOMER_ID = "a1b2c3d4-e5f6-7890-abcd-ef1234567801";

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

    @Test
    @DisplayName("Deve invalidar token de cliente emitido pelas lambdas de auth")
    void shouldInvalidateCustomerToken() {
        String customerToken = customerTokenFromAuthLambda(CUSTOMER_ID);

        boolean isValid = jwtService.isValidToken(customerToken);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Deve invalidar token sem issuer")
    void shouldInvalidateTokenWithoutIssuer() {
        var now = Instant.now();
        String tokenWithoutIssuer = Jwts.builder()
                .subject(TEST_USER_PRINCIPAL_EMAIL)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(EXPIRATION)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();

        boolean isValid = jwtService.isValidToken(tokenWithoutIssuer);

        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("Não deve extrair o subject de um token de cliente")
    void shouldNotExtractSubjectFromCustomerToken() {
        String customerToken = customerTokenFromAuthLambda(CUSTOMER_ID);

        assertThatThrownBy(() -> jwtService.extractEmail(customerToken))
                .isInstanceOf(JwtException.class);
    }

    /**
     * Reproduz o token emitido pelas lambdas de auth: mesmo secret, mas com o
     * issuer delas e o id do cliente no subject.
     */
    private String customerTokenFromAuthLambda(String customerId) {
        var now = Instant.now();
        return Jwts.builder()
                .issuer(AUTH_LAMBDA_ISSUER)
                .subject(customerId)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(EXPIRATION)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .compact();
    }

}