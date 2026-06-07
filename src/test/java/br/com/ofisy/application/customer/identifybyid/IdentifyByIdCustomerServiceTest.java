package br.com.ofisy.application.customer.identifybyid;

import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentifyByIdCustomerServiceTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_NAME = "John Doe";
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PHONE = "11999999999";

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private IdentifyByIdCustomerService identifyByIdCustomerService;

    @Nested
    class IdentifyCustomerById {

        @Test
        void shouldReturnCustomerWhenIdExists() {
            var id = UUID.randomUUID();
            var customer = validCustomer();
            when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

            var result = identifyByIdCustomerService.execute(id);

            assertThat(result).isNotNull();
            assertThat(result.getCpfCnpj().getValue()).isEqualTo(VALID_CPF);
            assertThat(result.getName()).isEqualTo(VALID_NAME);
            assertThat(result.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(result.getPhone()).isEqualTo(VALID_PHONE);
        }

        @Test
        void shouldThrowCustomerNotFoundExceptionWhenIdDoesNotExist() {
            var id = UUID.randomUUID();
            when(customerRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> identifyByIdCustomerService.execute(id))
                    .isInstanceOf(CustomerNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> identifyByIdCustomerService.execute(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    private Customer validCustomer() {
        return Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);
    }
}