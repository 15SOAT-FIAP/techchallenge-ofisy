package br.com.ofisy.application.serviceOrderService.dto;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderServiceRequestDTOTest {

    @Test
    void shouldCreateDTOWithValidData() {
        var serviceId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto = new ServiceOrderServiceRequestDTO(serviceId, serviceOrderId);
        
        assertThat(dto).isNotNull();
        assertThat(dto.serviceId()).isEqualTo(serviceId);
        assertThat(dto.serviceOrderId()).isEqualTo(serviceOrderId);
    }

    @Test
    void shouldHaveServiceIdField() {
        var serviceId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto = new ServiceOrderServiceRequestDTO(serviceId, serviceOrderId);
        
        assertThat(dto.serviceId()).isNotNull();
        assertThat(dto.serviceId()).isEqualTo(serviceId);
    }

    @Test
    void shouldHaveServiceOrderIdField() {
        var serviceId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto = new ServiceOrderServiceRequestDTO(serviceId, serviceOrderId);
        
        assertThat(dto.serviceOrderId()).isNotNull();
        assertThat(dto.serviceOrderId()).isEqualTo(serviceOrderId);
    }

    @Test
    void shouldBeEqualWithSameValues() {
        var serviceId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto1 = new ServiceOrderServiceRequestDTO(serviceId, serviceOrderId);
        var dto2 = new ServiceOrderServiceRequestDTO(serviceId, serviceOrderId);
        
        assertThat(dto1).isEqualTo(dto2);
    }

    @Test
    void shouldNotBeEqualWithDifferentServiceId() {
        var serviceOrderId = UUID.randomUUID();
        
        var dto1 = new ServiceOrderServiceRequestDTO(UUID.randomUUID(), serviceOrderId);
        var dto2 = new ServiceOrderServiceRequestDTO(UUID.randomUUID(), serviceOrderId);
        
        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void shouldNotBeEqualWithDifferentServiceOrderId() {
        var serviceId = UUID.randomUUID();
        
        var dto1 = new ServiceOrderServiceRequestDTO(serviceId, UUID.randomUUID());
        var dto2 = new ServiceOrderServiceRequestDTO(serviceId, UUID.randomUUID());
        
        assertThat(dto1).isNotEqualTo(dto2);
    }

    @Test
    void shouldHaveSameHashCodeWithSameValues() {
        var serviceId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto1 = new ServiceOrderServiceRequestDTO(serviceId, serviceOrderId);
        var dto2 = new ServiceOrderServiceRequestDTO(serviceId, serviceOrderId);
        
        assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
    }

    @Test
    void shouldHaveProperStringRepresentation() {
        var serviceId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto = new ServiceOrderServiceRequestDTO(serviceId, serviceOrderId);
        var toString = dto.toString();
        
        assertThat(toString).contains("serviceId");
        assertThat(toString).contains("serviceOrderId");
        assertThat(toString).contains(serviceId.toString());
        assertThat(toString).contains(serviceOrderId.toString());
    }

    @Test
    void shouldBeRecordType() {
        var serviceId = UUID.randomUUID();
        var serviceOrderId = UUID.randomUUID();
        
        var dto = new ServiceOrderServiceRequestDTO(serviceId, serviceOrderId);
        
        assertThat(dto).isNotNull();
        assertThat(dto.getClass().isRecord()).isTrue();
    }
}

