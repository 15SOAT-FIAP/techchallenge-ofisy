package br.com.ofisy.application.customer;

import br.com.ofisy.application.customer.dto.CustomerRequestDTO;
import br.com.ofisy.application.customer.exceptions.CustomerCpfCnpjNotFoundException;
import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import br.com.ofisy.domain.customer.exceptions.InvalidCpfCnpjException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";
    private static final String VALID_NAME = "John Doe";
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PHONE = "11999999999";

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    private CustomerRequestDTO validRequest() {
        return new CustomerRequestDTO(VALID_CPF, VALID_NAME, VALID_EMAIL, VALID_PHONE);
    }

    private Customer validCustomer() {
        return Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);
    }

    @Nested
    class RegisterCustomer {

        @ParameterizedTest
        @ValueSource(strings = {"52998224725", "11222333000181"})
        void shouldSaveCustomerWithValidCpfOrCnpj(String cpfCnpj) {
            var request = new CustomerRequestDTO(cpfCnpj, VALID_NAME, VALID_EMAIL, VALID_PHONE);

            customerService.registerCustomer(request);

            var captor = ArgumentCaptor.forClass(Customer.class);
            verify(customerRepository).save(captor.capture());
            var saved = captor.getValue();
            assertThat(saved.getCpfCnpj().getValue()).isEqualTo(cpfCnpj);
            assertThat(saved.getName()).isEqualTo(VALID_NAME);
            assertThat(saved.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(saved.getPhone()).isEqualTo(VALID_PHONE);
        }

        @Test
        void shouldCallRepositorySaveExactlyOnce() {
            customerService.registerCustomer(validRequest());

            verify(customerRepository, times(1)).save(any(Customer.class));
        }
    }

    @Nested
    class ListRegisteredCustomers {

        @Test
        void shouldReturnPageOfCustomerDTOs() {
            var customer = validCustomer();
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(customer), pageable, 1);
            when(customerRepository.findAll(pageable)).thenReturn(page);

            var result = customerService.listRegisteredCustomers(pageable);

            assertThat(result.getContent()).hasSize(1);
            var dto = result.getContent().getFirst();
            assertThat(dto.cpfCnpj()).isEqualTo(VALID_CPF);
            assertThat(dto.name()).isEqualTo(VALID_NAME);
            assertThat(dto.email()).isEqualTo(VALID_EMAIL);
            assertThat(dto.phone()).isEqualTo(VALID_PHONE);
            assertThat(dto.createdAt()).isNotNull();
            assertThat(dto.updatedAt()).isNotNull();
        }

        @Test
        void shouldReturnEmptyPageWhenNoCustomers() {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<Customer>(Collections.emptyList(), pageable, 0);
            when(customerRepository.findAll(pageable)).thenReturn(emptyPage);

            var result = customerService.listRegisteredCustomers(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        void shouldReturnMultipleCustomers() {
            var customer1 = Customer.create(new CpfCnpj(VALID_CPF), "Alice", "alice@mail.com", "111");
            var customer2 = Customer.create(new CpfCnpj(VALID_CNPJ), "Bob", "bob@mail.com", "222");
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(customer1, customer2), pageable, 2);
            when(customerRepository.findAll(pageable)).thenReturn(page);

            var result = customerService.listRegisteredCustomers(pageable);

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).name()).isEqualTo("Alice");
            assertThat(result.getContent().get(1).name()).isEqualTo("Bob");
        }
    }

    @Nested
    class IdentifyCustomerById {

        @Test
        void shouldReturnCustomerDTOWhenFound() {
            var id = UUID.randomUUID();
            var customer = validCustomer();
            when(customerRepository.findById(id)).thenReturn(Optional.of(customer));

            var result = customerService.identifyCustomerById(id);

            assertThat(result).isNotNull();
            assertThat(result.cpfCnpj()).isEqualTo(VALID_CPF);
            assertThat(result.name()).isEqualTo(VALID_NAME);
            assertThat(result.email()).isEqualTo(VALID_EMAIL);
            assertThat(result.phone()).isEqualTo(VALID_PHONE);
        }

        @Test
        void shouldThrowCustomerNotFoundExceptionWhenNotFound() {
            var id = UUID.randomUUID();
            when(customerRepository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.identifyCustomerById(id))
                    .isInstanceOf(CustomerNotFoundException.class)
                    .hasMessageContaining(id.toString());
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> customerService.identifyCustomerById(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class IdentifyCustomerByCpfCnpj {

        @ParameterizedTest
        @ValueSource(strings = {"52998224725", "11222333000181"})
        void shouldReturnCustomerDTOWhenFound(String cpfCnpj) {
            var customer = Customer.create(new CpfCnpj(cpfCnpj), VALID_NAME, VALID_EMAIL, VALID_PHONE);
            when(customerRepository.findByCpfCnpj(any(CpfCnpj.class))).thenReturn(Optional.of(customer));

            var result = customerService.identifyCustomerByCpfCnpj(cpfCnpj);

            assertThat(result).isNotNull();
            assertThat(result.cpfCnpj()).isEqualTo(cpfCnpj);
            assertThat(result.name()).isEqualTo(VALID_NAME);
        }

        @Test
        void shouldThrowCustomerCpfCnpjNotFoundExceptionWhenNotFound() {
            when(customerRepository.findByCpfCnpj(any(CpfCnpj.class))).thenReturn(Optional.empty());

            assertThatThrownBy(() -> customerService.identifyCustomerByCpfCnpj(VALID_CPF))
                    .isInstanceOf(CustomerCpfCnpjNotFoundException.class)
                    .hasMessageContaining(VALID_CPF);
        }

        @ParameterizedTest
        @ValueSource(strings = {"00000000000", "", "   ", "123", "abcdefghijk"})
        void shouldThrowInvalidCpfCnpjExceptionForInvalidInput(String invalidCpfCnpj) {
            assertThatThrownBy(() -> customerService.identifyCustomerByCpfCnpj(invalidCpfCnpj))
                    .isInstanceOf(InvalidCpfCnpjException.class);
        }

        @Test
        void shouldThrowInvalidCpfCnpjExceptionForNullCpfCnpj() {
            assertThatThrownBy(() -> customerService.identifyCustomerByCpfCnpj(null))
                    .isInstanceOf(InvalidCpfCnpjException.class);
        }
    }
}