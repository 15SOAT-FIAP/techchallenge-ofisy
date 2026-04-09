package br.com.ofisy.domain.customer;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

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
}