package br.com.ofisy.application.customer.register;

import br.com.ofisy.application.customer.exceptions.CustomerAlreadyExistsException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterCustomerServiceTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";
    private static final String VALID_NAME = "John Doe";
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PHONE = "11999999999";

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private RegisterCustomerService registerCustomerService;

    @Nested
    class RegisterCustomer {

        @ParameterizedTest
        @ValueSource(strings = {VALID_CPF, VALID_CNPJ})
        void shouldSaveAndReturnCustomerWhenCpfCnpjIsNew(String cpfCnpj) {
            var cmd = validCommand(cpfCnpj);
            when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = registerCustomerService.execute(cmd);

            var captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerRepository).save(captor.capture());
            var saved = captor.getValue();
            assertThat(saved.getCpfCnpj().getValue()).isEqualTo(cpfCnpj);
            assertThat(saved.getName()).isEqualTo(VALID_NAME);
            assertThat(saved.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(saved.getPhone()).isEqualTo(VALID_PHONE);

            assertThat(result).isNotNull();
            assertThat(result.getCpfCnpj().getValue()).isEqualTo(cpfCnpj);
            assertThat(result.getName()).isEqualTo(VALID_NAME);
            assertThat(result.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(result.getPhone()).isEqualTo(VALID_PHONE);
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();
        }

        @Test
        void shouldCallRepositorySaveExactlyOnce() {
            when(customerRepository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

            registerCustomerService.execute(validCommand(VALID_CPF));

            verify(customerRepository, times(1)).save(any(Customer.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {VALID_CPF, VALID_CNPJ})
        void shouldThrowCustomerAlreadyExistsExceptionWhenCpfCnpjAlreadyRegistered(String cpfCnpj) {
            when(customerRepository.findByCpfCnpj(any(CpfCnpj.class))).thenReturn(Optional.of(validCustomer()));

            var command = validCommand(cpfCnpj);

            assertThatThrownBy(() -> registerCustomerService.execute(command))
                    .isInstanceOf(CustomerAlreadyExistsException.class)
                    .hasMessageContaining(cpfCnpj);

            verify(customerRepository, never()).save(any(Customer.class));
        }
    }

    private RegisterCustomerUseCase.RegisterCustomerCommand validCommand(String cpfCnpj) {
        return new RegisterCustomerUseCase.RegisterCustomerCommand(cpfCnpj, VALID_NAME, VALID_EMAIL, VALID_PHONE);
    }

    private Customer validCustomer() {
        return Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);
    }
}