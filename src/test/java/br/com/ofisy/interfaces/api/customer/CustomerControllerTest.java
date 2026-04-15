package br.com.ofisy.interfaces.api.customer;

import br.com.ofisy.application.customer.CustomerService;
import br.com.ofisy.application.customer.dto.CustomerResponseDTO;
import br.com.ofisy.application.customer.exceptions.CustomerAlreadyExistsException;
import br.com.ofisy.application.customer.exceptions.CustomerCpfCnpjNotFoundException;
import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.domain.customer.exceptions.InvalidCpfCnpjException;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CustomerController.class)
@WithMockUser
class CustomerControllerTest {

    private static final String BASE_URL = "/api/v1/customers";
    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CustomerService customerService;

    private CustomerResponseDTO responseDTO(String cpfCnpj, String name) {
        return new CustomerResponseDTO(cpfCnpj, name, name.toLowerCase().replace(" ", "") + "@mail.com", "11999999999", NOW, NOW);
    }

    @Nested
    class GetAllCustomers {

        @Test
        void shouldReturn200WithPageOfCustomers() throws Exception {
            var dto = responseDTO(VALID_CPF, "John Doe");
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(dto), pageable, 1);
            when(customerService.listRegisteredCustomers(any())).thenReturn(page);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].cpfCnpj").value(VALID_CPF))
                    .andExpect(jsonPath("$.content[0].name").value("John Doe"))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        void shouldReturn200WithEmptyPageWhenNoCustomers() throws Exception {
            var pageable = PageRequest.of(0, 10);
            when(customerService.listRegisteredCustomers(any()))
                    .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        void shouldReturn200WithMultipleCustomers() throws Exception {
            var dto1 = responseDTO(VALID_CPF, "Alice");
            var dto2 = responseDTO(VALID_CNPJ, "Bob");
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(dto1, dto2), pageable, 2);
            when(customerService.listRegisteredCustomers(any())).thenReturn(page);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].name").value("Alice"))
                    .andExpect(jsonPath("$.content[1].name").value("Bob"))
                    .andExpect(jsonPath("$.totalElements").value(2));
        }

        @Test
        void shouldRespectPageAndSizeQueryParams() throws Exception {
            var pageable = PageRequest.of(1, 5);
            when(customerService.listRegisteredCustomers(any()))
                    .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

            mockMvc.perform(get(BASE_URL).param("page", "1").param("size", "5"))
                    .andExpect(status().isOk());

            verify(customerService).listRegisteredCustomers(any());
        }
    }

    @Nested
    class GetCustomerById {

        @Test
        void shouldReturn200WithCustomerWhenFound() throws Exception {
            var id = UUID.randomUUID();
            var dto = responseDTO(VALID_CPF, "John Doe");
            when(customerService.identifyCustomerById(id)).thenReturn(dto);

            mockMvc.perform(get(BASE_URL + "/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cpfCnpj").value(VALID_CPF))
                    .andExpect(jsonPath("$.name").value("John Doe"))
                    .andExpect(jsonPath("$.email").value("johndoe@mail.com"))
                    .andExpect(jsonPath("$.phone").value("11999999999"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        void shouldReturn404WhenCustomerNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(customerService.identifyCustomerById(id))
                    .thenThrow(new CustomerNotFoundException(id));

            mockMvc.perform(get(BASE_URL + "/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Cliente não encontrado"));
        }

        @Test
        void shouldReturn400WhenIdIsNotAValidUUID() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", "not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetCustomerByCpfCnpj {

        @Test
        void shouldReturn200WithCustomerWhenCpfFound() throws Exception {
            var dto = responseDTO(VALID_CPF, "John Doe");
            when(customerService.identifyCustomerByCpfCnpj(VALID_CPF)).thenReturn(dto);

            mockMvc.perform(get(BASE_URL).param("cpfCnpj", VALID_CPF))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cpfCnpj").value(VALID_CPF))
                    .andExpect(jsonPath("$.name").value("John Doe"));
        }

        @Test
        void shouldReturn200WithCustomerWhenCnpjFound() throws Exception {
            var dto = responseDTO(VALID_CNPJ, "Empresa SA");
            when(customerService.identifyCustomerByCpfCnpj(VALID_CNPJ)).thenReturn(dto);

            mockMvc.perform(get(BASE_URL).param("cpfCnpj", VALID_CNPJ))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.cpfCnpj").value(VALID_CNPJ));
        }

        @Test
        void shouldReturn404WhenCpfCnpjNotFound() throws Exception {
            when(customerService.identifyCustomerByCpfCnpj(VALID_CPF))
                    .thenThrow(new CustomerCpfCnpjNotFoundException(VALID_CPF));

            mockMvc.perform(get(BASE_URL).param("cpfCnpj", VALID_CPF))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Cliente não encontrado"))
                    .andExpect(jsonPath("$.detail").value("Cliente com CPF/CNPJ " + VALID_CPF + " não encontrado."));
        }

        @Test
        void shouldReturn400WhenCpfCnpjIsInvalid() throws Exception {
            var invalid = "00000000000";
            when(customerService.identifyCustomerByCpfCnpj(invalid))
                    .thenThrow(new InvalidCpfCnpjException(invalid));

            mockMvc.perform(get(BASE_URL).param("cpfCnpj", invalid))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("CPF/CNPJ inválido"));
        }
    }

    @Nested
    class RegisterCustomer {

        private String validBody() {
            return """
                    {
                        "cpfCnpj": "52998224725",
                        "name": "John Doe",
                        "email": "john@mail.com",
                        "phone": "11999999999"
                    }
                    """;
        }

        @Test
        void shouldReturn201WithCreatedCustomer() throws Exception {
            var dto = responseDTO(VALID_CPF, "John Doe");
            when(customerService.registerCustomer(any())).thenReturn(dto);

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.cpfCnpj").value(VALID_CPF))
                    .andExpect(jsonPath("$.name").value("John Doe"))
                    .andExpect(jsonPath("$.email").value("johndoe@mail.com"))
                    .andExpect(jsonPath("$.phone").value("11999999999"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        void shouldReturn400WhenBodyIsEmpty() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de validação"))
                    .andExpect(jsonPath("$.errors.cpfCnpj").value("CPF/CNPJ é obrigatório"))
                    .andExpect(jsonPath("$.errors.name").value("Nome é obrigatório"))
                    .andExpect(jsonPath("$.errors.email").value("Email é obrigatório"))
                    .andExpect(jsonPath("$.errors.phone").value("Telefone é obrigatório"));
        }

        @Test
        void shouldReturn400WhenCpfCnpjIsBlank() throws Exception {
            var body = """
                    {
                        "cpfCnpj": "",
                        "name": "John Doe",
                        "email": "john@mail.com",
                        "phone": "11999999999"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.cpfCnpj").value("CPF/CNPJ é obrigatório"));
        }

        @Test
        void shouldReturn400WhenNameIsBlank() throws Exception {
            var body = """
                    {
                        "cpfCnpj": "52998224725",
                        "name": "",
                        "email": "john@mail.com",
                        "phone": "11999999999"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.name").value("Nome é obrigatório"));
        }

        @Test
        void shouldReturn400WhenEmailIsBlank() throws Exception {
            var body = """
                    {
                        "cpfCnpj": "52998224725",
                        "name": "John Doe",
                        "email": "",
                        "phone": "11999999999"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").exists());
        }

        @Test
        void shouldReturn400WhenEmailIsInvalid() throws Exception {
            var body = """
                    {
                        "cpfCnpj": "52998224725",
                        "name": "John Doe",
                        "email": "not-an-email",
                        "phone": "11999999999"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").value("Email deve ser válido"));
        }

        @Test
        void shouldReturn400WhenPhoneIsBlank() throws Exception {
            var body = """
                    {
                        "cpfCnpj": "52998224725",
                        "name": "John Doe",
                        "email": "john@mail.com",
                        "phone": ""
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.phone").value("Telefone é obrigatório"));
        }

        @Test
        void shouldReturn400WhenBodyIsMissing() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn409WhenCustomerAlreadyExists() throws Exception {
            when(customerService.registerCustomer(any()))
                    .thenThrow(new CustomerAlreadyExistsException(VALID_CPF));

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Cliente já existe"))
                    .andExpect(jsonPath("$.detail").value("Cliente com CPF/CNPJ " + VALID_CPF + " já existe."));
        }

        @Test
        void shouldReturn400WhenCpfCnpjIsInvalid() throws Exception {
            when(customerService.registerCustomer(any()))
                    .thenThrow(new InvalidCpfCnpjException("00000000000"));

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("CPF/CNPJ inválido"));
        }
    }
}