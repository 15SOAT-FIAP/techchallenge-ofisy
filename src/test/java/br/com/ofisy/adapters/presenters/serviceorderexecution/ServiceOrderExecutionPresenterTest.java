package br.com.ofisy.adapters.presenters.serviceorderexecution;

import br.com.ofisy.adapters.controllers.serviceorderexecution.dto.ServiceOrderExecutionResponseDTO;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import org.junit.jupiter.api.Test;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderExecutionPresenterTest {

    @Test
    void shouldPresentServiceOrderExecutionToResponseDTO() {
        UUID id = UUID.randomUUID();
        UUID serviceCatalogId = UUID.randomUUID();
        UUID serviceOrderId = UUID.randomUUID();

        LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);
        LocalDateTime startedAt = LocalDateTime.now().minusHours(5);
        LocalDateTime finishedAt = LocalDateTime.now();

        ServiceOrderExecution serviceOrderExecution =  ServiceOrderExecution.reconstruct(
                id,
                serviceCatalogId,
                serviceOrderId,
                ServiceOrderExecutionStatus.COMPLETED,
                createdAt,
                updatedAt,
                startedAt,
                finishedAt
        );

        ServiceOrderExecutionResponseDTO response =
                ServiceOrderExecutionPresenter.present(serviceOrderExecution);

        assertNotNull(response);
        assertEquals(id, response.id());
        assertEquals(serviceCatalogId, response.serviceCatalogId());
        assertEquals(serviceOrderId, response.serviceOrderId());
        assertEquals(ServiceOrderExecutionStatus.COMPLETED, response.status());
        assertEquals(createdAt, response.createdAt());
        assertEquals(updatedAt, response.updatedAt());
        assertEquals(startedAt, response.startedAt());
        assertEquals(finishedAt, response.finishedAt());
    }

    @Test
    void shouldPresentWithNullDatesWhenServiceOrderExecutionHasNullDates() {
        ServiceOrderExecution serviceOrderExecution = ServiceOrderExecution.create(
                UUID.randomUUID(),
                UUID.randomUUID()
        );

        ServiceOrderExecutionResponseDTO response =
                ServiceOrderExecutionPresenter.present(serviceOrderExecution);

        assertNotNull(response);
        assertEquals(serviceOrderExecution.getId(), response.id());
        assertEquals(serviceOrderExecution.getServiceCatalogId(), response.serviceCatalogId());
        assertEquals(serviceOrderExecution.getServiceOrderId(), response.serviceOrderId());
        assertEquals(serviceOrderExecution.getStatus(), response.status());

        assertNull(response.createdAt());
        assertNull(response.updatedAt());
        assertNull(response.startedAt());
        assertNull(response.finishedAt());
    }
}