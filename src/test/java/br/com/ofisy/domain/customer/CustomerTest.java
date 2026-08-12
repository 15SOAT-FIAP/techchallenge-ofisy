package br.com.ofisy.domain.customer;

import br.com.ofisy.domain.customer.exceptions.CustomerAlreadyActiveException;
import br.com.ofisy.domain.customer.exceptions.CustomerAlreadyInactiveException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CustomerTest {

    private static final String VALID_CPF = "52998224725";
    private static final String VALID_NAME = "John Doe";
    private static final String VALID_EMAIL = "john@example.com";
    private static final String VALID_PHONE = "11999999999";

    @Test
    void shouldCreateCustomerWithValidData() {
        var cpfCnpj = new CpfCnpj(VALID_CPF);

        var customer = Customer.create(cpfCnpj, VALID_NAME, VALID_EMAIL, VALID_PHONE);

        assertThat(customer).isNotNull();
        assertThat(customer.getCpfCnpj()).isEqualTo(cpfCnpj);
        assertThat(customer.getName()).isEqualTo(VALID_NAME);
        assertThat(customer.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(customer.getPhone()).isEqualTo(VALID_PHONE);
    }

    @Test
    void shouldCreateCustomerWithValidCnpj() {
        var cpfCnpj = new CpfCnpj("11222333000181");

        var customer = Customer.create(cpfCnpj, VALID_NAME, VALID_EMAIL, VALID_PHONE);

        assertThat(customer).isNotNull();
        assertThat(customer.getCpfCnpj().getValue()).isEqualTo("11222333000181");
    }

    @Test
    void shouldNotGenerateIdBeforePersistence() {
        var cpfCnpj = new CpfCnpj(VALID_CPF);

        var customer = Customer.create(cpfCnpj, VALID_NAME, VALID_EMAIL, VALID_PHONE);

        assertThat(customer.getId()).isNull();
    }

    @Test
    void shouldSetCreatedAtOnCreation() {
        var cpfCnpj = new CpfCnpj(VALID_CPF);
        var before = LocalDateTime.now();

        var customer = Customer.create(cpfCnpj, VALID_NAME, VALID_EMAIL, VALID_PHONE);

        var after = LocalDateTime.now();
        assertThat(customer.getCreatedAt()).isNotNull();
        assertThat(customer.getCreatedAt()).isBetween(before, after);
    }

    @Test
    void shouldSetUpdatedAtOnCreation() {
        var cpfCnpj = new CpfCnpj(VALID_CPF);
        var before = LocalDateTime.now();

        var customer = Customer.create(cpfCnpj, VALID_NAME, VALID_EMAIL, VALID_PHONE);

        var after = LocalDateTime.now();
        assertThat(customer.getUpdatedAt()).isNotNull();
        assertThat(customer.getUpdatedAt()).isBetween(before, after);
    }

    @Test
    void shouldCreateCustomerWithNullCpfCnpj() {
        var customer = Customer.create(null, VALID_NAME, VALID_EMAIL, VALID_PHONE);

        assertThat(customer.getCpfCnpj()).isNull();
    }

    @Test
    void shouldCreateCustomerWithNullName() {
        var cpfCnpj = new CpfCnpj(VALID_CPF);

        var customer = Customer.create(cpfCnpj, null, VALID_EMAIL, VALID_PHONE);

        assertThat(customer.getName()).isNull();
    }

    @Test
    void shouldCreateCustomerWithNullEmail() {
        var cpfCnpj = new CpfCnpj(VALID_CPF);

        var customer = Customer.create(cpfCnpj, VALID_NAME, null, VALID_PHONE);

        assertThat(customer.getEmail()).isNull();
    }

    @Test
    void shouldCreateCustomerWithNullPhone() {
        var cpfCnpj = new CpfCnpj(VALID_CPF);

        var customer = Customer.create(cpfCnpj, VALID_NAME, VALID_EMAIL, null);

        assertThat(customer.getPhone()).isNull();
    }

    @Test
    void shouldCreateCustomerWithAllNullFields() {
        var customer = Customer.create(null, null, null, null);

        assertThat(customer).isNotNull();
        assertThat(customer.getCpfCnpj()).isNull();
        assertThat(customer.getName()).isNull();
        assertThat(customer.getEmail()).isNull();
        assertThat(customer.getPhone()).isNull();
    }

    @Test
    void shouldCreateCustomerWithEmptyStringFields() {
        var cpfCnpj = new CpfCnpj(VALID_CPF);

        var customer = Customer.create(cpfCnpj, "", "", "");

        assertThat(customer.getName()).isEmpty();
        assertThat(customer.getEmail()).isEmpty();
        assertThat(customer.getPhone()).isEmpty();
    }

    @Nested
    class Reconstruct {

        @Test
        void shouldReconstructCustomerWithAllFields() {
            var id = UUID.randomUUID();
            var cpfCnpj = new CpfCnpj(VALID_CPF);
            var createdAt = LocalDateTime.of(2024, 1, 10, 10, 0);
            var updatedAt = LocalDateTime.of(2024, 1, 15, 12, 0);

            var customer = Customer.reconstruct(id, cpfCnpj, VALID_NAME, VALID_EMAIL, VALID_PHONE, false, createdAt, updatedAt);

            assertThat(customer.getId()).isEqualTo(id);
            assertThat(customer.isActive()).isFalse();
            assertThat(customer.getCpfCnpj()).isEqualTo(cpfCnpj);
            assertThat(customer.getName()).isEqualTo(VALID_NAME);
            assertThat(customer.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(customer.getPhone()).isEqualTo(VALID_PHONE);
            assertThat(customer.getCreatedAt()).isEqualTo(createdAt);
            assertThat(customer.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        void shouldNotOverwriteTimestampsWithNowWhenReconstructing() {
            var id = UUID.randomUUID();
            var createdAt = LocalDateTime.of(2020, 6, 1, 8, 0);
            var updatedAt = LocalDateTime.of(2021, 3, 15, 9, 30);

            var customer = Customer.reconstruct(id, new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE,
                    true, createdAt, updatedAt);

            assertThat(customer.getCreatedAt()).isEqualTo(createdAt);
            assertThat(customer.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        void shouldReconstructWithNullId() {
            var createdAt = LocalDateTime.now();
            var updatedAt = LocalDateTime.now();

            var customer = Customer.reconstruct(null, new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE,
                    true, createdAt, updatedAt);

            assertThat(customer.getId()).isNull();
        }
    }

    @Nested
    class Activation {

        @Test
        void shouldCreateCustomerAsActive() {
            var customer = Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);

            assertThat(customer.isActive()).isTrue();
        }

        @Test
        void shouldDeactivateActiveCustomer() {
            var customer = Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);

            customer.deactivate();

            assertThat(customer.isActive()).isFalse();
        }

        @Test
        void shouldActivateInactiveCustomer() {
            var customer = Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);
            customer.deactivate();

            customer.activate();

            assertThat(customer.isActive()).isTrue();
        }

        @Test
        void shouldRefreshUpdatedAtOnDeactivate() {
            var customer = Customer.reconstruct(UUID.randomUUID(), new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL,
                    VALID_PHONE, true, LocalDateTime.of(2020, 1, 1, 0, 0), LocalDateTime.of(2020, 1, 1, 0, 0));

            customer.deactivate();

            assertThat(customer.getUpdatedAt()).isAfter(LocalDateTime.of(2020, 1, 1, 0, 0));
        }

        @Test
        void shouldThrowWhenActivatingAlreadyActiveCustomer() {
            var customer = Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);

            assertThatThrownBy(customer::activate)
                    .isInstanceOf(CustomerAlreadyActiveException.class)
                    .hasMessageContaining("já está ativo");
        }

        @Test
        void shouldThrowWhenDeactivatingAlreadyInactiveCustomer() {
            var customer = Customer.create(new CpfCnpj(VALID_CPF), VALID_NAME, VALID_EMAIL, VALID_PHONE);
            customer.deactivate();

            assertThatThrownBy(customer::deactivate)
                    .isInstanceOf(CustomerAlreadyInactiveException.class)
                    .hasMessageContaining("já está desativado");
        }
    }
}
