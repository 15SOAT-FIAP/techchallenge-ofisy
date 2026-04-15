package br.com.ofisy.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    public static final String TEST_USER_PRINCIPAL_NAME = "João Silva";
    public static final String TEST_USER_PRINCIPAL_EMAIL = "joao@ofisy.com";
    public static final String TEST_USER_PRINCIPAL_PASSWORD = "senha-hash";
    public static final Role TEST_USER_PRINCIPAL_ROLE = Role.ATTENDANT;

    @Test
    @DisplayName("Deve criar usuário ativo por padrão")
    void shouldCreateActiveUser() {
        User user = createUser();
        assertThat(user.isActive()).isTrue();
        assertThat(user.getEmail().emailAddress()).isEqualTo(TEST_USER_PRINCIPAL_EMAIL);
        assertThat(user.getRole()).isEqualTo(TEST_USER_PRINCIPAL_ROLE);
        assertThat(user.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve desativar usuário ativo")
    void shouldDeactivateUser() {
        User user = createUser();
        user.deactivate();
        assertThat(user.isActive()).isFalse();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Deve lançar exceção ao desativar usuário já inativo")
    void shouldThrowExceptionAuthUserAlreadyDeactivated() {
        User user = createUser();
        user.deactivate();
        assertThatThrownBy(user::deactivate)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("já está desativado");
    }

    @Test
    @DisplayName("Deve ativar usuário inativo")
    void shouldActivateInativeAuthUser() {
        User user = createUser();
        user.deactivate();
        user.activate();
        assertThat(user.isActive()).isTrue();
    }

    @Test
    @DisplayName("Deve alterar role do usuário")
    void shouldModifyRule() {
        User user = createUser();
        user.modifyRole(Role.ADMIN);
        assertThat(user.getRole()).isEqualTo(Role.ADMIN);
        assertThat(user.isAdmin()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar authority correta para o Spring Security")
    void shouldReturnCorrectAuthority() {
        User user = createUser();
        assertThat(user.getAuthorities()).containsExactly("ROLE_ATTENDANT");
    }


    private User createUser() {
        return User.create(new Email(TEST_USER_PRINCIPAL_EMAIL).emailAddress(),TEST_USER_PRINCIPAL_PASSWORD, TEST_USER_PRINCIPAL_NAME, TEST_USER_PRINCIPAL_ROLE);
    }
}
