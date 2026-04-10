package br.com.ofisy.application.auth.service;

import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfisyUserDetailsServiceTest {

    @Mock private UserRepository userRepository;

    @InjectMocks private OfisyUserDetailsService userDetailsService;

    @Test
    @DisplayName("Deve carregar usuário pelo email com sucesso")
    void shouldLoadUserByEmailSuccessfully() {
        String email = "joao@ofisy.com";
        User user = User.create(email, "hashed-password", "João Silva", Role.ATENDENTE);

        when(userRepository.findByEmailEmailAddress(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo(email);
        assertThat(userDetails.getPassword()).isEqualTo("hashed-password");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ATENDENTE");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não encontrado")
    void shouldThrowExceptionWhenUserNotFound() {
        String email = "naoexiste@ofisy.com";
        when(userRepository.findByEmailEmailAddress(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("Usuário não encontrado");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário está inativo")
    void shouldThrowExceptionWhenUserIsInactive() {
        String email = "joao@ofisy.com";
        User user = User.create(email, "hashed-password", "João Silva", Role.ATENDENTE);
        user.deactivate();

        when(userRepository.findByEmailEmailAddress(email)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("inativo");
    }

    @Test
    @DisplayName("Deve retornar authority correta para role ADMIN")
    void shouldReturnCorrectAuthorityForAdminRole() {
        String email = "admin@ofisy.com";
        User user = User.create(email, "hashed-password", "Admin", Role.ADMIN);

        when(userRepository.findByEmailEmailAddress(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ADMIN");
    }
}
