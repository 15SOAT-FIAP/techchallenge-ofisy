package br.com.ofisy.adapters.presenter.customer;

import br.com.ofisy.adapters.controllers.customer.dto.CustomerResponseDTO;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerPresenterTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_CNPJ = "11222333000181";
    private static final String VALID_NAME = "John Doe";
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PHONE = "11999999999";

    @Nested
    class Present {

        @ParameterizedTest
        @ValueSource(strings = {VALID_CPF, VALID_CNPJ})
        void shouldMapAllFieldsCorrectly(String cpfCnpj) {
            var customer = Customer.create(new CpfCnpj(cpfCnpj), VALID_NAME, VALID_EMAIL, VALID_PHONE);

            CustomerResponseDTO dto = CustomerPresenter.present(customer);

            assertThat(dto).isNotNull();
            assertThat(dto.cpfCnpj()).isEqualTo(cpfCnpj);
            assertThat(dto.name()).isEqualTo(VALID_NAME);
            assertThat(dto.email()).isEqualTo(VALID_EMAIL);
            assertThat(dto.phone()).isEqualTo(VALID_PHONE);
        }

        @Test
        void shouldSetTimestampsFromCreatedCustomer() {
            var customer = Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);

            var dto = CustomerPresenter.present(customer);

            assertThat(dto.createdAt()).isNotNull();
            assertThat(dto.updatedAt()).isNotNull();
        }

        @Test
        void shouldPreserveIdFromReconstructedCustomer() {
            var id = UUID.randomUUID();
            var createdAt = LocalDateTime.of(2024, 1, 10, 10, 0);
            var updatedAt = LocalDateTime.of(2024, 1, 15, 12, 0);
            var customer = Customer.reconstruct(id, new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE,
                    createdAt, updatedAt);

            var dto = CustomerPresenter.present(customer);

            assertThat(dto.id()).isEqualTo(id);
            assertThat(dto.createdAt()).isEqualTo(createdAt);
            assertThat(dto.updatedAt()).isEqualTo(updatedAt);
        }

        @Test
        void shouldHaveNullIdForNewCustomer() {
            var customer = Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);

            var dto = CustomerPresenter.present(customer);

            assertThat(dto.id()).isNull();
        }
    }
}