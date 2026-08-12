package br.com.ofisy.application.customer.activate;

import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import br.com.ofisy.domain.customer.exceptions.CustomerAlreadyActiveException;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ActivateCustomerServiceTest {

    private static final String VALID_CPF = "52998224725";

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private ActivateCustomerService service;

    @Nested
    class Execute {

        @Test
        void shouldActivateInactiveCustomer() {
            var id = UUID.randomUUID();
            var customer = newCustomer();
            customer.deactivate();
            when(repository.findById(id)).thenReturn(Optional.of(customer));
            when(repository.save(any(Customer.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(id);

            assertThat(result.isActive()).isTrue();
            verify(repository).save(customer);
        }

        @Test
        void shouldThrowWhenCustomerAlreadyActive() {
            var id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.of(newCustomer()));

            assertThatThrownBy(() -> service.execute(id))
                    .isInstanceOf(CustomerAlreadyActiveException.class)
                    .hasMessageContaining("já está ativo");
        }

        @Test
        void shouldThrowCustomerNotFoundExceptionWhenNotFound() {
            var id = UUID.randomUUID();
            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(id))
                    .isInstanceOf(CustomerNotFoundException.class);
        }
    }

    private Customer newCustomer() {
        return Customer.create(new CpfCnpj(VALID_CPF), "John Doe", "john@example.com", "11999999999");
    }
}