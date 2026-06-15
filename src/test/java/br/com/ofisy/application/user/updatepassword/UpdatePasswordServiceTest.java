package br.com.ofisy.application.user.updatepassword;

import br.com.ofisy.application.user.exceptions.UserNotFoundException;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UpdatePasswordServiceTest {

    @Mock
    private UserRepository repository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UpdatePasswordService service;

    @Nested
    class Execute {

        @Test
        void shouldUpdatePasswordSuccessfully() {
            var id = UUID.randomUUID();
            var user = User.create("joao@ofisy.com", "hashed-old", "João Silva", Role.ATTENDANT);
            var cmd = new UpdatePasswordUseCase.UpdatePasswordCommand("oldPassword", "newPassword123");
            when(repository.findById(id)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("oldPassword", "hashed-old")).thenReturn(true);
            when(passwordEncoder.encode("newPassword123")).thenReturn("hashed-new");
            when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(id, cmd);

            assertThat(result.getPassword()).isEqualTo("hashed-new");
            verify(passwordEncoder).encode("newPassword123");
            verify(repository).save(user);
        }

        @Test
        void shouldThrowWhenCurrentPasswordIsWrong() {
            var id = UUID.randomUUID();
            var user = User.create("joao@ofisy.com", "hashed-old", "João Silva", Role.ATTENDANT);
            var cmd = new UpdatePasswordUseCase.UpdatePasswordCommand("wrongPassword", "newPassword123");
            when(repository.findById(id)).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongPassword", "hashed-old")).thenReturn(false);

            assertThatThrownBy(() -> service.execute(id, cmd))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Senha atual incorreta");
        }

        @Test
        void shouldThrowUserNotFoundExceptionWhenNotFound() {
            var id = UUID.randomUUID();
            var cmd = new UpdatePasswordUseCase.UpdatePasswordCommand("old", "new12345");
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(id, cmd))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
