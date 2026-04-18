package br.com.ofisy.interfaces.api;

import br.com.ofisy.application.customer.CustomerService;
import br.com.ofisy.application.customer.exceptions.CustomerAlreadyExistsException;
import br.com.ofisy.application.customer.exceptions.CustomerCpfCnpjNotFoundException;
import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.user.UserService;
import br.com.ofisy.application.user.dto.CreateUserRequestDTO;
import br.com.ofisy.application.user.dto.LoginRequestDTO;
import br.com.ofisy.application.user.exceptions.UserNotFoundException;
import br.com.ofisy.domain.customer.exceptions.InvalidCpfCnpjException;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.exceptions.EmailAlreadyExistsException;
import br.com.ofisy.infrastructure.config.security.SecurityConfig;
import br.com.ofisy.interfaces.api.customer.CustomerController;
import br.com.ofisy.interfaces.api.user.LoginController;
import br.com.ofisy.interfaces.api.user.UserController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CustomerController.class, UserController.class, LoginController.class})
@WithMockUser
class GlobalExceptionHandlerTest extends ControllerTestBase {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    @Nested
    class CustomerNotFound {

        @Test
        void shouldReturn404WhenCustomerNotFoundById() throws Exception {
            var id = UUID.randomUUID();
            when(customerService.identifyCustomerById(any(UUID.class)))
                    .thenThrow(new CustomerNotFoundException(id));

            mockMvc.perform(get("/api/v1/customers/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Cliente não encontrado"))
                    .andExpect(jsonPath("$.detail").value("Cliente não encontrado com o ID: " + id));
        }

        @Test
        void shouldReturn404WhenCustomerNotFoundByCpfCnpj() throws Exception {
            var cpfCnpj = "52998224725";
            when(customerService.identifyCustomerByCpfCnpj(cpfCnpj))
                    .thenThrow(new CustomerCpfCnpjNotFoundException(cpfCnpj));

            mockMvc.perform(get("/api/v1/customers").param("cpfCnpj", cpfCnpj))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Cliente não encontrado"))
                    .andExpect(jsonPath("$.detail").value("Cliente com CPF/CNPJ " + cpfCnpj + " não encontrado."));
        }
    }

    @Nested
    class CustomerAlreadyExists {

        @Test
        void shouldReturn409WhenCustomerAlreadyExists() throws Exception {
            var cpfCnpj = "52998224725";
            when(customerService.registerCustomer(any()))
                    .thenThrow(new CustomerAlreadyExistsException(cpfCnpj));

            var body = """
                    {
                        "cpfCnpj": "52998224725",
                        "name": "John Doe",
                        "email": "john@mail.com",
                        "phone": "11999999999"
                    }
                    """;

            mockMvc.perform(post("/api/v1/customers")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Cliente já existe"))
                    .andExpect(jsonPath("$.detail").value("Cliente com CPF/CNPJ " + cpfCnpj + " já existe."));
        }
    }

    @Nested
    class InvalidCpfCnpj {

        @Test
        void shouldReturn400WhenCpfCnpjIsInvalid() throws Exception {
            var invalidCpfCnpj = "00000000000";
            when(customerService.identifyCustomerByCpfCnpj(invalidCpfCnpj))
                    .thenThrow(new InvalidCpfCnpjException(invalidCpfCnpj));

            mockMvc.perform(get("/api/v1/customers").param("cpfCnpj", invalidCpfCnpj))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("CPF/CNPJ inválido"))
                    .andExpect(jsonPath("$.detail").value("CPF ou CNPJ inválido: " + invalidCpfCnpj));
        }
    }

    @Nested
    class IllegalArgument {

        @Test
        void shouldReturn400WhenIllegalArgumentIsThrown() throws Exception {
            when(customerService.identifyCustomerById(any(UUID.class)))
                    .thenThrow(new IllegalArgumentException("ID não pode ser nulo"));

            mockMvc.perform(get("/api/v1/customers/{id}", UUID.randomUUID()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Requisição inválida"))
                    .andExpect(jsonPath("$.detail").value("ID não pode ser nulo"));
        }
    }

    @Nested
    class ValidationErrors {

        @Test
        void shouldReturn400WithFieldErrorsWhenRequestBodyIsInvalid() throws Exception {
            var invalidBody = "{}";

            mockMvc.perform(post("/api/v1/customers")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de validação"))
                    .andExpect(jsonPath("$.detail").value("Um ou mais campos são inválidos"))
                    .andExpect(jsonPath("$.errors").isMap())
                    .andExpect(jsonPath("$.errors.cpfCnpj").value("CPF/CNPJ é obrigatório"))
                    .andExpect(jsonPath("$.errors.name").value("Nome é obrigatório"))
                    .andExpect(jsonPath("$.errors.email").value("Email é obrigatório"))
                    .andExpect(jsonPath("$.errors.phone").value("Telefone é obrigatório"));
        }

        @Test
        void shouldReturn400WithEmailErrorWhenEmailIsInvalid() throws Exception {
            var bodyWithInvalidEmail = """
                    {
                        "cpfCnpj": "52998224725",
                        "name": "John Doe",
                        "email": "not-an-email",
                        "phone": "11999999999"
                    }
                    """;

            mockMvc.perform(post("/api/v1/customers")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(bodyWithInvalidEmail))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de validação"))
                    .andExpect(jsonPath("$.errors.email").value("Email deve ser válido"));
        }
    }

    @Nested
    class BadCredentials {
        @Test
        @DisplayName("Deve retornar 401 com credenciais inválidas")
        void shouldReturn401WithInvalidCredentials() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO("admin@ofisy.com", "senhaErrada");

            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            mockMvc.perform(post("/api/v1/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    class UserNotFound {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 404 quando usuário não encontrado")
        void shouldReturn404WhenUserNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            when(userService.findById(id)).thenThrow(new UserNotFoundException(id));
            mockMvc.perform(get("/api/v1/users/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    class EmailAlreadyExists {
        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 409 quando email do usuário já existe")
        void shouldReturn409WhenEmailAlreadyExists() throws Exception {
            CreateUserRequestDTO request = new CreateUserRequestDTO("Pedro Mecânico", "mecanico@ofisy.com", "senha12345", Role.MECHANIC);

            when(userService.create(any())).thenThrow(new EmailAlreadyExistsException("mecanico@ofisy.com"));
            mockMvc.perform(post("/api/v1/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    class HttpMessageNotReadable {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 400 quando role é inválida")
        void shouldReturn400WhenRoleIsInvalid() throws Exception {
            var id = UUID.randomUUID();

            mockMvc.perform(patch("/api/v1/users/{id}/modify-role", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                                "role": "ROLE_INVALIDA"
                            }
                        """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de validação"))
                    .andExpect(jsonPath("$.detail").value("Um ou mais campos são inválidos ou contêm valores não permitidos"));
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("Deve retornar 400 quando role está ausente")
        void shouldReturn400WhenRoleIsNull() throws Exception {
            var id = UUID.randomUUID();

            mockMvc.perform(patch("/api/v1/users/{id}/modify-role", id)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                                "role": null
                            }
                        """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de validação"));
        }
    }

    private final ObjectMapper objectMapper = new ObjectMapper();
}