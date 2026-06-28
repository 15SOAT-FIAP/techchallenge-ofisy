package br.com.ofisy.application.serviceorderexecution.identifybyid;

import br.com.ofisy.application.serviceorderexecution.exceptions.ServiceOrderExecutionNotFoundException;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdentifyByIdServiceOrderExecutionServiceTest {

    private static final UUID VALID_ID = UUID.randomUUID();
    private static final UUID SERVICE_CATALOG_ID = UUID.randomUUID();
    private static final UUID SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderExecutionRepository repository;

    @InjectMocks
    private IdentifyByIdServiceOrderExecutionService identifyByIdService;

    @Nested
    class Execute {

        @Test
        void shouldReturnServiceOrderExecutionSuccessfully() {
            var execution = createServiceOrderExecution(VALID_ID, SERVICE_CATALOG_ID, SERVICE_ORDER_ID);
            when(repository.findById(VALID_ID)).thenReturn(Optional.of(execution));

            ServiceOrderExecution result = identifyByIdService.execute(VALID_ID);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(VALID_ID);
            assertThat(result.getServiceCatalogId()).isEqualTo(SERVICE_CATALOG_ID);
            assertThat(result.getServiceOrderId()).isEqualTo(SERVICE_ORDER_ID);
            verify(repository).findById(VALID_ID);
        }

        @Test
        void shouldThrowServiceOrderExecutionNotFoundExceptionWhenIdNotExists() {
            UUID nonExistentId = UUID.randomUUID();
            when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> identifyByIdService.execute(nonExistentId))
                    .isInstanceOf(ServiceOrderExecutionNotFoundException.class);

            verify(repository).findById(nonExistentId);
        }

        @Test
        void shouldReturnDifferentExecutions() {
            UUID id1 = UUID.randomUUID();
            UUID id2 = UUID.randomUUID();

            var execution1 = createServiceOrderExecution(id1, UUID.randomUUID(), UUID.randomUUID());
            var execution2 = createServiceOrderExecution(id2, UUID.randomUUID(), UUID.randomUUID());

            when(repository.findById(id1)).thenReturn(Optional.of(execution1));
            when(repository.findById(id2)).thenReturn(Optional.of(execution2));

            ServiceOrderExecution result1 = identifyByIdService.execute(id1);
            ServiceOrderExecution result2 = identifyByIdService.execute(id2);

            assertThat(result1.getId()).isEqualTo(id1);
            assertThat(result2.getId()).isEqualTo(id2);
            assertThat(result1.getId()).isNotEqualTo(result2.getId());
        }
    }

    private ServiceOrderExecution createServiceOrderExecution(UUID id, UUID serviceCatalogId, UUID serviceOrderId) {
        return ServiceOrderExecution.reconstruct(
                id,
                serviceCatalogId,
                serviceOrderId,
                ServiceOrderExecutionStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );
    }
}

