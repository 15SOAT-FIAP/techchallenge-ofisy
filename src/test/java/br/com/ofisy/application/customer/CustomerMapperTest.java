package br.com.ofisy.application.customer;

import br.com.ofisy.interfaces.api.customer.CustomerMapper;
import br.com.ofisy.interfaces.api.customer.dto.CustomerRequestDTO;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerMapperTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_NAME = "John Doe";
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PHONE = "11999999999";

    @Nested
    class ToDomain {

        @ParameterizedTest
        @ValueSource(strings = {"52998224725", "11222333000181"})
        void shouldMapCpfOrCnpjCorrectly(String cpfCnpj) {
            var request = new CustomerRequestDTO(cpfCnpj, VALID_NAME, VALID_EMAIL, VALID_PHONE);

            var customer = CustomerMapper.toDomain(request);

            assertThat(customer).isNotNull();
            assertThat(customer.getCpfCnpj().getValue()).isEqualTo(cpfCnpj);
            assertThat(customer.getName()).isEqualTo(VALID_NAME);
            assertThat(customer.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(customer.getPhone()).isEqualTo(VALID_PHONE);
        }

        @Test
        void shouldSetCreatedAtAndUpdatedAt() {
            var request = new CustomerRequestDTO(VALID_CPF, VALID_NAME, VALID_EMAIL, VALID_PHONE);

            var customer = CustomerMapper.toDomain(request);

            assertThat(customer.getCreatedAt()).isNotNull();
            assertThat(customer.getUpdatedAt()).isNotNull();
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenRequestIsNull() {
            assertThatThrownBy(() -> CustomerMapper.toDomain(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    class ToDTO {

        @ParameterizedTest
        @ValueSource(strings = {"52998224725", "11222333000181"})
        void shouldMapAllFieldsCorrectly(String cpfCnpj) {
            var customer = Customer.create(new CpfCnpj(cpfCnpj), VALID_NAME, VALID_EMAIL, VALID_PHONE);

            var dto = CustomerMapper.toDTO(customer);

            assertThat(dto).isNotNull();
            assertThat(dto.cpfCnpj()).isEqualTo(cpfCnpj);
            assertThat(dto.name()).isEqualTo(VALID_NAME);
            assertThat(dto.email()).isEqualTo(VALID_EMAIL);
            assertThat(dto.phone()).isEqualTo(VALID_PHONE);
            assertThat(dto.createdAt()).isNotNull();
            assertThat(dto.updatedAt()).isNotNull();
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenCustomerIsNull() {
            assertThatThrownBy(() -> CustomerMapper.toDTO(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenCpfCnpjIsNull() {
            var customer = Customer.create(null, VALID_NAME, VALID_EMAIL, VALID_PHONE);

            assertThatThrownBy(() -> CustomerMapper.toDTO(customer))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}