package br.com.ofisy.application.user.createuser;

import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import br.com.ofisy.domain.user.exceptions.EmailAlreadyExistsException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateUserServiceTest {

    private static final String VALID_EMAIL = "joao@ofisy.com";
    private static final String VALID_NAME = "João Silva";
    private static final String VALID_PASSWORD = "senha123";
    private static final String HASHED_PASSWORD = "hashed-senha123";
    private static final Role VALID_ROLE = Role.ATTENDANT;

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CreateUserService service;

    @Nested
    class Execute {

        @Test
        void shouldCreateUserAndReturnDomain() {
            var cmd = new CreateUserUseCase.CreateUserCommand(VALID_NAME, VALID_EMAIL, VALID_PASSWORD, VALID_ROLE);
            when(repository.existsByEmailAddress(VALID_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn(HASHED_PASSWORD);
            when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo(VALID_NAME);
            assertThat(result.getEmail().emailAddress()).isEqualTo(VALID_EMAIL);
            assertThat(result.getRole()).isEqualTo(VALID_ROLE);
            assertThat(result.isActive()).isTrue();
        }

        @Test
        void shouldEncodePasswordBeforeSaving() {
            var cmd = new CreateUserUseCase.CreateUserCommand(VALID_NAME, VALID_EMAIL, VALID_PASSWORD, VALID_ROLE);
            when(repository.existsByEmailAddress(VALID_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(VALID_PASSWORD)).thenReturn(HASHED_PASSWORD);
            when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result.getPassword()).isEqualTo(HASHED_PASSWORD);
            verify(passwordEncoder).encode(VALID_PASSWORD);
        }

        @Test
        void shouldThrowWhenEmailAlreadyExists() {
            var cmd = new CreateUserUseCase.CreateUserCommand(VALID_NAME, VALID_EMAIL, VALID_PASSWORD, VALID_ROLE);
            when(repository.existsByEmailAddress(VALID_EMAIL)).thenReturn(true);

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            verify(repository, never()).save(any());
        }

        @Test
        void shouldCallRepositorySaveOnce() {
            var cmd = new CreateUserUseCase.CreateUserCommand(VALID_NAME, VALID_EMAIL, VALID_PASSWORD, VALID_ROLE);
            when(repository.existsByEmailAddress(VALID_EMAIL)).thenReturn(false);
            when(passwordEncoder.encode(anyString())).thenReturn(HASHED_PASSWORD);
            when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            service.execute(cmd);

            verify(repository).save(any(User.class));
        }
    }
}
