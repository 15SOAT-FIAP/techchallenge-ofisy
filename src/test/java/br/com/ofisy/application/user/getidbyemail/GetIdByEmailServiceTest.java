package br.com.ofisy.application.user.getidbyemail;

import br.com.ofisy.application.user.exceptions.EmailNotFoundException;
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
class GetIdByEmailServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private GetIdByEmailService service;

    @Nested
    class Execute {

        @Test
        void shouldReturnUserIdWhenEmailFound() {
            var email = "joao@ofisy.com";
            var user = User.create(email, "hashed", "João Silva", Role.ATTENDANT);
            when(repository.findByEmailAddress(email)).thenReturn(Optional.of(user));

            var result = service.execute(email);

            assertThat(result).isEqualTo(user.getId());
            verify(repository).findByEmailAddress(email);
        }

        @Test
        void shouldThrowEmailNotFoundExceptionWhenEmailNotFound() {
            var email = "naoexiste@ofisy.com";
            when(repository.findByEmailAddress(email)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(email))
                    .isInstanceOf(EmailNotFoundException.class);
        }
    }
}
