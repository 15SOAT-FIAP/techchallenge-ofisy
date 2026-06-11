package br.com.ofisy.application.user.findbyid;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindUserByIdServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private FindUserByIdService service;

    @Nested
    class Execute {

        @Test
        void shouldReturnUserWhenFound() {
            var id = UUID.randomUUID();
            var user = User.create("joao@ofisy.com", "hashed", "João Silva", Role.ATTENDANT);
            when(repository.findById(id)).thenReturn(Optional.of(user));

            var result = service.execute(id);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("João Silva");
            assertThat(result.getEmail().emailAddress()).isEqualTo("joao@ofisy.com");
            verify(repository).findById(id);
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
