package br.com.ofisy.domain.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserTest {

    private User createUser() {
        return User.create(new Email("joao@ofisy.com").emailAddress(), "senha-hash", "João Silva", Role.ATENDENTE);
    }

    @Test
    @DisplayName("Deve criar usuário ativo por padrão")
    void shouldCreateActiveUser() {
        User user = createUser();
        assertThat(user.isActive()).isTrue();
        assertThat(user.getEmail().emailAddress()).isEqualTo("joao@ofisy.com");
        assertThat(user.getRole()).isEqualTo(Role.ATENDENTE);
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
        assertThat(user.getAuthorities()).containsExactly("ROLE_ATENDENTE");
    }
}
