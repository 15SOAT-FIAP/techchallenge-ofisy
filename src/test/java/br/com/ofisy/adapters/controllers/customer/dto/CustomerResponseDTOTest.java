package br.com.ofisy.adapters.controllers.customer.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerResponseDTOTest {

    @Test
    void shouldStoreAllFields() {
        var now = LocalDateTime.now();
        var dto = new CustomerResponseDTO(UUID.randomUUID(), "52998224725", "John Doe", "john@example.com", "11999999999", now, now);

        assertThat(dto.cpfCnpj()).isEqualTo("52998224725");
        assertThat(dto.name()).isEqualTo("John Doe");
        assertThat(dto.email()).isEqualTo("john@example.com");
        assertThat(dto.phone()).isEqualTo("11999999999");
        assertThat(dto.createdAt()).isEqualTo(now);
        assertThat(dto.updatedAt()).isEqualTo(now);
    }

    @Test
    void shouldAllowAllNullFields() {
        var dto = new CustomerResponseDTO(null, null, null, null, null, null, null);

        assertThat(dto.cpfCnpj()).isNull();
        assertThat(dto.name()).isNull();
        assertThat(dto.email()).isNull();
        assertThat(dto.phone()).isNull();
        assertThat(dto.createdAt()).isNull();
        assertThat(dto.updatedAt()).isNull();
    }

    @Test
    void shouldBeEqualForIdenticalData() {
        var now = LocalDateTime.now();
        var dto1 = new CustomerResponseDTO(UUID.fromString("6032c49a-6e85-4e6e-9d49-0050d8471135"), "52998224725", "John", "john@mail.com", "111", now, now);
        var dto2 = new CustomerResponseDTO(UUID.fromString("6032c49a-6e85-4e6e-9d49-0050d8471135"), "52998224725", "John", "john@mail.com", "111", now, now);

        assertThat(dto1).isEqualTo(dto2);
        assertThat(dto1.hashCode()).hasSameHashCodeAs(dto2.hashCode());
    }

    @Test
    void shouldNotBeEqualForDifferentData() {
        var now = LocalDateTime.now();
        var dto1 = new CustomerResponseDTO(UUID.fromString("6032c49a-6e85-4e6e-9d49-0050d8471136"), "52998224725", "John", "john@mail.com", "111", now, now);
        var dto2 = new CustomerResponseDTO(UUID.fromString("6032c49a-6e85-4e6e-9d49-0050d8471137"), "11222333000181", "Jane", "jane@mail.com", "222", now, now);

        assertThat(dto1).isNotEqualTo(dto2);
    }
}