package br.com.ofisy.application.serviceorderexecution.cancel;

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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelServiceOrderExecutionServiceTest {

    private static final UUID VALID_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderExecutionRepository repository;

    @InjectMocks
    private CancelServiceOrderExecutionService cancelService;

    @Nested
    class Execute {

        @Test
        void shouldCancelServiceOrderExecutionSuccessfully() {
            var execution = createServiceOrderExecution(ServiceOrderExecutionStatus.PENDING);
            when(repository.findById(VALID_ID)).thenReturn(Optional.of(execution));
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            ServiceOrderExecution result = cancelService.execute(VALID_ID);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(ServiceOrderExecutionStatus.CANCELLED);
            assertThat(result.getFinishedAt()).isNotNull();
            verify(repository).findById(VALID_ID);
            verify(repository).save(any());
        }

        @Test
        void shouldThrowServiceOrderExecutionNotFoundExceptionWhenNotFound() {
            when(repository.findById(VALID_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cancelService.execute(VALID_ID))
                    .isInstanceOf(ServiceOrderExecutionNotFoundException.class);

            verify(repository).findById(VALID_ID);
        }

        @Test
        void shouldThrowIllegalArgumentExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> cancelService.execute(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("ID não pode ser nulo");
        }
    }

    private ServiceOrderExecution createServiceOrderExecution(ServiceOrderExecutionStatus status) {
        return ServiceOrderExecution.reconstruct(
                VALID_ID,
                UUID.randomUUID(),
                UUID.randomUUID(),
                status,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );
    }
}

