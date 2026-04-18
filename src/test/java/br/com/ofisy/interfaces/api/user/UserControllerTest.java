package br.com.ofisy.interfaces.api.user;

import br.com.ofisy.application.user.UserService;
import br.com.ofisy.application.user.dto.*;
import br.com.ofisy.application.user.exceptions.UserNotFoundException;
import br.com.ofisy.domain.user.Email;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.interfaces.api.ControllerTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@EnableMethodSecurity
@WebMvcTest(UserController.class)
class UserControllerTest extends ControllerTestBase {

    public static final String BASE_URL = "/api/v1/users";
    public static final String TEST_USER_PRINCIPAL_EMAIL = "joao@ofisy.com";
    public static final String TEST_USER_PRINCIPAL_NAME = "João Silva";
    public static final String TEST_USER_PRINCIPAL_PASSWORD = "senha123";
    public static final Role TEST_USER_PRINCIPAL_ROLE = Role.ATTENDANT;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Nested
    @DisplayName("POST /api/v1/users")
    class CreateUser {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve criar usuário e retornar 201")
        void shouldCreateUserAndReturn201() throws Exception {
            CreateUserRequestDTO request = mockCreateUserRequest();

            when(userService.create(any())).thenReturn(mockResponse());
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value(TEST_USER_PRINCIPAL_NAME));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 400 quando dados inválidos")
        void shouldReturn400WhenInvalidData() throws Exception {
            String invalidRequest = """
                {
                    "name": "",
                    "email": "emailinvalido",
                    "password": "123",
                    "role": null
                }
                """;

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }

    }

    @Nested
    @DisplayName("GET /api/v1/users")
    class ListAllUsers {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar página com usuários e retornar 200")
        void shouldReturnPageWithUsersAndReturn200() throws Exception {
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(mockResponse()), pageable, 1);

            when(userService.listAllUsers(any())).thenReturn(page);
            mockMvc.perform(get(BASE_URL)
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar página vazia quando não há usuários")
        void shouldReturnEmptyPageWhenNoUsers() throws Exception {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<UserResponseDTO>(List.of(), pageable, 0);

            when(userService.listAllUsers(any())).thenReturn(emptyPage);
            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(0))
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{id}")
    class FindById {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar usuário quando encontrado")
        void shouldReturnUserWhenFound() throws Exception {
            UUID id = UUID.randomUUID();
            when(userService.findById(id)).thenReturn(mockResponse());
            mockMvc.perform(get(BASE_URL + "/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(TEST_USER_PRINCIPAL_NAME));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{id}/modify-role")
    class ModifyRole {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve alterar role e retornar 200")
        void shouldModifyRoleAndReturn200() throws Exception {
            UUID id = UUID.randomUUID();
            ModifyUserRoleRequestDTO request = new ModifyUserRoleRequestDTO(Role.ADMIN);

            when(userService.modifyUserRole(eq(id), any())).thenReturn(mockResponse());
            mockMvc.perform(patch(BASE_URL + "/{id}/modify-role", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{id}/update-password")
    class UpdatePassword {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve atualizar senha e retornar 200")
        void shouldUpdatePasswordAndReturn200() throws Exception {
            UUID id = UUID.randomUUID();
            UpdatePasswordRequestDTO request = new UpdatePasswordRequestDTO(TEST_USER_PRINCIPAL_PASSWORD, "novaSenha123");

            when(userService.updatePassword(eq(id), any())).thenReturn(mockResponse());
            mockMvc.perform(patch(BASE_URL + "/{id}/update-password", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{id}/deactivate")
    class DeactivateUser {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve desativar usuário e retornar 200")
        void shouldDeactivateUserAndReturn200() throws Exception {
            UUID id = UUID.randomUUID();
            when(userService.deactivateUser(id)).thenReturn(mockResponse());
            mockMvc.perform(patch(BASE_URL + "/{id}/deactivate", id)
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{id}/activate")
    class ActivateUser {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve ativar usuário e retornar 200")
        void shouldActivateUserAndReturn200() throws Exception {
            UUID id = UUID.randomUUID();
            when(userService.activateUser(id)).thenReturn(mockResponse());
            mockMvc.perform(patch(BASE_URL + "/{id}/activate", id)
                            .with(csrf()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{id}/remove")
    class RemoveUser {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve remover usuário e retornar 204")
        void shouldRemoveUserAndReturn204() throws Exception {
            UUID id = UUID.randomUUID();
            doNothing().when(userService).removeUser(id);
            mockMvc.perform(delete(BASE_URL + "/{id}/remove", id)
                            .with(csrf()))
                    .andExpect(status().isNoContent());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 404 ao remover usuário inexistente")
        void shouldReturn404WhenRemovingNonExistentUser() throws Exception {
            UUID id = UUID.randomUUID();
            doThrow(new UserNotFoundException(id)).when(userService).removeUser(id);
            mockMvc.perform(delete(BASE_URL + "/{id}/remove", id)
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    private UserResponseDTO mockResponse() {
        return new UserResponseDTO(
                UUID.randomUUID(), TEST_USER_PRINCIPAL_NAME,
                new Email(TEST_USER_PRINCIPAL_EMAIL).emailAddress(),
                TEST_USER_PRINCIPAL_ROLE, true,
                LocalDateTime.now(), null
        );
    }

    private CreateUserRequestDTO mockCreateUserRequest() {
        return new CreateUserRequestDTO(
                TEST_USER_PRINCIPAL_NAME, TEST_USER_PRINCIPAL_EMAIL, TEST_USER_PRINCIPAL_PASSWORD, TEST_USER_PRINCIPAL_ROLE
        );
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
}