package br.com.ofisy.application.customer.identifybycpfcnpj;

import br.com.ofisy.application.customer.exceptions.CustomerCpfCnpjNotFoundException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import br.com.ofisy.domain.customer.exceptions.InvalidCpfCnpjException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentifyByCpfCnpjCustomerServiceTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";
    private static final String VALID_NAME = "John Doe";
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PHONE = "11999999999";

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private IdentifyByCpfCnpjCustomerService identifyByCpfCnpjCustomerService;

    @Nested
    class IdentifyCustomerByCpfCnpj {

        @ParameterizedTest
        @ValueSource(strings = {VALID_CPF, VALID_CNPJ})
        void shouldReturnCustomerWhenCpfCnpjExists(String cpfCnpj) {
            var customer = Customer.create(new CpfCnpj(cpfCnpj), VALID_NAME, VALID_EMAIL, VALID_PHONE);
            when(customerRepository.findByCpfCnpj(any(CpfCnpj.class))).thenReturn(Optional.of(customer));

            var result = identifyByCpfCnpjCustomerService.execute(cpfCnpj);

            assertThat(result).isNotNull();
            assertThat(result.getCpfCnpj().getValue()).isEqualTo(cpfCnpj);
            assertThat(result.getName()).isEqualTo(VALID_NAME);
        }

        @Test
        void shouldThrowCustomerCpfCnpjNotFoundExceptionWhenNotFound() {
            when(customerRepository.findByCpfCnpj(any(CpfCnpj.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> identifyByCpfCnpjCustomerService.execute(VALID_CPF))
                    .isInstanceOf(CustomerCpfCnpjNotFoundException.class)
                    .hasMessageContaining(VALID_CPF);
        }

        @ParameterizedTest
        @ValueSource(strings = {"00000000000", "", "   ", "123", "abcdefghijk"})
        void shouldThrowInvalidCpfCnpjExceptionForInvalidInput(String invalidCpfCnpj) {
            assertThatThrownBy(() -> identifyByCpfCnpjCustomerService.execute(invalidCpfCnpj))
                    .isInstanceOf(InvalidCpfCnpjException.class);
        }

        @Test
        void shouldThrowInvalidCpfCnpjExceptionWhenNull() {
            assertThatThrownBy(() -> identifyByCpfCnpjCustomerService.execute(null))
                    .isInstanceOf(InvalidCpfCnpjException.class);
        }
    }
}