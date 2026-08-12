package br.com.ofisy.config;

import br.com.ofisy.adapters.controllers.user.LoginController;
import br.com.ofisy.adapters.controllers.user.UserController;
import br.com.ofisy.adapters.controllers.user.dto.CreateUserRequestDTO;
import br.com.ofisy.application.customer.exceptions.CustomerAlreadyExistsException;
import br.com.ofisy.application.customer.exceptions.CustomerCpfCnpjNotFoundException;
import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.customer.activate.ActivateCustomerUseCase;
import br.com.ofisy.application.customer.deactivate.DeactivateCustomerUseCase;
import br.com.ofisy.application.customer.identifybycpfcnpj.IdentifyByCpfCnpjCustomerUseCase;
import br.com.ofisy.application.customer.identifybyid.IdentifyByIdCustomerUseCase;
import br.com.ofisy.application.customer.list.ListRegisteredCustomerUseCase;
import br.com.ofisy.application.customer.register.RegisterCustomerUseCase;
import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.application.quote.exceptions.QuoteAlreadyExistsException;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.application.quote.findbyserviceorderid.FindQuoteByServiceOrderIdUseCase;
import br.com.ofisy.application.servicecatalog.exceptions.ServiceCatalogNotFoundException;
import br.com.ofisy.application.serviceorder.createcomplete.CreateCompleteServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.exceptions.QuoteNotFoundForServiceOrderException;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.serviceorder.submitquoteforapproval.SubmitQuoteForApprovalUseCase;
import br.com.ofisy.application.quote.update.UpdateQuoteUseCase;
import br.com.ofisy.application.stock.exceptions.InsufficientStockException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.application.stock.release.ReleaseStockUseCase;
import br.com.ofisy.application.user.activateuser.ActivateUserUseCase;
import br.com.ofisy.application.user.createuser.CreateUserUseCase;
import br.com.ofisy.application.user.deactivateuser.DeactivateUserUseCase;
import br.com.ofisy.application.user.exceptions.EmailNotFoundException;
import br.com.ofisy.application.serviceorder.approvequote.ApproveServiceOrderQuoteUseCase;
import br.com.ofisy.application.serviceorder.cancel.CancelServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.create.CreateServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.delivertocustomer.DeliverToCustomerUseCase;
import br.com.ofisy.application.serviceorder.generatequote.GenerateServiceOrderQuoteUseCase;
import br.com.ofisy.application.serviceorder.getstatus.GetServiceOrderStatusUseCase;
import br.com.ofisy.application.serviceorder.listactive.ListActiveServiceOrdersUseCase;
import br.com.ofisy.application.serviceorder.listfinished.ListFinishedServiceOrdersUseCase;
import br.com.ofisy.application.serviceorder.listreceived.ListReceivedServiceOrdersUseCase;
import br.com.ofisy.application.serviceorder.reprovequote.ReproveServiceOrderQuoteUseCase;
import br.com.ofisy.application.serviceorder.startdiagnostic.StartDiagnosticUseCase;
import br.com.ofisy.application.user.exceptions.UserNotFoundException;
import br.com.ofisy.application.user.findbyid.FindUserByIdUseCase;
import br.com.ofisy.application.user.listall.ListAllUsersUseCase;
import br.com.ofisy.application.user.login.LoginUseCase;
import br.com.ofisy.application.user.modifyrole.ModifyUserRoleUseCase;
import br.com.ofisy.application.user.updatepassword.UpdatePasswordUseCase;
import br.com.ofisy.application.vehicle.exceptions.VehicleAlreadyExistsException;
import br.com.ofisy.application.vehicle.exceptions.VehicleLicensePlateNotFoundException;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.application.vehicle.identifybyid.IdentifyVehicleByIdUseCase;
import br.com.ofisy.application.vehicle.identifybylicenseplate.IdentifyVehicleByLicensePlateUseCase;
import br.com.ofisy.application.vehicle.listall.ListRegisteredVehiclesUseCase;
import br.com.ofisy.application.vehicle.listbycustomer.ListVehiclesByCustomerUseCase;
import br.com.ofisy.application.vehicle.register.RegisterVehicleUseCase;
import br.com.ofisy.domain.customer.exceptions.InvalidCpfCnpjException;
import br.com.ofisy.domain.notification.exceptions.InvalidNotificationMessageException;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteDataException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteItemException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.exceptions.EmailAlreadyExistsException;
import br.com.ofisy.adapters.controllers.customer.CustomerController;
import br.com.ofisy.adapters.controllers.serviceorder.ServiceOrderController;
import br.com.ofisy.domain.user.exceptions.InactiveUserException;
import br.com.ofisy.interfaces.api.ControllerTestBase;
import br.com.ofisy.adapters.controllers.vehicle.VehicleController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
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

@WebMvcTest({CustomerController.class, UserController.class, LoginController.class, VehicleController.class, ServiceOrderController.class})
@WithMockUser
class GlobalExceptionHandlerTest extends ControllerTestBase {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private RegisterCustomerUseCase registerCustomerUseCase;
    @MockitoBean
    private ListRegisteredCustomerUseCase listRegisteredCustomerUseCase;
    @MockitoBean
    private IdentifyByIdCustomerUseCase identifyByIdCustomerUseCase;
    @MockitoBean
    private IdentifyByCpfCnpjCustomerUseCase identifyByCpfCnpjCustomerUseCase;

    @MockitoBean
    private ActivateCustomerUseCase activateCustomerUseCase;

    @MockitoBean
    private DeactivateCustomerUseCase deactivateCustomerUseCase;

    @MockitoBean
    private CreateServiceOrderUseCase createServiceOrderUseCase;
    @MockitoBean
    private CreateCompleteServiceOrderUseCase createCompleteServiceOrderUseCase;
    @MockitoBean
    private CancelServiceOrderUseCase cancelServiceOrderUseCase;
    @MockitoBean
    private ListReceivedServiceOrdersUseCase listReceivedServiceOrdersUseCase;
    @MockitoBean
    private ListFinishedServiceOrdersUseCase listFinishedServiceOrdersUseCase;
    @MockitoBean
    private ListActiveServiceOrdersUseCase listActiveServiceOrdersUseCase;
    @MockitoBean
    private StartDiagnosticUseCase startDiagnosticUseCase;
    @MockitoBean
    private DeliverToCustomerUseCase deliverToCustomerUseCase;
    @MockitoBean
    private GetServiceOrderStatusUseCase getServiceOrderStatusUseCase;
    @MockitoBean
    private GenerateServiceOrderQuoteUseCase generateServiceOrderQuoteUseCase;
    @MockitoBean
    private ApproveServiceOrderQuoteUseCase approveServiceOrderQuoteUseCase;
    @MockitoBean
    private ReproveServiceOrderQuoteUseCase reproveServiceOrderQuoteUseCase;
    @MockitoBean
    private UpdateQuoteUseCase updateQuoteUseCase;
    @MockitoBean
    private SubmitQuoteForApprovalUseCase submitQuoteForApprovalUseCase;
    @MockitoBean
    private FindQuoteByServiceOrderIdUseCase findQuoteByServiceOrderIdUseCase;

    @MockitoBean
    private AuthenticationManager authenticationManager;
    @MockitoBean
    private RegisterVehicleUseCase registerVehicleUseCase;
    @MockitoBean
    private ListRegisteredVehiclesUseCase listRegisteredVehiclesUseCase;
    @MockitoBean
    private ListVehiclesByCustomerUseCase listVehiclesByCustomerUseCase;
    @MockitoBean
    private IdentifyVehicleByIdUseCase identifyVehicleByIdUseCase;
    @MockitoBean
    private IdentifyVehicleByLicensePlateUseCase identifyVehicleByLicensePlateUseCase;
    @MockitoBean
    private CreateUserUseCase createUserUseCase;
    @MockitoBean
    private FindUserByIdUseCase findUserByIdUseCase;
    @MockitoBean
    private ListAllUsersUseCase listAllUsersUseCase;
    @MockitoBean
    private ModifyUserRoleUseCase modifyUserRoleUseCase;
    @MockitoBean
    private UpdatePasswordUseCase updatePasswordUseCase;
    @MockitoBean
    private DeactivateUserUseCase deactivateUserUseCase;
    @MockitoBean
    private ActivateUserUseCase activateUserUseCase;
    @MockitoBean
    private LoginUseCase loginUseCase;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Nested
    class CustomerNotFound {

        @Test
        void shouldReturn404WhenCustomerNotFoundById() throws Exception {
            var id = UUID.randomUUID();
            when(identifyByIdCustomerUseCase.execute(any(UUID.class)))
                    .thenThrow(new CustomerNotFoundException(id));

            mockMvc.perform(get("/api/v1/customers/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Cliente não encontrado"))
                    .andExpect(jsonPath("$.detail").value("Cliente não encontrado com o ID: " + id));
        }

        @Test
        void shouldReturn404WhenCustomerNotFoundByCpfCnpj() throws Exception {
            var cpfCnpj = "52998224725";
            when(identifyByCpfCnpjCustomerUseCase.execute(cpfCnpj))
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
            when(registerCustomerUseCase.execute(any()))
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
            when(identifyByCpfCnpjCustomerUseCase.execute(invalidCpfCnpj))
                    .thenThrow(new InvalidCpfCnpjException(invalidCpfCnpj));

            mockMvc.perform(get("/api/v1/customers").param("cpfCnpj", invalidCpfCnpj))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("CPF/CNPJ inválido"))
                    .andExpect(jsonPath("$.detail").value("CPF ou CNPJ inválido: " + invalidCpfCnpj));
        }
    }

    @Nested
    class InvalidNotificationMessage {

        @Test
        void shouldReturn400WhenNotificationMessageIsInvalid() throws Exception {
            var message = "Mensagem inválida";
            when(identifyByIdCustomerUseCase.execute(any(UUID.class)))
                    .thenThrow(new InvalidNotificationMessageException(message));

            mockMvc.perform(get("/api/v1/customers/{id}", UUID.randomUUID()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Mensagem de notificação inválida"))
                    .andExpect(jsonPath("$.detail").value(message));
        }
    }

    @Nested
    class IllegalArgument {

        @Test
        void shouldReturn400WhenIllegalArgumentIsThrown() throws Exception {
            when(identifyByIdCustomerUseCase.execute(any(UUID.class)))
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
            when(loginUseCase.execute(any()))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            mockMvc.perform(post("/api/v1/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                                "email": "admin@ofisy.com",
                                "password": "senhaErrada"
                            }
                        """))
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
            when(findUserByIdUseCase.execute(id)).thenThrow(new UserNotFoundException(id));

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
            var request = new CreateUserRequestDTO("Pedro Mecânico", "mecanico@ofisy.com", "senha12345", Role.MECHANIC);

            when(createUserUseCase.execute(any())).thenThrow(new EmailAlreadyExistsException("mecanico@ofisy.com"));

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

    @Nested
    class UsernameNotFound {

        @Test
        @DisplayName("Deve retornar 401 quando usuário não encontrado")
        void shouldReturn401WhenUsernameNotFound() throws Exception {
            when(loginUseCase.execute(any()))
                    .thenThrow(new UsernameNotFoundException("Usuário não encontrado"));

            mockMvc.perform(post("/api/v1/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                                "email": "naoexiste@ofisy.com",
                                "password": "senha123"
                            }
                        """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.title").value("Usuário não autorizado"));
        }
    }

    @Nested
    class VehicleNotFound {

        @Test
        void shouldReturn404WhenVehicleNotFoundById() throws Exception {
            var id = UUID.randomUUID();
            when(identifyVehicleByIdUseCase.execute(any(UUID.class)))
                    .thenThrow(new VehicleNotFoundException(id));

            mockMvc.perform(get("/api/v1/vehicles/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Veículo não encontrado"))
                    .andExpect(jsonPath("$.detail").value("Veículo com ID " + id + " não encontrado"));
        }
    }

    @Nested
    class VehicleLicensePlateNotFound {

        @Test
        void shouldReturn404WhenVehicleNotFoundByLicensePlate() throws Exception {
            var plate = "ABC1234";
            when(identifyVehicleByLicensePlateUseCase.execute(plate))
                    .thenThrow(new VehicleLicensePlateNotFoundException(plate));

            mockMvc.perform(get("/api/v1/vehicles").param("licensePlate", plate))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Veículo não encontrado pela placa"))
                    .andExpect(jsonPath("$.detail").value("Veículo com placa '" + plate + "' não encontrado"));
        }
    }

    @Nested
    class VehicleAlreadyExists {

        @Test
        void shouldReturn409WhenVehicleAlreadyExists() throws Exception {
            var plate = "ABC1234";
            when(registerVehicleUseCase.execute(any()))
                    .thenThrow(new VehicleAlreadyExistsException(plate));

            var body = """
                    {
                        "customerId": "00000000-0000-0000-0000-000000000001",
                        "licensePlate": "ABC1234",
                        "model": "Civic",
                        "brand": "Honda",
                        "color": "Preto",
                        "year": 2022
                    }
                    """;

            mockMvc.perform(post("/api/v1/vehicles")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Veículo já existe"))
                    .andExpect(jsonPath("$.detail").value("Veículo com placa " + plate + " já está registrado."));
        }
    }

    @Nested
    class InvalidServiceOrderTransition {

        @Test
        void shouldReturn409WhenStatusTransitionIsInvalid() throws Exception {
            when(identifyByIdCustomerUseCase.execute(any(UUID.class)))
                    .thenThrow(new InvalidServiceOrderTransitionException(
                            ServiceOrderStatus.RECEIVED, ServiceOrderStatus.DELIVERED));

            mockMvc.perform(get("/api/v1/customers/{id}", UUID.randomUUID()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Transição de status inválida"))
                    .andExpect(jsonPath("$.detail").value(
                            "Nao pode alterar o status da ordem de servico de RECEIVED para DELIVERED"));
        }
    }

    @Nested
    class EmailNotFound {

        @Test
        @DisplayName("Deve retornar 404 quando email não encontrado")
        void shouldReturn404WhenEmailNotFound() throws Exception {
            when(loginUseCase.execute(any()))
                    .thenThrow(new EmailNotFoundException("naoexiste@ofisy.com"));

            mockMvc.perform(post("/api/v1/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                                "email": "naoexiste@ofisy.com",
                                "password": "senha123"
                            }
                        """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Email informado não encontrado"));
        }
    }

    @Nested
    class UserDisabled {

        @Test
        @DisplayName("Deve retornar 401 quando usuário está inativo")
        void shouldReturn401WhenUserIsDisabled() throws Exception {
            when(loginUseCase.execute(any()))
                    .thenThrow(new InactiveUserException("Usuário inativo: joao@ofisy.com"));

            mockMvc.perform(post("/api/v1/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                                "email": "joao@ofisy.com",
                                "password": "senha123"
                            }
                        """))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.title").value("Usuário inativo"));
        }
    }


    @Nested
    class ServiceOrderNotFound {

        @Test
        @WithMockUser(roles = "MECHANIC")
        @DisplayName("Deve retornar 404 quando ordem de serviço não encontrada")
        void shouldReturn404WhenServiceOrderNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(startDiagnosticUseCase.execute(id))
                    .thenThrow(new ServiceOrderNotFoundException(id));

            mockMvc.perform(patch("/api/v1/service-orders/{id}/start-diagnostic", id)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Ordem de serviço não encontrada"));
        }
    }

    @Nested
    class ServiceCatalogNotFound {

        @Test
        @WithMockUser(roles = "MECHANIC")
        @DisplayName("Deve retornar 404 quando serviço não encontrado no catálogo")
        void shouldReturn404WhenServiceCatalogNotFound() throws Exception {
            var serviceOrderId = UUID.randomUUID();
            var serviceCatalogId = UUID.randomUUID();
            when(generateServiceOrderQuoteUseCase.execute(any()))
                    .thenThrow(new ServiceCatalogNotFoundException(serviceCatalogId.toString()));

            var body = """
                {
                    "stockItems": [],
                    "serviceItems": [{ "serviceCatalogId": "%s" }]
                }
                """.formatted(serviceCatalogId);

            mockMvc.perform(post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Serviço não encontrado no catalogo"));
        }
    }

    @Nested
    class QuoteNotFound {

        @Test
        @DisplayName("Deve retornar 404 quando orçamento não encontrado")
        void shouldReturn404WhenQuoteNotFound() throws Exception {
            var quoteId = UUID.randomUUID();
            when(approveServiceOrderQuoteUseCase.execute(quoteId))
                    .thenThrow(new QuoteNotFoundException(quoteId));

            mockMvc.perform(patch("/api/v1/service-orders/quote/{id}/approve", quoteId)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Orçamento não encontrado"));
        }
    }

    @Nested
    class QuoteNotFoundForServiceOrder {

        @Test
        @WithMockUser(roles = "MECHANIC")
        @DisplayName("Deve retornar 404 quando orçamento não encontrado para a ordem de serviço")
        void shouldReturn404WhenQuoteNotFoundForServiceOrder() throws Exception {
            var serviceOrderId = UUID.randomUUID();
            when(submitQuoteForApprovalUseCase.execute(serviceOrderId))
                    .thenThrow(new QuoteNotFoundForServiceOrderException(serviceOrderId));

            mockMvc.perform(patch("/api/v1/service-orders/{id}/submit-for-approval", serviceOrderId)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Orçamento não encontrado para ordem de serviço informada"));
        }
    }

    @Nested
    class InvalidQuoteStatus {

        @Test
        @WithMockUser(roles = "MECHANIC")
        @DisplayName("Deve retornar 409 quando status do orçamento é inválido para a ação")
        void shouldReturn409WhenQuoteStatusIsInvalid() throws Exception {
            var quoteId = UUID.randomUUID();
            when(updateQuoteUseCase.execute(any()))
                    .thenThrow(new InvalidQuoteStatusException("editar", QuoteStatus.APPROVED));

            var body = """
                {
                    "stockItems": [{ "stockId": "%s", "quantity": 1 }],
                    "serviceItems": []
                }
                """.formatted(UUID.randomUUID());

            mockMvc.perform(patch("/api/v1/service-orders/quote/{id}/update", quoteId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Status do orçamento inválido"));
        }
    }

    @Nested
    class QuoteItemAlreadyExists {

        @Test
        @WithMockUser(roles = "MECHANIC")
        @DisplayName("Deve retornar 409 quando item já existe no orçamento")
        void shouldReturn409WhenQuoteItemAlreadyExists() throws Exception {
            var serviceOrderId = UUID.randomUUID();
            when(generateServiceOrderQuoteUseCase.execute(any()))
                    .thenThrow(new QuoteItemAlreadyExistsException("Filtro de óleo"));

            var body = """
                {
                    "stockItems": [{ "stockId": "%s", "quantity": 1 }],
                    "serviceItems": []
                }
                """.formatted(UUID.randomUUID());

            mockMvc.perform(post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Item já existe no orçamento"));
        }
    }

    @Nested
    class InvalidQuoteData {

        @Test
        @WithMockUser(roles = "MECHANIC")
        @DisplayName("Deve retornar 400 quando dados do orçamento são inválidos")
        void shouldReturn400WhenQuoteDataIsInvalid() throws Exception {
            var serviceOrderId = UUID.randomUUID();
            when(generateServiceOrderQuoteUseCase.execute(any()))
                    .thenThrow(new InvalidQuoteDataException(UUID.randomUUID()));

            var body = """
                {
                    "stockItems": [],
                    "serviceItems": []
                }
                """;

            mockMvc.perform(post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Dados do orçamento inválidos"));
        }
    }

    @Nested
    class InvalidQuoteItem {

        @Test
        @WithMockUser(roles = "MECHANIC")
        @DisplayName("Deve retornar 400 quando item do orçamento é inválido")
        void shouldReturn400WhenQuoteItemIsInvalid() throws Exception {
            var serviceOrderId = UUID.randomUUID();
            when(generateServiceOrderQuoteUseCase.execute(any()))
                    .thenThrow(new InvalidQuoteItemException("Quantidade do item deve ser maior que zero!"));

            var body = """
                {
                    "stockItems": [{ "stockId": "%s", "quantity": 1 }],
                    "serviceItems": []
                }
                """.formatted(UUID.randomUUID());

            mockMvc.perform(post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Item do orçamento inválido"));
        }
    }

    @Nested
    class StockNotFound {

        @Test
        @WithMockUser(roles = "MECHANIC")
        @DisplayName("Deve retornar 404 quando estoque não encontrado")
        void shouldReturn404WhenStockNotFound() throws Exception {
            var serviceOrderId = UUID.randomUUID();
            var stockId = UUID.randomUUID();
            when(generateServiceOrderQuoteUseCase.execute(any()))
                    .thenThrow(new StockNotFoundException(stockId));

            var body = """
                {
                    "stockItems": [{ "stockId": "%s", "quantity": 1 }],
                    "serviceItems": []
                }
                """.formatted(stockId);

            mockMvc.perform(post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Estoque não encontrado"));
        }
    }

    @Nested
    class InsufficientStock {

        @Test
        @WithMockUser(roles = "MECHANIC")
        @DisplayName("Deve retornar 409 quando estoque é insuficiente")
        void shouldReturn409WhenStockIsInsufficient() throws Exception {
            var serviceOrderId = UUID.randomUUID();
            var stockId = UUID.randomUUID();
            when(generateServiceOrderQuoteUseCase.execute(any()))
                    .thenThrow(new InsufficientStockException(stockId));

            var body = """
                {
                    "stockItems": [{ "stockId": "%s", "quantity": 999 }],
                    "serviceItems": []
                }
                """.formatted(stockId);

            mockMvc.perform(post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Estoque insuficiente"));
        }
    }

    @Nested
    class QuoteAlreadyExists {

        @Test
        @WithMockUser(roles = "MECHANIC")
        @DisplayName("Deve retornar 409 quando já existe orçamento para a ordem de serviço")
        void shouldReturn409WhenQuoteAlreadyExists() throws Exception {
            var serviceOrderId = UUID.randomUUID();
            when(generateServiceOrderQuoteUseCase.execute(any()))
                    .thenThrow(new QuoteAlreadyExistsException(serviceOrderId));

            var body = """
                {
                    "stockItems": [{ "stockId": "%s", "quantity": 1 }],
                    "serviceItems": []
                }
                """.formatted(UUID.randomUUID());

            mockMvc.perform(post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Orçamento já existe para a ordem de serviço"));
        }
    }
}
