package br.com.ofisy.application.serviceorder;

import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderFinalizationServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final String VALID_REPORT = "Barulho na suspensão dianteira";

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @Mock
    private ServiceOrderExecutionRepository serviceOrderExecutionRepository;

    @InjectMocks
    private ServiceOrderFinalizationService finalizationService;

    @Nested
    class CancelPending {

        @Test
        void shouldCancelPendingAndInProgressExecutions() {
            var serviceOrderId = UUID.randomUUID();
            var pending = ServiceOrderExecution.create(UUID.randomUUID(), serviceOrderId);
            var inProgress = ServiceOrderExecution.create(UUID.randomUUID(), serviceOrderId);
            inProgress.start();

            when(serviceOrderExecutionRepository.findByServiceOrderIdAndStatusIn(any(), any()))
                    .thenReturn(List.of(pending, inProgress));
            when(serviceOrderExecutionRepository.save(any(ServiceOrderExecution.class)))
                    .thenAnswer(inv -> inv.getArgument(0));

            finalizationService.cancelPending(serviceOrderId);

            verify(serviceOrderExecutionRepository, times(2)).save(any(ServiceOrderExecution.class));
            assertThat(pending.getStatus()).isEqualTo(ServiceOrderExecutionStatus.CANCELLED);
            assertThat(inProgress.getStatus()).isEqualTo(ServiceOrderExecutionStatus.CANCELLED);
        }

        @Test
        void shouldDoNothingWhenNoActiveExecutionsExist() {
            var serviceOrderId = UUID.randomUUID();

            when(serviceOrderExecutionRepository.findByServiceOrderIdAndStatusIn(any(), any()))
                    .thenReturn(Collections.emptyList());

            finalizationService.cancelPending(serviceOrderId);

            verify(serviceOrderExecutionRepository, never()).save(any(ServiceOrderExecution.class));
        }
    }

    @Nested
    class Finish {

        @Test
        void shouldFinishServiceOrderSuccessfully() {
            var serviceOrder = executingServiceOrder();
            var serviceOrderId = serviceOrder.getId();

            when(serviceOrderRepository.findById(serviceOrderId)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            finalizationService.finish(serviceOrderId);

            verify(serviceOrderRepository).save(serviceOrder);
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            var serviceOrderId = UUID.randomUUID();
            when(serviceOrderRepository.findById(serviceOrderId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> finalizationService.finish(serviceOrderId))
                    .isInstanceOf(ServiceOrderNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }
    }

    private ServiceOrder executingServiceOrder() {
        var order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_USER_ID);
        order.startDiagnostic();
        order.sendToApproval();
        order.approve();
        order.startExecution();
        return order;
    }
}