package br.com.ofisy.infrastructure.persistence.customer;

import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerRepositoryImplTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_NAME = "John Doe";
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PHONE = "11999999999";

    @Mock
    private JpaCustomerRepository jpa;

    @InjectMocks
    private CustomerRepositoryImpl repository;

    private Customer validCustomer() {
        return Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);
    }

    @Nested
    class Save {

        @Test
        void shouldDelegateToJpaAndReturnSavedCustomer() {
            var customer = validCustomer();
            when(jpa.save(customer)).thenReturn(customer);

            var result = repository.save(customer);

            assertThat(result).isSameAs(customer);
            verify(jpa).save(customer);
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldDelegateToJpaWithPageable() {
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(validCustomer()), pageable, 1);
            when(jpa.findAll(pageable)).thenReturn(page);

            var result = repository.findAll(pageable);

            assertThat(result).isEqualTo(page);
            verify(jpa).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoCustomers() {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<Customer>(List.of(), pageable, 0);
            when(jpa.findAll(pageable)).thenReturn(emptyPage);

            var result = repository.findAll(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnCustomerWhenFound() {
            var id = UUID.randomUUID();
            var customer = validCustomer();
            when(jpa.findById(id)).thenReturn(Optional.of(customer));

            var result = repository.findById(id);

            assertThat(result).isPresent().contains(customer);
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            var id = UUID.randomUUID();
            when(jpa.findById(id)).thenReturn(Optional.empty());

            var result = repository.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByCpfCnpj {

        @ParameterizedTest
        @ValueSource(strings = {"52998224725", "11222333000181"})
        void shouldReturnCustomerWhenFound(String cpfCnpj) {
            var cpfCnpjObj = new CpfCnpj(cpfCnpj);
            var customer = Customer.create(cpfCnpjObj, VALID_NAME, VALID_EMAIL, VALID_PHONE);
            when(jpa.findByCpfCnpjValue(cpfCnpj)).thenReturn(Optional.of(customer));

            var result = repository.findByCpfCnpj(cpfCnpjObj);

            assertThat(result).isPresent().contains(customer);
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            var cpfCnpj = new CpfCnpj(VALID_CPF);
            when(jpa.findByCpfCnpjValue(VALID_CPF)).thenReturn(Optional.empty());

            var result = repository.findByCpfCnpj(cpfCnpj);

            assertThat(result).isEmpty();
            verify(jpa).findByCpfCnpjValue(VALID_CPF);
        }
    }
}