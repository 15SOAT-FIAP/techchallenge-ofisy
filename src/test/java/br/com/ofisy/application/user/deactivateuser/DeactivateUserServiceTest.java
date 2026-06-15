package br.com.ofisy.application.user.deactivateuser;

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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeactivateUserServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private DeactivateUserService service;

    @Nested
    class Execute {

        @Test
        void shouldDeactivateActiveUser() {
            var id = UUID.randomUUID();
            var user = User.create("joao@ofisy.com", "hashed", "João Silva", Role.ATTENDANT);
            when(repository.findById(id)).thenReturn(Optional.of(user));
            when(repository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(id);

            assertThat(result.isActive()).isFalse();
            verify(repository).save(user);
        }

        @Test
        void shouldThrowWhenUserAlreadyInactive() {
            var id = UUID.randomUUID();
            var user = User.create("joao@ofisy.com", "hashed", "João Silva", Role.ATTENDANT);
            user.deactivate();
            when(repository.findById(id)).thenReturn(Optional.of(user));

            assertThatThrownBy(() -> service.execute(id))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("já está desativado");
        }

        @Test
        void shouldThrowUserNotFoundExceptionWhenNotFound() {
            var id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(id))
                    .isInstanceOf(UserNotFoundException.class);
        }
    }
}
