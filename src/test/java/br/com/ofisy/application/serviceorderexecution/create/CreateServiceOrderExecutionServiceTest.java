package br.com.ofisy.application.serviceorderexecution.create;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CreateServiceOrderExecutionServiceTest {

    private static final UUID VALID_SERVICE_CATALOG_ID = UUID.randomUUID();
    private static final UUID VALID_SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderExecutionRepository repository;

    @InjectMocks
    private CreateServiceOrderExecutionService createServiceOrderExecutionService;

    private CreateServiceOrderExecutionUseCase.CreateServiceOrderExecutionCommand validCommand() {
        return new CreateServiceOrderExecutionUseCase.CreateServiceOrderExecutionCommand(
                VALID_SERVICE_CATALOG_ID, VALID_SERVICE_ORDER_ID);
    }

    @Nested
    class Execute {

        @Test
        void shouldCreateServiceOrderExecutionSuccessfully() {
            var cmd = validCommand();
            when(repository.save(any())).thenAnswer(inv -> {
                ServiceOrderExecution execution = inv.getArgument(0);
                return ServiceOrderExecution.reconstruct(
                        UUID.randomUUID(),
                        execution.getServiceCatalogId(),
                        execution.getServiceOrderId(),
                        execution.getStatus(),
                        execution.getCreatedAt(),
                        execution.getUpdatedAt(),
                        execution.getStartedAt(),
                        execution.getFinishedAt()
                );
            });

            ServiceOrderExecution result = createServiceOrderExecutionService.execute(cmd);

            assertThat(result).isNotNull();
            assertThat(result.getServiceCatalogId()).isEqualTo(VALID_SERVICE_CATALOG_ID);
            assertThat(result.getServiceOrderId()).isEqualTo(VALID_SERVICE_ORDER_ID);
            assertThat(result.getStatus()).isEqualTo(ServiceOrderExecutionStatus.PENDING);
            verify(repository).save(any());
        }

        @Test
        void shouldCreateWithPendingStatusByDefault() {
            var cmd = validCommand();
            when(repository.save(any())).thenAnswer(inv -> {
                ServiceOrderExecution execution = inv.getArgument(0);
                return ServiceOrderExecution.reconstruct(
                        UUID.randomUUID(),
                        execution.getServiceCatalogId(),
                        execution.getServiceOrderId(),
                        execution.getStatus(),
                        execution.getCreatedAt(),
                        execution.getUpdatedAt(),
                        execution.getStartedAt(),
                        execution.getFinishedAt()
                );
            });

            ServiceOrderExecution result = createServiceOrderExecutionService.execute(cmd);

            assertThat(result.getStatus()).isEqualTo(ServiceOrderExecutionStatus.PENDING);
        }

        @Test
        void shouldReturnDTOWithAllRequiredFields() {
            var cmd = validCommand();
            when(repository.save(any())).thenAnswer(inv -> {
                ServiceOrderExecution execution = inv.getArgument(0);
                return ServiceOrderExecution.reconstruct(
                        UUID.randomUUID(),
                        execution.getServiceCatalogId(),
                        execution.getServiceOrderId(),
                        execution.getStatus(),
                        execution.getCreatedAt(),
                        execution.getUpdatedAt(),
                        execution.getStartedAt(),
                        execution.getFinishedAt()
                );
            });

            ServiceOrderExecution result = createServiceOrderExecutionService.execute(cmd);

            assertThat(result.getId()).isNotNull();
            assertThat(result.getServiceCatalogId()).isNotNull();
            assertThat(result.getServiceCatalogId()).isNotNull();
            assertThat(result.getStatus()).isNotNull();
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();
        }
    }
}

