package br.com.ofisy.adapters.controllers.user;

import br.com.ofisy.adapters.controllers.user.dto.ModifyUserRoleRequestDTO;
import br.com.ofisy.adapters.controllers.user.dto.UpdatePasswordRequestDTO;
import br.com.ofisy.application.user.activateuser.ActivateUserUseCase;
import br.com.ofisy.application.user.createuser.CreateUserUseCase;
import br.com.ofisy.application.user.deactivateuser.DeactivateUserUseCase;
import br.com.ofisy.application.user.exceptions.UserNotFoundException;
import br.com.ofisy.application.user.findbyid.FindUserByIdUseCase;
import br.com.ofisy.application.user.listall.ListAllUsersUseCase;
import br.com.ofisy.application.user.modifyrole.ModifyUserRoleUseCase;
import br.com.ofisy.application.user.updatepassword.UpdatePasswordUseCase;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.interfaces.api.ControllerTestBase;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@WithMockUser(roles = "ADMIN")
class UserControllerTest extends ControllerTestBase {

    private static final String BASE_URL = "/api/v1/users";
    private static final String VALID_EMAIL = "joao@ofisy.com";
    private static final String VALID_NAME = "João Silva";
    private static final String VALID_PASSWORD = "senha123";
    private static final Role VALID_ROLE = Role.ATTENDANT;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateUserUseCase createUserUseCase;
    @MockitoBean
    private ListAllUsersUseCase listAllUsersUseCase;
    @MockitoBean
    private FindUserByIdUseCase findUserByIdUseCase;
    @MockitoBean
    private ModifyUserRoleUseCase modifyUserRoleUseCase;
    @MockitoBean
    private UpdatePasswordUseCase updatePasswordUseCase;
    @MockitoBean
    private DeactivateUserUseCase deactivateUserUseCase;
    @MockitoBean
    private ActivateUserUseCase activateUserUseCase;

    @Nested
    class CreateUser {

        @Test
        @DisplayName("Deve criar usuário e retornar 201")
        void shouldCreateUserAndReturn201() throws Exception {
            when(createUserUseCase.execute(any())).thenReturn(validUser());

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validCreateBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value(VALID_NAME))
                    .andExpect(jsonPath("$.email").value(VALID_EMAIL))
                    .andExpect(jsonPath("$.role").value(VALID_ROLE.name()))
                    .andExpect(jsonPath("$.active").value(true));
        }

        @Test
        @DisplayName("Deve retornar 400 quando body está vazio")
        void shouldReturn400WhenBodyIsEmpty() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de validação"))
                    .andExpect(jsonPath("$.errors.name").value("Nome é obrigatório"))
                    .andExpect(jsonPath("$.errors.email").value("Email é obrigatório"))
                    .andExpect(jsonPath("$.errors.password").value("Senha é obrigatória"))
                    .andExpect(jsonPath("$.errors.role").value("Role é obrigatória"));
        }

        @Test
        @DisplayName("Deve retornar 400 quando email é inválido")
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            var body = """
                    {
                        "name": "João Silva",
                        "email": "emailinvalido",
                        "password": "senha123",
                        "role": "ATTENDANT"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").value("Email inválido"));
        }

        @Test
        @DisplayName("Deve retornar 400 quando senha tem menos de 8 caracteres")
        void shouldReturn400WhenPasswordIsTooShort() throws Exception {
            var body = """
                    {
                        "name": "João Silva",
                        "email": "joao@ofisy.com",
                        "password": "123",
                        "role": "ATTENDANT"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.password").value("Senha deve ter no mínimo 8 caracteres"));
        }
    }

    @Nested
    class ListAllUsers {

        @Test
        @DisplayName("Deve retornar página com usuários e status 200")
        void shouldReturnPageWithUsersAndReturn200() throws Exception {
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(validUser()), pageable, 1);
            when(listAllUsersUseCase.execute(any())).thenReturn(page);

            mockMvc.perform(get(BASE_URL).param("page", "0").param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há usuários")
        void shouldReturnEmptyPageWhenNoUsers() throws Exception {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<User>(Collections.emptyList(), pageable, 0);
            when(listAllUsersUseCase.execute(any())).thenReturn(emptyPage);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }
    }

    @Nested
    class FindById {

        @Test
        @DisplayName("Deve retornar usuário quando encontrado")
        void shouldReturnUserWhenFound() throws Exception {
            var id = UUID.randomUUID();
            when(findUserByIdUseCase.execute(id)).thenReturn(validUser());

            mockMvc.perform(get(BASE_URL + "/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value(VALID_NAME))
                    .andExpect(jsonPath("$.email").value(VALID_EMAIL));
        }

        @Test
        @DisplayName("Deve retornar 404 quando usuário não encontrado")
        void shouldReturn404WhenNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(findUserByIdUseCase.execute(id)).thenThrow(new UserNotFoundException(id));

            mockMvc.perform(get(BASE_URL + "/{id}", id))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Deve retornar 400 quando id não é UUID válido")
        void shouldReturn400WhenIdIsInvalid() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", "not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class ModifyRole {

        @Test
        @DisplayName("Deve alterar role e retornar 200")
        void shouldModifyRoleAndReturn200() throws Exception {
            var id = UUID.randomUUID();
            when(modifyUserRoleUseCase.execute(eq(id), any())).thenReturn(validUser());

            mockMvc.perform(patch(BASE_URL + "/{id}/modify-role", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ModifyUserRoleRequestDTO(Role.ADMIN))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve retornar 404 quando usuário não encontrado")
        void shouldReturn404WhenNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(modifyUserRoleUseCase.execute(eq(id), any())).thenThrow(new UserNotFoundException(id));

            mockMvc.perform(patch(BASE_URL + "/{id}/modify-role", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new ModifyUserRoleRequestDTO(Role.ADMIN))))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class UpdatePassword {

        @Test
        @DisplayName("Deve atualizar senha e retornar 200")
        void shouldUpdatePasswordAndReturn200() throws Exception {
            var id = UUID.randomUUID();
            when(updatePasswordUseCase.execute(eq(id), any())).thenReturn(validUser());

            mockMvc.perform(patch(BASE_URL + "/{id}/update-password", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new UpdatePasswordRequestDTO(VALID_PASSWORD, "novaSenha123"))))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve retornar 404 quando usuário não encontrado")
        void shouldReturn404WhenNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(updatePasswordUseCase.execute(eq(id), any())).thenThrow(new UserNotFoundException(id));

            mockMvc.perform(patch(BASE_URL + "/{id}/update-password", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(
                                    new UpdatePasswordRequestDTO(VALID_PASSWORD, "novaSenha123"))))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class DeactivateUser {

        @Test
        @DisplayName("Deve desativar usuário e retornar 200")
        void shouldDeactivateUserAndReturn200() throws Exception {
            var id = UUID.randomUUID();
            when(deactivateUserUseCase.execute(id)).thenReturn(validUser());

            mockMvc.perform(patch(BASE_URL + "/{id}/deactivate", id).with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve retornar 404 quando usuário não encontrado")
        void shouldReturn404WhenNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(deactivateUserUseCase.execute(id)).thenThrow(new UserNotFoundException(id));

            mockMvc.perform(patch(BASE_URL + "/{id}/deactivate", id).with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class ActivateUser {

        @Test
        @DisplayName("Deve ativar usuário e retornar 200")
        void shouldActivateUserAndReturn200() throws Exception {
            var id = UUID.randomUUID();
            when(activateUserUseCase.execute(id)).thenReturn(validUser());

            mockMvc.perform(patch(BASE_URL + "/{id}/activate", id).with(csrf()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Deve retornar 404 quando usuário não encontrado")
        void shouldReturn404WhenNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(activateUserUseCase.execute(id)).thenThrow(new UserNotFoundException(id));

            mockMvc.perform(patch(BASE_URL + "/{id}/activate", id).with(csrf()))
                    .andExpect(status().isNotFound());
        }
    }

    private User validUser() {
        return User.create(VALID_EMAIL, "hashed-password", VALID_NAME, VALID_ROLE);
    }

    private String validCreateBody() {
        return """
                {
                    "name": "João Silva",
                    "email": "joao@ofisy.com",
                    "password": "senha123",
                    "role": "ATTENDANT"
                }
                """;
    }
}
