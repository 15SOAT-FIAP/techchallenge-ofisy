package br.com.ofisy.application.user;

import br.com.ofisy.application.user.dto.*;
import br.com.ofisy.application.user.exception.UserNotFoundException;
import br.com.ofisy.domain.user.Email;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import br.com.ofisy.domain.user.exceptions.EmailAlreadyExistsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    public static final String TEST_USER_PRINCIPAL_EMAIL = "joao@ofisy.com";
    public static final String TEST_USER_PRINCIPAL_NAME = "João Silva";
    public static final String TEST_USER_PRINCIPAL_PASSWORD = "senha123";
    public static final Role TEST_USER_PRINCIPAL_ROLE = Role.ATTENDANT;

    @Mock private UserRepository repository;
    @Mock private UserMapper mapper;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserService userService;

    @Test
    @DisplayName("Deve criar usuário com sucesso")
    void shouldCreateUserSuccessfully() {
        UUID id = UUID.randomUUID();
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                TEST_USER_PRINCIPAL_NAME, TEST_USER_PRINCIPAL_EMAIL, TEST_USER_PRINCIPAL_PASSWORD, TEST_USER_PRINCIPAL_ROLE
        );
        User user = mockUser();
        UserResponseDTO response = mockResponse(id);

        when(repository.existsByEmailAddress(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("hashed-password");
        when(repository.save(any(User.class))).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponseDTO result = userService.create(request);

        assertThat(result).isNotNull();
        verify(repository).save(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao criar usuário com email já existente")
    void shouldThrowExceptionWhenEmailAlreadyExists() {
        CreateUserRequestDTO request = new CreateUserRequestDTO(
                TEST_USER_PRINCIPAL_NAME, TEST_USER_PRINCIPAL_EMAIL, TEST_USER_PRINCIPAL_PASSWORD, TEST_USER_PRINCIPAL_ROLE
        );

        when(repository.existsByEmailAddress(request.email())).thenReturn(true);

        assertThatThrownBy(() -> userService.create(request))
                .isInstanceOf(EmailAlreadyExistsException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve buscar usuário por ID com sucesso")
    void shouldFindUserByIdSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        UserResponseDTO response = mockResponse(id);

        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponseDTO result = userService.findById(id);

        assertThat(result).isNotNull();
        verify(repository).findById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao buscar usuário inexistente")
    void shouldThrowExceptionWhenUserNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(id))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("Deve listar todos os usuários paginados")
    void shouldListAllUsersPaginated() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        UserResponseDTO response = mockResponse(id);
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(List.of(user), pageable, 1);

        when(repository.findAll(pageable)).thenReturn(userPage);
        when(mapper.toResponse(user)).thenReturn(response);

        Page<UserResponseDTO> result = userService.listAllUsers(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(repository).findAll(pageable);
    }

    @Test
    @DisplayName("Deve retornar página vazia quando não há usuários")
    void shouldReturnEmptyPageWhenNoUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(repository.findAll(pageable)).thenReturn(emptyPage);

        Page<UserResponseDTO> result = userService.listAllUsers(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    @DisplayName("Deve alterar role do usuário com sucesso")
    void shouldModifyUserRoleSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        UserResponseDTO response = mockResponse(id);
        ModifyUserRoleRequestDTO request = new ModifyUserRoleRequestDTO(Role.ADMIN);

        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponseDTO result = userService.modifyUserRole(id, request);

        assertThat(result).isNotNull();
        verify(repository).save(user);
    }

    @Test
    @DisplayName("Deve atualizar senha com sucesso")
    void shouldUpdatePasswordSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        UserResponseDTO response = mockResponse(id);
        UpdatePasswordRequestDTO request = new UpdatePasswordRequestDTO(TEST_USER_PRINCIPAL_PASSWORD, "novaSenha123");

        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.currentPassword(), user.getPassword())).thenReturn(true);
        when(passwordEncoder.encode(request.newPassword())).thenReturn("nova-senha-hash");
        when(repository.save(user)).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponseDTO result = userService.updatePassword(id, request);

        assertThat(result).isNotNull();
        verify(repository).save(user);
    }

    @Test
    @DisplayName("Deve lançar exceção ao atualizar senha com senha atual incorreta")
    void shouldThrowExceptionWhenCurrentPasswordIsWrong() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        UpdatePasswordRequestDTO request = new UpdatePasswordRequestDTO("senhaErrada", "novaSenha123");

        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.currentPassword(), user.getPassword())).thenReturn(false);

        assertThatThrownBy(() -> userService.updatePassword(id, request))
                .isInstanceOf(IllegalArgumentException.class);

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve desativar usuário com sucesso")
    void shouldDeactivateUserSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        UserResponseDTO response = mockResponse(id);

        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponseDTO result = userService.deactivateUser(id);

        assertThat(result).isNotNull();
        verify(repository).save(user);
    }

    @Test
    @DisplayName("Deve ativar usuário com sucesso")
    void shouldActivateUserSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = mockUser();
        user.deactivate();
        UserResponseDTO response = mockResponse(id);

        when(repository.findById(id)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponseDTO result = userService.activateUser(id);

        assertThat(result).isNotNull();
        verify(repository).save(user);
    }

    @Test
    @DisplayName("Deve remover usuário com sucesso")
    void shouldRemoveUserSuccessfully() {
        UUID id = UUID.randomUUID();
        User user = mockUser();

        when(repository.findById(id)).thenReturn(Optional.of(user));

        userService.removeUser(id);

        verify(repository).deleteById(id);
    }

    @Test
    @DisplayName("Deve lançar exceção ao remover usuário inexistente")
    void shouldThrowExceptionWhenRemovingNonExistentUser() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.removeUser(id))
                .isInstanceOf(UserNotFoundException.class);

        verify(repository, never()).deleteById(any());
    }

    private User mockUser() {
        return User.create(TEST_USER_PRINCIPAL_EMAIL, "hashed-password", TEST_USER_PRINCIPAL_NAME, TEST_USER_PRINCIPAL_ROLE);
    }

    private UserResponseDTO mockResponse(UUID id) {
        return new UserResponseDTO(id, TEST_USER_PRINCIPAL_NAME, new Email(TEST_USER_PRINCIPAL_EMAIL),
                TEST_USER_PRINCIPAL_ROLE, true, LocalDateTime.now(), null);
    }
}