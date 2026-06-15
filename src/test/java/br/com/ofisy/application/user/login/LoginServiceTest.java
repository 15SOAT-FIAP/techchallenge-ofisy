package br.com.ofisy.application.user.login;

import br.com.ofisy.application.user.exceptions.EmailNotFoundException;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import br.com.ofisy.domain.user.exceptions.InactiveUserException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    private static final String VALID_EMAIL = "joao@ofisy.com";
    private static final String VALID_PASSWORD = "senha123";
    private static final String HASHED_PASSWORD = "hashed-senha123";
    private static final String GENERATED_TOKEN = "jwt-token-gerado";

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenGenerator tokenGenerator;

    @InjectMocks
    private LoginService service;

    @Nested
    class Execute {

        @Test
        void shouldReturnTokenWhenCredentialsAreValid() {
            var cmd = new LoginUseCase.LoginCommand(VALID_EMAIL, VALID_PASSWORD);
            var user = User.create(VALID_EMAIL, HASHED_PASSWORD, "João Silva", Role.ATTENDANT);
            when(repository.findByEmailAddress(VALID_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(VALID_PASSWORD, HASHED_PASSWORD)).thenReturn(true);
            when(tokenGenerator.generateToken(VALID_EMAIL)).thenReturn(GENERATED_TOKEN);

            var result = service.execute(cmd);

            assertThat(result).isEqualTo(GENERATED_TOKEN);
            verify(tokenGenerator).generateToken(VALID_EMAIL);
        }

        @Test
        void shouldThrowWhenEmailNotFound() {
            var cmd = new LoginUseCase.LoginCommand("naoexiste@ofisy.com", VALID_PASSWORD);
            when(repository.findByEmailAddress("naoexiste@ofisy.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(EmailNotFoundException.class);
        }

        @Test
        void shouldThrowWhenPasswordIsWrong() {
            var cmd = new LoginUseCase.LoginCommand(VALID_EMAIL, "senhaErrada");
            var user = User.create(VALID_EMAIL, HASHED_PASSWORD, "João Silva", Role.ATTENDANT);
            when(repository.findByEmailAddress(VALID_EMAIL)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("senhaErrada", HASHED_PASSWORD)).thenReturn(false);

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Senha atual incorreta");
        }

        @Test
        void shouldThrowDisabledExceptionWhenUserIsInactive() {
            var cmd = new LoginUseCase.LoginCommand(VALID_EMAIL, VALID_PASSWORD);
            var user = User.create(VALID_EMAIL, HASHED_PASSWORD, "João Silva", Role.ATTENDANT);
            user.deactivate();
            when(repository.findByEmailAddress(VALID_EMAIL)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(InactiveUserException.class)
                    .hasMessageContaining("Usuário inativo");
        }
    }
}