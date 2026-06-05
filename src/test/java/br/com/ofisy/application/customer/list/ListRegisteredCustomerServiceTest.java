package br.com.ofisy.application.customer.list;

import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListRegisteredCustomerServiceTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private ListRegisteredCustomerService listRegisteredCustomerService;

    @Nested
    class ListRegisteredCustomers {

        @Test
        void shouldReturnPageOfCustomers() {
            var customer = validCustomer(VALID_CPF, "John Doe", "john@example.com", "11999999999");
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(customer), pageable, 1);
            when(customerRepository.findAll(pageable)).thenReturn(page);

            var result = listRegisteredCustomerService.execute(pageable);

            assertThat(result.getContent()).hasSize(1);
            var found = result.getContent().getFirst();
            assertThat(found.getCpfCnpj().getValue()).isEqualTo(VALID_CPF);
            assertThat(found.getName()).isEqualTo("John Doe");
            assertThat(found.getEmail()).isEqualTo("john@example.com");
            assertThat(found.getPhone()).isEqualTo("11999999999");
            assertThat(found.getCreatedAt()).isNotNull();
            assertThat(found.getUpdatedAt()).isNotNull();
        }

        @Test
        void shouldReturnEmptyPageWhenNoCustomersRegistered() {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<Customer>(Collections.emptyList(), pageable, 0);
            when(customerRepository.findAll(pageable)).thenReturn(emptyPage);

            var result = listRegisteredCustomerService.execute(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        void shouldReturnMultipleCustomers() {
            var customer1 = validCustomer(VALID_CPF, "Alice", "alice@mail.com", "111");
            var customer2 = validCustomer(VALID_CNPJ, "Bob", "bob@mail.com", "222");
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(customer1, customer2), pageable, 2);
            when(customerRepository.findAll(pageable)).thenReturn(page);

            var result = listRegisteredCustomerService.execute(pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Alice");
            assertThat(result.getContent().get(1).getName()).isEqualTo("Bob");
        }

        @Test
        void shouldDelegatePageableToRepository() {
            var pageable = PageRequest.of(2, 5);
            var emptyPage = new PageImpl<Customer>(Collections.emptyList(), pageable, 0);
            when(customerRepository.findAll(pageable)).thenReturn(emptyPage);

            listRegisteredCustomerService.execute(pageable);

            verify(customerRepository).findAll(pageable);
        }
    }

    private Customer validCustomer(String cpfCnpj, String name, String email, String phone) {
        return Customer.create(new CpfCnpj(cpfCnpj), name, email, phone);
    }
}