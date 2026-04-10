package br.com.ofisy.interfaces.api;

import br.com.ofisy.application.customer.CustomerService;
import br.com.ofisy.application.customer.exceptions.CustomerCpfCnpjNotFoundException;
import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.domain.customer.exceptions.InvalidCpfCnpjException;
import br.com.ofisy.interfaces.api.customer.CustomerController;
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

@WebMvcTest(CustomerController.class)
@WithMockUser
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

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

            mockMvc.perform(get("/api/v1/customers/cpfcnpj/{cpfCnpj}", cpfCnpj))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Cliente não encontrado"))
                    .andExpect(jsonPath("$.detail").value("Cliente com CPF/CNPJ " + cpfCnpj + " não encontrado."));
        }
    }

    @Nested
    class InvalidCpfCnpj {

        @Test
        void shouldReturn400WhenCpfCnpjIsInvalid() throws Exception {
            var invalidCpfCnpj = "00000000000";
            when(customerService.identifyCustomerByCpfCnpj(invalidCpfCnpj))
                    .thenThrow(new InvalidCpfCnpjException(invalidCpfCnpj));

            mockMvc.perform(get("/api/v1/customers/cpfcnpj/{cpfCnpj}", invalidCpfCnpj))
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
}