package br.com.ofisy.interfaces.api;

import br.com.ofisy.application.customer.CustomerService;
import br.com.ofisy.application.customer.exceptions.CustomerAlreadyExistsException;
import br.com.ofisy.application.customer.exceptions.CustomerCpfCnpjNotFoundException;
import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.vehicle.VehicleService;
import br.com.ofisy.application.vehicle.exceptions.VehicleAlreadyExistsException;
import br.com.ofisy.application.vehicle.exceptions.VehicleLicensePlateNotFoundException;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.domain.customer.exceptions.InvalidCpfCnpjException;
import br.com.ofisy.interfaces.api.customer.CustomerController;
import br.com.ofisy.interfaces.api.vehicle.VehicleController;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest({CustomerController.class, VehicleController.class})
@WithMockUser
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    @MockitoBean
    private VehicleService vehicleService;

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
    class VehicleNotFound {

        @Test
        void shouldReturn404WhenVehicleNotFoundById() throws Exception {
            var id = UUID.randomUUID();
            when(vehicleService.identifyVehicleById(any(UUID.class)))
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
            when(vehicleService.identifyVehicleByLicensePlate(plate))
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
            when(vehicleService.registerVehicle(any()))
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
}
