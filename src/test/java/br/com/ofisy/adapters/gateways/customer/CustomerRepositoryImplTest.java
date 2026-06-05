package br.com.ofisy.adapters.gateways.customer;

import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerRepositoryImplTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";
    private static final String VALID_NAME = "John Doe";
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PHONE = "11999999999";

    @Mock
    private JpaCustomerRepository jpa;

    @InjectMocks
    private CustomerRepositoryImpl repository;

    @Nested
    class Save {

        @Test
        void shouldConvertToEntityBeforeSaving() {
            var customer = validCustomer();
            var savedEntity = validEntity();
            when(jpa.save(any(CustomerEntity.class))).thenReturn(savedEntity);

            repository.save(customer);

            var captor = ArgumentCaptor.forClass(CustomerEntity.class);
            verify(jpa).save(captor.capture());
            assertThat(captor.getValue().getCpfCnpj()).isEqualTo(VALID_CPF);
            assertThat(captor.getValue().getName()).isEqualTo(VALID_NAME);
            assertThat(captor.getValue().getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(captor.getValue().getPhone()).isEqualTo(VALID_PHONE);
        }

        @Test
        void shouldReturnDomainCustomerWithIdAfterSave() {
            var customer = validCustomer();
            var savedEntity = validEntity();
            when(jpa.save(any(CustomerEntity.class))).thenReturn(savedEntity);

            var result = repository.save(customer);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(savedEntity.getId());
            assertThat(result.getName()).isEqualTo(savedEntity.getName());
            assertThat(result.getEmail()).isEqualTo(savedEntity.getEmail());
            assertThat(result.getPhone()).isEqualTo(savedEntity.getPhone());
            assertThat(result.getCpfCnpj().getValue()).isEqualTo(savedEntity.getCpfCnpj());
        }

        @Test
        void shouldReturnCustomerWithTimestampsFromPersistedEntity() {
            var customer = validCustomer();
            var savedEntity = validEntity();
            when(jpa.save(any(CustomerEntity.class))).thenReturn(savedEntity);

            var result = repository.save(customer);

            assertThat(result.getCreatedAt()).isEqualTo(savedEntity.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(savedEntity.getUpdatedAt());
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldReturnPageOfDomainCustomersMappedFromEntities() {
            var pageable = PageRequest.of(0, 10);
            var entity = validEntity();
            var page = new PageImpl<>(List.of(entity), pageable, 1);
            when(jpa.findAll(pageable)).thenReturn(page);

            var result = repository.findAll(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getId()).isEqualTo(entity.getId());
            assertThat(result.getContent().get(0).getName()).isEqualTo(entity.getName());
            verify(jpa).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoCustomers() {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<CustomerEntity>(List.of(), pageable, 0);
            when(jpa.findAll(pageable)).thenReturn(emptyPage);

            var result = repository.findAll(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnDomainCustomerWhenEntityFound() {
            var id = UUID.randomUUID();
            var entity = validEntity();
            when(jpa.findById(id)).thenReturn(Optional.of(entity));

            var result = repository.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(entity.getId());
            assertThat(result.get().getName()).isEqualTo(entity.getName());
            assertThat(result.get().getCpfCnpj().getValue()).isEqualTo(entity.getCpfCnpj());
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
        @ValueSource(strings = {VALID_CPF, VALID_CNPJ})
        void shouldReturnDomainCustomerWhenFound(String cpfCnpj) {
            var cpfCnpjObj = new CpfCnpj(cpfCnpj);
            var entity = CustomerEntity.builder()
                    .id(UUID.randomUUID())
                    .cpfCnpj(cpfCnpj)
                    .name(VALID_NAME)
                    .email(VALID_EMAIL)
                    .phone(VALID_PHONE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            when(jpa.findByCpfCnpj(cpfCnpj)).thenReturn(Optional.of(entity));

            var result = repository.findByCpfCnpj(cpfCnpjObj);

            assertThat(result).isPresent();
            assertThat(result.get().getCpfCnpj().getValue()).isEqualTo(cpfCnpj);
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            var cpfCnpj = new CpfCnpj(VALID_CPF);
            when(jpa.findByCpfCnpj(VALID_CPF)).thenReturn(Optional.empty());

            var result = repository.findByCpfCnpj(cpfCnpj);

            assertThat(result).isEmpty();
            verify(jpa).findByCpfCnpj(VALID_CPF);
        }
    }

    private Customer validCustomer() {
        return Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);
    }

    private CustomerEntity validEntity() {
        return CustomerEntity.builder()
                .id(UUID.randomUUID())
                .cpfCnpj(VALID_CPF)
                .name(VALID_NAME)
                .email(VALID_EMAIL)
                .phone(VALID_PHONE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}