package br.com.ofisy.application.serviceorder.finish;

import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.application.serviceorder.finish.FinishServiceOrderUseCase.FinishServiceOrderCommand;
import br.com.ofisy.config.metrics.ServiceOrderMetrics;
import br.com.ofisy.domain.messaging.OutboxEvent;
import br.com.ofisy.domain.messaging.OutboxEventRepository;
import br.com.ofisy.domain.messaging.ProcessedEvent;
import br.com.ofisy.domain.messaging.ProcessedEventRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinishServiceOrderServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final UUID VALID_SERVICE_ORDER_ID = UUID.randomUUID();
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final String EVENT_TYPE = "AllExecutionsCompleted";
    private static final FinishServiceOrderCommand COMMAND =
            new FinishServiceOrderCommand(EVENT_ID, EVENT_TYPE, VALID_SERVICE_ORDER_ID);

    @Mock
    private ServiceOrderRepository serviceOrderRepository;
    @Mock
    private ProcessedEventRepository processedEventRepository;
    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private ServiceOrderMetrics serviceOrderMetrics;
    @Captor
    private ArgumentCaptor<ProcessedEvent> processedEventCaptor;
    @Captor
    private ArgumentCaptor<OutboxEvent> outboxEventCaptor;

    @InjectMocks
    private FinishServiceOrderService finishServiceOrderService;

    @Nested
    class Execute {

        @Test
        void shouldFinishServiceOrderWhenOrderIsInProgress() {
            ServiceOrder serviceOrder = inProgressServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            finishServiceOrderService.execute(COMMAND);

            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.FINISHED);
            assertThat(serviceOrder.getFinishedAt()).isNotNull();
            verify(serviceOrderRepository).save(serviceOrder);
            verify(serviceOrderMetrics).recordTransition(
                    eq(ServiceOrderStatus.IN_PROGRESS), eq(ServiceOrderStatus.FINISHED), any());
        }

        @Test
        void shouldPublishServiceOrderFinishedWhenOrderIsFinished() {
            ServiceOrder serviceOrder = inProgressServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            finishServiceOrderService.execute(COMMAND);

            verify(outboxEventRepository).save(outboxEventCaptor.capture());
            OutboxEvent published = outboxEventCaptor.getValue();
            assertThat(published.getEventType()).isEqualTo("ServiceOrderFinished");
            assertThat(published.getServiceOrderId()).isEqualTo(VALID_SERVICE_ORDER_ID);
        }

        @Test
        void shouldStartAndFinishWhenOrderIsStillAwaitingExecution() {
            ServiceOrder serviceOrder = awaitingExecutionServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            finishServiceOrderService.execute(COMMAND);

            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.FINISHED);
            verify(serviceOrderMetrics).recordTransition(
                    eq(ServiceOrderStatus.AWAITING_EXECUTION), eq(ServiceOrderStatus.IN_PROGRESS), any());
            verify(serviceOrderMetrics).recordTransition(
                    eq(ServiceOrderStatus.IN_PROGRESS), eq(ServiceOrderStatus.FINISHED), any());
            verify(processedEventRepository).save(any(ProcessedEvent.class));
        }

        @Test
        void shouldThrowSoEventIsRetriedWhenOrderIsNotYetAwaitingExecution() {
            ServiceOrder serviceOrder = awaitingApprovalServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            assertThatThrownBy(() -> finishServiceOrderService.execute(COMMAND))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);

            verify(serviceOrderRepository, never()).save(any());
            verify(processedEventRepository, never()).save(any());
        }

        @Test
        void shouldRegisterProcessedEvent() {
            ServiceOrder serviceOrder = inProgressServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            finishServiceOrderService.execute(COMMAND);

            verify(processedEventRepository).save(processedEventCaptor.capture());
            assertThat(processedEventCaptor.getValue().getEventId()).isEqualTo(EVENT_ID);
            assertThat(processedEventCaptor.getValue().getEventType()).isEqualTo(EVENT_TYPE);
        }

        @Test
        void shouldIgnoreAlreadyProcessedEvent() {
            when(processedEventRepository.existsById(EVENT_ID)).thenReturn(true);

            finishServiceOrderService.execute(COMMAND);

            verify(serviceOrderRepository, never()).findById(any());
            verify(serviceOrderRepository, never()).save(any());
            verify(processedEventRepository, never()).save(any());
            verify(outboxEventRepository, never()).save(any());
            verify(serviceOrderMetrics, never()).recordTransition(any(), any(), any());
        }

        @Test
        void shouldOnlyRegisterEventWhenOrderIsCancelled() {
            ServiceOrder serviceOrder = cancelledServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            finishServiceOrderService.execute(COMMAND);

            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
            verify(serviceOrderRepository, never()).save(any());
            verify(outboxEventRepository, never()).save(any());
            verify(processedEventRepository).save(any(ProcessedEvent.class));
            verify(serviceOrderMetrics, never()).recordTransition(any(), any(), any());
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> finishServiceOrderService.execute(COMMAND))
                    .isInstanceOf(ServiceOrderNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
            verify(processedEventRepository, never()).save(any());
        }
    }

    private ServiceOrder awaitingApprovalServiceOrder() {
        ServiceOrder order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
        order.startDiagnostic();
        order.sendToApproval();
        return order;
    }

    private ServiceOrder awaitingExecutionServiceOrder() {
        ServiceOrder order = awaitingApprovalServiceOrder();
        order.approve();
        return order;
    }

    private ServiceOrder inProgressServiceOrder() {
        ServiceOrder order = awaitingExecutionServiceOrder();
        order.startExecution();
        return order;
    }

    private ServiceOrder cancelledServiceOrder() {
        ServiceOrder order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
        order.cancel();
        return order;
    }
}