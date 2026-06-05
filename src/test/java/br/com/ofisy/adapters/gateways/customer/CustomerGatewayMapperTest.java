package br.com.ofisy.adapters.gateways.customer;

import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerGatewayMapperTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";
    private static final String VALID_NAME = "John Doe";
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PHONE = "11999999999";

    @Nested
    class ToEntity {

        @ParameterizedTest
        @ValueSource(strings = {VALID_CPF, VALID_CNPJ})
        void shouldMapAllFieldsFromDomainToEntity(String cpfCnpj) {
            var customer = Customer.create(new CpfCnpj(cpfCnpj), VALID_NAME, VALID_EMAIL, VALID_PHONE);

            var entity = CustomerMapper.toEntity(customer);

            assertThat(entity).isNotNull();
            assertThat(entity.getCpfCnpj()).isEqualTo(cpfCnpj);
            assertThat(entity.getName()).isEqualTo(VALID_NAME);
            assertThat(entity.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(entity.getPhone()).isEqualTo(VALID_PHONE);
        }

        @Test
        void shouldPreserveNullIdForNewCustomer() {
            var customer = Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);

            var entity = CustomerMapper.toEntity(customer);

            assertThat(entity.getId()).isNull();
        }

        @Test
        void shouldPreserveIdForReconstructedCustomer() {
            var id = UUID.randomUUID();
            var customer = Customer.reconstruct(id, new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE,
                    LocalDateTime.now(), LocalDateTime.now());

            var entity = CustomerMapper.toEntity(customer);

            assertThat(entity.getId()).isEqualTo(id);
        }

        @Test
        void shouldStoreCpfCnpjAsRawStringValue() {
            var customer = Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);

            var entity = CustomerMapper.toEntity(customer);

            assertThat(entity.getCpfCnpj()).isEqualTo(VALID_CPF);
            assertThat(entity.getCpfCnpj()).matches("\\d+");
        }

        @Test
        void shouldPreserveTimestampsFromDomain() {
            var createdAt = LocalDateTime.of(2024, 1, 10, 10, 0);
            var updatedAt = LocalDateTime.of(2024, 1, 15, 12, 0);
            var customer = Customer.reconstruct(UUID.randomUUID(), new CpfCnpj(VALID_CPF),
                    VALID_NAME, VALID_EMAIL, VALID_PHONE, createdAt, updatedAt);

            var entity = CustomerMapper.toEntity(customer);

            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldMapAllFieldsFromEntityToDomain() {
            var entity = validEntity();

            var customer = CustomerMapper.toDomain(entity);

            assertThat(customer).isNotNull();
            assertThat(customer.getId()).isEqualTo(entity.getId());
            assertThat(customer.getName()).isEqualTo(entity.getName());
            assertThat(customer.getEmail()).isEqualTo(entity.getEmail());
            assertThat(customer.getPhone()).isEqualTo(entity.getPhone());
        }

        @ParameterizedTest
        @ValueSource(strings = {VALID_CPF, VALID_CNPJ})
        void shouldReconstructCpfCnpjValueObjectFromString(String cpfCnpj) {
            var entity = CustomerEntity.builder()
                    .id(UUID.randomUUID())
                    .cpfCnpj(cpfCnpj)
                    .name(VALID_NAME)
                    .email(VALID_EMAIL)
                    .phone(VALID_PHONE)
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            var customer = CustomerMapper.toDomain(entity);

            assertThat(customer.getCpfCnpj()).isNotNull();
            assertThat(customer.getCpfCnpj().getValue()).isEqualTo(cpfCnpj);
        }

        @Test
        void shouldPreserveTimestampsFromEntity() {
            var entity = validEntity();

            var customer = CustomerMapper.toDomain(entity);

            assertThat(customer.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(customer.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }

        @Test
        void shouldPreserveIdFromEntity() {
            var entity = validEntity();

            var customer = CustomerMapper.toDomain(entity);

            assertThat(customer.getId()).isEqualTo(entity.getId());
        }
    }

    private CustomerEntity validEntity() {
        return CustomerEntity.builder()
                .id(UUID.randomUUID())
                .cpfCnpj(VALID_CPF)
                .name(VALID_NAME)
                .email(VALID_EMAIL)
                .phone(VALID_PHONE)
                .createdAt(LocalDateTime.of(2024, 1, 10, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 12, 0))
                .build();
    }
}