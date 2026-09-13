package br.com.ofisy.shared.securityfilter;

import br.com.ofisy.shared.jwt.JwtService;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    public static final String TEST_USER_PRINCIPAL_EMAIL = "joao@ofisy.com";
    public static final String CUSTOMER_TOKEN = "token-emitido-pela-lambda-de-auth";
    public static final String USER_TOKEN = "token-emitido-pela-api";

    @Mock private JwtService jwtService;
    @Mock private OfisyUserDetailsService userDetailsService;
    @Mock private FilterChain filterChain;

    @InjectMocks private JwtAuthFilter jwtAuthFilter;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Deve autenticar o usuário quando o token é da API")
    void shouldAuthenticateUserWhenTokenIsFromApi() throws Exception {
        MockHttpServletRequest request = requestWithBearer(USER_TOKEN);
        UserDetails userDetails = new User(
                TEST_USER_PRINCIPAL_EMAIL,
                "hashed-password",
                List.of(new SimpleGrantedAuthority("ROLE_ATTENDANT"))
        );

        when(jwtService.isValidToken(USER_TOKEN)).thenReturn(true);
        when(jwtService.extractEmail(USER_TOKEN)).thenReturn(TEST_USER_PRINCIPAL_EMAIL);
        when(userDetailsService.loadUserByUsername(TEST_USER_PRINCIPAL_EMAIL)).thenReturn(userDetails);

        jwtAuthFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo(TEST_USER_PRINCIPAL_EMAIL);
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    @DisplayName("Não deve procurar usuário quando o token é de cliente")
    void shouldNotLookUpUserWhenTokenBelongsToCustomer() throws Exception {
        MockHttpServletRequest request = requestWithBearer(CUSTOMER_TOKEN);

        when(jwtService.isValidToken(CUSTOMER_TOKEN)).thenReturn(false);

        jwtAuthFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(userDetailsService);
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    @DisplayName("Deve seguir a cadeia quando não há header Authorization")
    void shouldContinueChainWhenAuthorizationHeaderIsMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();

        jwtAuthFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userDetailsService);
        verify(filterChain).doFilter(any(), any());
    }

    @Test
    @DisplayName("Deve seguir a cadeia quando o header não tem o prefixo Bearer")
    void shouldContinueChainWhenHeaderHasNoBearerPrefix() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtAuthFilter.AUTHORIZATION, USER_TOKEN);

        jwtAuthFilter.doFilter(request, new MockHttpServletResponse(), filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verifyNoInteractions(jwtService, userDetailsService);
        verify(filterChain).doFilter(any(), any());
    }

    private static MockHttpServletRequest requestWithBearer(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(JwtAuthFilter.AUTHORIZATION, JwtAuthFilter.BEARER + token);
        return request;
    }
}