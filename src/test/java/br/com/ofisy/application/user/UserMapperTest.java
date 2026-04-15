package br.com.ofisy.application.user;

import br.com.ofisy.application.user.dto.UserResponseDTO;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    public static final String TEST_USER_PRINCIPAL_EMAIL = "joao@ofisy.com";
    public static final String TEST_USER_PRINCIPAL_NAME = "João Silva";
    public static final Role TEST_USER_PRINCIPAL_ROLE = Role.ATTENDANT;
    public static final String TEST_USER_PRINCIPAL_PASSWORD = "test-hashed-password";
    private final UserMapper mapper = new UserMapper();

    @Test
    @DisplayName("Deve mapear User para UserResponseDTO corretamente")
    void shouldMapUserToResponseDTO() {
        User user = User.create(TEST_USER_PRINCIPAL_EMAIL, TEST_USER_PRINCIPAL_PASSWORD, TEST_USER_PRINCIPAL_NAME, TEST_USER_PRINCIPAL_ROLE);

        UserResponseDTO response = mapper.toResponse(user);

        assertThat(response.name()).isEqualTo(TEST_USER_PRINCIPAL_NAME);
        assertThat(response.email().emailAddress()).isEqualTo(TEST_USER_PRINCIPAL_EMAIL);
        assertThat(response.role()).isEqualTo(TEST_USER_PRINCIPAL_ROLE);
        assertThat(response.active()).isTrue();
        assertThat(response.creationDate()).isNotNull();
        assertThat(response.modifiedDate()).isNull();
    }

    @Test
    @DisplayName("Deve mapear usuário inativo corretamente")
    void shouldMapInactiveUserCorrectly() {
        User user = User.create(TEST_USER_PRINCIPAL_EMAIL, TEST_USER_PRINCIPAL_PASSWORD, TEST_USER_PRINCIPAL_NAME, TEST_USER_PRINCIPAL_ROLE);
        user.deactivate();

        UserResponseDTO response = mapper.toResponse(user);

        assertThat(response.active()).isFalse();
        assertThat(response.modifiedDate()).isNotNull();
    }

    @Test
    @DisplayName("Deve mapear role corretamente")
    void shouldMapRoleCorrectly() {
        User user = User.create("admin@ofisy.com", "test-hashed-password-2", "Admin", Role.ADMIN);

        UserResponseDTO response = mapper.toResponse(user);

        assertThat(response.role()).isEqualTo(Role.ADMIN);
    }
}