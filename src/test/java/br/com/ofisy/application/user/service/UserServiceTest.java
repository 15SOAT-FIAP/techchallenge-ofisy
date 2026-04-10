package br.com.ofisy.application.user.service;

import br.com.ofisy.application.user.dto.UserDTO.*;
import br.com.ofisy.application.user.exception.UserNotFoundException;
import br.com.ofisy.application.user.mapper.UserMapper;
import br.com.ofisy.application.user.service.UserService;
import br.com.ofisy.domain.user.Email;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository repository;
    @Mock private UserMapper mapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    private User mockUser() {
        return User.create("joao@ofisy.com", "hashed-password", "João Silva", Role.ATENDENTE);
    }

    private UserResponse mockUserResponse(UUID id) {
        return new UserResponse(id, "João Silva", new Email("joao@ofisy.com"), Role.ATENDENTE, true,
            LocalDateTime.now(), null);
    }

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void shouldCreateUserSuccessfully() {
        UUID id = UUID.randomUUID();
        CreateUserRequest request = new CreateUserRequest("João Silva", "joao@ofisy.com", "senha123", Role.ATENDENTE);
        User user = mockUser();
        UserResponse response = mockUserResponse(id);

        when(repository.existsByEmailEmailAddress(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(repository.save(any(User.class))).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.create(request);

        assertThat(result).isNotNull();
        assertThat(result.email().emailAddress()).isEqualTo("joao@ofisy.com");
        verify(repository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com email já existente")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        CreateUserRequest request = new CreateUserRequest("João Silva", "joao@ofisy.com", "senha123", Role.ATENDENTE);

        when(repository.existsByEmailEmailAddress(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email já cadastrado");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void shouldFindUserByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        UserResponse response = mockUserResponse(id);

        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.findById(id);

        assertThat(result).isNotNull();
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar usuário inexistente")
    void shouldThrowExceptionWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(id))
            .isInstanceOf(UserNotFoundException.class)
            .hasMessageContaining("Usuário com id " + id + " não encontrado!");
    }

    @Test
    @DisplayName("Deve listar todos os usuários")
    void shouldListAllUsers() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        UserResponse response = mockUserResponse(id);

        when(repository.findAll()).thenReturn(List.of(user));
        when(mapper.toResponse(user)).thenReturn(response);

        List<UserResponse> result = userService.listAllUsers();

        assertThat(result).hasSize(1);
        verify(repository).findAll();
    }

    @Test
    @DisplayName("Deve alterar role do usuário com sucesso")
    void shouldModifyUserRoleSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        UserResponse response = mockUserResponse(id);
        ModifyUserRoleRequest request = new ModifyUserRoleRequest(Role.ADMIN);

        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.modifyUserRole(id, request);

        assertThat(result).isNotNull();
        verify(repository).save(user);
    }

    @Test
    @DisplayName("Deve atualizar senha com sucesso")
    void shouldUpdatePasswordSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        UserResponse response = mockUserResponse(id);
        UpdatePasswordRequest request = new UpdatePasswordRequest("senha123", "novaSenha123");

        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.currentPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.newPassword())).thenReturn("nova-senha-hash");
        when(repository.save(user)).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.updatePassword(id, request);

        assertThat(result).isNotNull();
        verify(repository).save(user);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar senha com senha atual incorreta")
    void shouldThrowExceptionWhenCurrentPasswordIsWrong() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        UpdatePasswordRequest request = new UpdatePasswordRequest("senhaErrada", "novaSenha123");

        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.currentPassword(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword(id, request))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Senha atual incorreta");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve desativar usuário com sucesso")
    void shouldDeactivateUserSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        UserResponse response = mockUserResponse(id);

        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.deactivateUser(id);

        assertThat(result).isNotNull();
        verify(repository).save(user);
    }

    @Test
    @DisplayName("Deve ativar usuário com sucesso")
    void shouldActivateUserSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        user.deactivate();
        UserResponse response = mockUserResponse(id);

        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.activateUser(id);

        assertThat(result).isNotNull();
        verify(repository).save(user);
    }
}