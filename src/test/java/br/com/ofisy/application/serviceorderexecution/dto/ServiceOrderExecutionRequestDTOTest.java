package br.com.ofisy.application.serviceorderexecution.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderExecutionRequestDTOTest {

    @Test
    void shouldCreateDTOWithValidData() {
        var serviceCatalogId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto = new ServiceOrderExecutionRequestDTO(serviceCatalogId, serviceOrderId);
        
        assertThat(dto).isNotNull();
        assertThat(dto.serviceCatalogId()).isEqualTo(serviceCatalogId);
        assertThat(dto.serviceOrderId()).isEqualTo(serviceOrderId);
    }

    @Test
    void shouldHaveServiceCatalogIdField() {
        var serviceCatalogId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto = new ServiceOrderExecutionRequestDTO(serviceCatalogId, serviceOrderId);
        
        assertThat(dto.serviceCatalogId()).isNotNull();
        assertThat(dto.serviceCatalogId()).isEqualTo(serviceCatalogId);
    }

    @Test
    void shouldHaveServiceOrderIdField() {
        var serviceCatalogId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto = new ServiceOrderExecutionRequestDTO(serviceCatalogId, serviceOrderId);
        
        assertThat(dto.serviceOrderId()).isNotNull();
        assertThat(dto.serviceOrderId()).isEqualTo(serviceOrderId);
    }

    @Test
    void shouldBeEqualWithSameValues() {
        var serviceCatalogId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto1 = new ServiceOrderExecutionRequestDTO(serviceCatalogId, serviceOrderId);
        var dto2 = new ServiceOrderExecutionRequestDTO(serviceCatalogId, serviceOrderId);
        
        assertThat(dto1).isEqualTo(dto2);
    }

    @Test
    void shouldNotBeEqualWithDifferentServiceCatalogId() {
        var serviceOrderId = UUID.randomUUID();
        
        var dto1 = new ServiceOrderExecutionRequestDTO(UUID.randomUUID(), serviceOrderId);
        var dto2 = new ServiceOrderExecutionRequestDTO(UUID.randomUUID(), serviceOrderId);
        
        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void shouldNotBeEqualWithDifferentServiceOrderId() {
        var serviceCatalogId = UUID.randomUUID();
        
        var dto1 = new ServiceOrderExecutionRequestDTO(serviceCatalogId, UUID.randomUUID());
        var dto2 = new ServiceOrderExecutionRequestDTO(serviceCatalogId, UUID.randomUUID());
        
        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void shouldHaveSameHashCodeWithSameValues() {
        var serviceCatalogId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto1 = new ServiceOrderExecutionRequestDTO(serviceCatalogId, serviceOrderId);
        var dto2 = new ServiceOrderExecutionRequestDTO(serviceCatalogId, serviceOrderId);
        
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void shouldHaveProperStringRepresentation() {
        var serviceCatalogId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto = new ServiceOrderExecutionRequestDTO(serviceCatalogId, serviceOrderId);
        var toString = dto.toString();
        
        assertThat(toString).contains("serviceCatalogId");
        assertThat(toString).contains("serviceOrderId");
        assertThat(toString).contains(serviceCatalogId.toString());
        assertThat(toString).contains(serviceOrderId.toString());
    }

    @Test
    void shouldBeRecordType() {
        var serviceCatalogId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto = new ServiceOrderExecutionRequestDTO(serviceCatalogId, serviceOrderId);
        
        assertThat(dto).isNotNull();
        assertThat(dto.getClass().isRecord()).isTrue();
    }
}

