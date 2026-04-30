package br.com.ofisy.application.serviceorderexecution;

import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionRequestDTO;
import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionResponseDTO;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ServiceOrderExecutionMapperTest {

    @Test
    void shouldConvertRequestDTOToDomain() {
        UUID serviceCatalogId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();

        ServiceOrderExecutionRequestDTO request = new ServiceOrderExecutionRequestDTO(
                serviceCatalogId,
                serviceOrderId
        );

        ServiceOrderExecution serviceOrderExecution = ServiceOrderExecutionMapper.toDomain(request);

        assertNotNull(serviceOrderExecution);
        assertEquals(serviceCatalogId, serviceOrderExecution.getServiceCatalogId());
        assertEquals(serviceOrderId, serviceOrderExecution.getServiceOrderId());
    }

    @Test
    void shouldThrowExceptionWhenRequestDTOIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceOrderExecutionMapper.toDomain(null)
        );

        assertEquals(
                "ServiceOrderExecutionRequestDTO não pode ser nulo",
                exception.getMessage()
        );
    }

    @Test
    void shouldConvertDomainToResponseDTO() {
        UUID serviceCatalogId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();

        ServiceOrderExecution serviceOrderExecution = ServiceOrderExecution.create(
                serviceCatalogId,
                serviceOrderId
        );

        ServiceOrderExecutionResponseDTO response =
                ServiceOrderExecutionMapper.toDTO(serviceOrderExecution);

        assertNotNull(response);
        assertEquals(serviceOrderExecution.getId(), response.id());
        assertEquals(serviceCatalogId, response.serviceCatalogId());
        assertEquals(serviceOrderId, response.serviceOrderId());
        assertEquals(serviceOrderExecution.getStatus(), response.status());
        assertEquals(serviceOrderExecution.getCreatedAt(), response.createdAt());
        assertEquals(serviceOrderExecution.getUpdatedAt(), response.updatedAt());
        assertEquals(serviceOrderExecution.getFinishedAt(), response.finishedAt());
        assertEquals(serviceOrderExecution.getStartedAt(), response.startedAt());
    }

    @Test
    void shouldThrowExceptionWhenDomainIsNull() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ServiceOrderExecutionMapper.toDTO(null)
        );

        assertEquals(
                "ServiceOrderExecution não pode ser nulo",
                exception.getMessage()
        );
    }
}