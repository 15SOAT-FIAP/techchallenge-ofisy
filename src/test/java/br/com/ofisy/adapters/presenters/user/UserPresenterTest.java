package br.com.ofisy.adapters.presenters.user;

import br.com.ofisy.adapters.controllers.user.dto.UserResponseDTO;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserPresenterTest {

    private static final String VALID_EMAIL = "joao@ofisy.com";
    private static final String VALID_NAME = "João Silva";
    private static final String VALID_PASSWORD = "hashed-password";
    private static final Role VALID_ROLE = Role.ATTENDANT;

    @Nested
    class Present {

        @Test
        void shouldMapAllFieldsCorrectly() {
            var user = User.create(VALID_EMAIL, VALID_PASSWORD, VALID_NAME, VALID_ROLE);

            UserResponseDTO dto = UserPresenter.present(user);

            assertThat(dto).isNotNull();
            assertThat(dto.email()).isEqualTo(VALID_EMAIL);
            assertThat(dto.name()).isEqualTo(VALID_NAME);
            assertThat(dto.role()).isEqualTo(VALID_ROLE);
            assertThat(dto.active()).isTrue();
        }

        @Test
        void shouldSetCreatedAtFromNewUser() {
            var user = User.create(VALID_EMAIL, VALID_PASSWORD, VALID_NAME, VALID_ROLE);

            var dto = UserPresenter.present(user);

            assertThat(dto.createdAt()).isNotNull();
            assertThat(dto.updatedAt()).isNull();
        }

        @Test
        void shouldPreserveIdFromReconstructedUser() {
            var id = UUID.randomUUID();
            var createdAt = LocalDateTime.of(2024, 1, 10, 10, 0);
            var updatedAt = LocalDateTime.of(2024, 1, 15, 12, 0);
            var user = userDomain(id, createdAt, updatedAt);

            var dto = UserPresenter.present(user);

            assertThat(dto.id()).isEqualTo(id);
            assertThat(dto.createdAt()).isEqualTo(createdAt);
            assertThat(dto.updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        void shouldHaveNullIdForNewUser() {
            var user = User.create(VALID_EMAIL, VALID_PASSWORD, VALID_NAME, VALID_ROLE);

            var dto = UserPresenter.present(user);

            assertThat(dto.id()).isNull();
        }

        @Test
        void shouldMapAdminRoleCorrectly() {
            var user = User.create(VALID_EMAIL, VALID_PASSWORD, VALID_NAME, Role.ADMIN);

            var dto = UserPresenter.present(user);

            assertThat(dto.role()).isEqualTo(Role.ADMIN);
        }

        @Test
        void shouldReflectInactiveUser() {
            var id = UUID.randomUUID();
            var user = userDomain(id, LocalDateTime.now(), null);
            user.deactivate();

            var dto = UserPresenter.present(user);

            assertThat(dto.active()).isFalse();
        }
    }

    private User userDomain(UUID id, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return User.reconstruct(id, new br.com.ofisy.domain.user.Email(VALID_EMAIL),
                VALID_PASSWORD, VALID_NAME, VALID_ROLE, true, createdAt, updatedAt);
    }
}
