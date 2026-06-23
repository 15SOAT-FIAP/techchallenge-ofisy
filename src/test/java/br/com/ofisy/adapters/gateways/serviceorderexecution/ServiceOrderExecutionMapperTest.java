package br.com.ofisy.adapters.gateways.serviceorderexecution;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ServiceOrderExecutionMapperTest {
        @Test
        void shouldConvertRequestEntityToDomain() {
            UUID serviceCatalogId = UUID.randomUUID();
            UUID serviceOrderId = UUID.randomUUID();

            ServiceOrderExecutionEntity entity = ServiceOrderExecutionEntity.builder()
                    .serviceCatalogId(serviceCatalogId)
                    .serviceOrderId(serviceOrderId)
                    .build();

            ServiceOrderExecution serviceOrderExecution = ServiceOrderExecutionMapper.toDomain(entity);

            assertNotNull(serviceOrderExecution);
            assertEquals(serviceCatalogId, serviceOrderExecution.getServiceCatalogId());
            assertEquals(serviceOrderId, serviceOrderExecution.getServiceOrderId());
        }

        @Test
        void shouldConvertDomainToEntity() {
            UUID serviceCatalogId = UUID.randomUUID();
            UUID serviceOrderId = UUID.randomUUID();

            ServiceOrderExecution serviceOrderExecution = ServiceOrderExecution.create(
                    serviceCatalogId,
                    serviceOrderId
            );

            ServiceOrderExecutionEntity response =
                    ServiceOrderExecutionMapper.toEntity(serviceOrderExecution);

            assertNotNull(response);
            assertEquals(serviceOrderExecution.getId(), response.getId());
            assertEquals(serviceCatalogId, response.getServiceCatalogId());
            assertEquals(serviceOrderId, response.getServiceOrderId());
            assertEquals(serviceOrderExecution.getStatus(), response.getStatus());
            assertEquals(serviceOrderExecution.getCreatedAt(), response.getCreatedAt());
            assertEquals(serviceOrderExecution.getUpdatedAt(), response.getUpdatedAt());
            assertEquals(serviceOrderExecution.getFinishedAt(), response.getFinishedAt());
            assertEquals(serviceOrderExecution.getStartedAt(), response.getStartedAt());
        }
}
