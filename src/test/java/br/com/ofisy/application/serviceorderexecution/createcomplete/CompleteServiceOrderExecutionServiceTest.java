package br.com.ofisy.application.serviceorderexecution.createcomplete;

import br.com.ofisy.application.serviceorder.finish.FinishServiceOrderUseCase;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompleteServiceOrderExecutionServiceTest {

    private static final UUID VALID_ID = UUID.randomUUID();
    private static final UUID SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderExecutionRepository repository;

    @Mock
    private FinishServiceOrderUseCase finishServiceOrderUseCase;

    @InjectMocks
    private CompleteServiceOrderExecutionService completeService;

    @Nested
    class Execute {

        @Test
        void shouldCompleteServiceOrderExecutionSuccessfully() {
            var execution = createServiceOrderExecution(ServiceOrderExecutionStatus.IN_PROGRESS);
            when(repository.findById(VALID_ID)).thenReturn(Optional.of(execution));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(repository.countByServiceOrderId(SERVICE_ORDER_ID)).thenReturn(1L);
            when(repository.countByServiceOrderIdAndStatus(SERVICE_ORDER_ID, ServiceOrderExecutionStatus.COMPLETED)).thenReturn(1L);

            ServiceOrderExecution result = completeService.execute(VALID_ID);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(ServiceOrderExecutionStatus.COMPLETED);
            assertThat(result.getFinishedAt()).isNotNull();
            verify(repository).findById(VALID_ID);
            verify(repository).save(any());
            verify(finishServiceOrderUseCase).execute(SERVICE_ORDER_ID);
        }

        @Test
        void shouldNotFinishServiceOrderWhenNotAllExecutionsAreComplete() {
            var execution = createServiceOrderExecution(ServiceOrderExecutionStatus.IN_PROGRESS);
            when(repository.findById(VALID_ID)).thenReturn(Optional.of(execution));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            when(repository.countByServiceOrderId(SERVICE_ORDER_ID)).thenReturn(2L);
            when(repository.countByServiceOrderIdAndStatus(SERVICE_ORDER_ID, ServiceOrderExecutionStatus.COMPLETED)).thenReturn(1L);

            ServiceOrderExecution result = completeService.execute(VALID_ID);

            assertThat(result.getStatus()).isEqualTo(ServiceOrderExecutionStatus.COMPLETED);
            verify(finishServiceOrderUseCase, never()).execute(SERVICE_ORDER_ID);
        }

        @Test
        void shouldThrowServiceOrderExecutionNotFoundExceptionWhenNotFound() {
            when(repository.findById(VALID_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> completeService.execute(VALID_ID))
                    .isInstanceOf(ServiceOrderExecutionNotFoundException.class);

            verify(repository).findById(VALID_ID);
        }
    }

    private ServiceOrderExecution createServiceOrderExecution(ServiceOrderExecutionStatus status) {
        return ServiceOrderExecution.reconstruct(
                VALID_ID,
                UUID.randomUUID(),
                SERVICE_ORDER_ID,
                status,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
    }
}

