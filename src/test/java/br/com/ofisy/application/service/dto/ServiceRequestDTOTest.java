package br.com.ofisy.application.service.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceRequestDTOTest {

    @Test
    void shouldCreateDTOWithValidData() {
        var price = new BigDecimal("150.00");
        var name = "Troca de Óleo";
        var description = "Troca completa de óleo do motor";

        var dto = new ServiceRequestDTO(price, name, description);

        assertThat(dto).isNotNull();
        assertThat(dto.price()).isEqualTo(price);
        assertThat(dto.name()).isEqualTo(name);
        assertThat(dto.description()).isEqualTo(description);
    }

    @Test
    void shouldHavePriceField() {
        var price = new BigDecimal("299.99");

        var dto = new ServiceRequestDTO(price, "Nome", "Descrição");

        assertThat(dto.price()).isNotNull();
        assertThat(dto.price()).isEqualTo(price);
    }

    @Test
    void shouldHaveNameField() {
        var name = "Troca de Pneus";

        var dto = new ServiceRequestDTO(new BigDecimal("100.00"), name, "Descrição");

        assertThat(dto.name()).isNotNull();
        assertThat(dto.name()).isEqualTo(name);
    }

    @Test
    void shouldHaveDescriptionField() {
        var description = "Troca completa dos quatro pneus";

        var dto = new ServiceRequestDTO(new BigDecimal("100.00"), "Nome", description);

        assertThat(dto.description()).isNotNull();
        assertThat(dto.description()).isEqualTo(description);
    }

    @Test
    void shouldBeEqualWithSameValues() {
        var price = new BigDecimal("150.00");
        var name = "Serviço";
        var description = "Descrição";

        var dto1 = new ServiceRequestDTO(price, name, description);
        var dto2 = new ServiceRequestDTO(price, name, description);

        assertThat(dto1).isEqualTo(dto2);
    }

    @Test
    void shouldNotBeEqualWithDifferentPrice() {
        var dto1 = new ServiceRequestDTO(new BigDecimal("100.00"), "Nome", "Descrição");
        var dto2 = new ServiceRequestDTO(new BigDecimal("200.00"), "Nome", "Descrição");

        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void shouldNotBeEqualWithDifferentName() {
        var dto1 = new ServiceRequestDTO(new BigDecimal("100.00"), "Nome1", "Descrição");
        var dto2 = new ServiceRequestDTO(new BigDecimal("100.00"), "Nome2", "Descrição");

        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void shouldNotBeEqualWithDifferentDescription() {
        var dto1 = new ServiceRequestDTO(new BigDecimal("100.00"), "Nome", "Descrição1");
        var dto2 = new ServiceRequestDTO(new BigDecimal("100.00"), "Nome", "Descrição2");

        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void shouldHaveSameHashCodeWithSameValues() {
        var price = new BigDecimal("150.00");
        var name = "Serviço";
        var description = "Descrição";

        var dto1 = new ServiceRequestDTO(price, name, description);
        var dto2 = new ServiceRequestDTO(price, name, description);

        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void shouldHaveProperStringRepresentation() {
        var dto = new ServiceRequestDTO(new BigDecimal("150.00"), "Serviço", "Descrição");
        var toString = dto.toString();

        assertThat(toString).contains("price");
        assertThat(toString).contains("name");
        assertThat(toString).contains("description");
        assertThat(toString).contains("150.00");
        assertThat(toString).contains("Serviço");
        assertThat(toString).contains("Descrição");
    }

    @Test
    void shouldBeRecordType() {
        var dto = new ServiceRequestDTO(new BigDecimal("100.00"), "Nome", "Descrição");

        assertThat(dto).isNotNull();
        assertThat(dto.getClass().isRecord()).isTrue();
    }

    @Test
    void shouldAcceptZeroPrice() {
        var dto = new ServiceRequestDTO(BigDecimal.ZERO, "Nome", "Descrição");

        assertThat(dto.price()).isZero();
    }

    @Test
    void shouldAcceptNegativePrice() {
        var dto = new ServiceRequestDTO(new BigDecimal("-50.00"), "Nome", "Descrição");

        assertThat(dto.price()).isNegative();
    }

    @Test
    void shouldAcceptEmptyName() {
        var dto = new ServiceRequestDTO(new BigDecimal("100.00"), "", "Descrição");

        assertThat(dto.name()).isEmpty();
    }

    @Test
    void shouldAcceptEmptyDescription() {
        var dto = new ServiceRequestDTO(new BigDecimal("100.00"), "Nome", "");

        assertThat(dto.description()).isEmpty();
    }
}

