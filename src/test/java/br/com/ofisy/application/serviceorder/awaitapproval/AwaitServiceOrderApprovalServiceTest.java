package br.com.ofisy.application.serviceorder.awaitapproval;

import br.com.ofisy.application.serviceorder.awaitapproval.AwaitServiceOrderApprovalUseCase.AwaitServiceOrderApprovalCommand;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.config.metrics.ServiceOrderMetrics;
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
class AwaitServiceOrderApprovalServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final UUID VALID_SERVICE_ORDER_ID = UUID.randomUUID();
    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final String EVENT_TYPE = "QuoteSentForApproval";
    private static final AwaitServiceOrderApprovalCommand COMMAND =
            new AwaitServiceOrderApprovalCommand(EVENT_ID, EVENT_TYPE, VALID_SERVICE_ORDER_ID);

    @Mock
    private ServiceOrderRepository serviceOrderRepository;
    @Mock
    private ProcessedEventRepository processedEventRepository;
    @Mock
    private ServiceOrderMetrics serviceOrderMetrics;
    @Captor
    private ArgumentCaptor<ProcessedEvent> processedEventCaptor;

    @InjectMocks
    private AwaitServiceOrderApprovalService awaitServiceOrderApprovalService;

    @Nested
    class Execute {

        @Test
        void shouldTransitionToAwaitingApprovalWhenOrderIsInDiagnostic() {
            ServiceOrder serviceOrder = inDiagnosticServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            awaitServiceOrderApprovalService.execute(COMMAND);

            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
            verify(serviceOrderRepository).save(serviceOrder);
            verify(serviceOrderMetrics).recordTransition(
                    eq(ServiceOrderStatus.IN_DIAGNOSTIC), eq(ServiceOrderStatus.AWAITING_APPROVAL), any());
        }

        @Test
        void shouldRegisterProcessedEvent() {
            ServiceOrder serviceOrder = inDiagnosticServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            awaitServiceOrderApprovalService.execute(COMMAND);

            verify(processedEventRepository).save(processedEventCaptor.capture());
            assertThat(processedEventCaptor.getValue().getEventId()).isEqualTo(EVENT_ID);
            assertThat(processedEventCaptor.getValue().getEventType()).isEqualTo(EVENT_TYPE);
        }

        @Test
        void shouldIgnoreAlreadyProcessedEvent() {
            when(processedEventRepository.existsById(EVENT_ID)).thenReturn(true);

            awaitServiceOrderApprovalService.execute(COMMAND);

            verify(serviceOrderRepository, never()).findById(any());
            verify(serviceOrderRepository, never()).save(any());
            verify(processedEventRepository, never()).save(any());
            verify(serviceOrderMetrics, never()).recordTransition(any(), any(), any());
        }

        @Test
        void shouldOnlyRegisterEventWhenOrderIsAlreadyAwaitingApproval() {
            ServiceOrder serviceOrder = awaitingApprovalServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            awaitServiceOrderApprovalService.execute(COMMAND);

            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
            verify(serviceOrderRepository, never()).save(any());
            verify(processedEventRepository).save(any(ProcessedEvent.class));
            verify(serviceOrderMetrics, never()).recordTransition(any(), any(), any());
        }

        @Test
        void shouldOnlyRegisterEventWhenOrderIsAlreadyAheadOfApproval() {
            ServiceOrder serviceOrder = inProgressServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            awaitServiceOrderApprovalService.execute(COMMAND);

            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.IN_PROGRESS);
            verify(serviceOrderRepository, never()).save(any());
            verify(processedEventRepository).save(any(ProcessedEvent.class));
        }

        @Test
        void shouldOnlyRegisterEventWhenOrderIsCancelled() {
            ServiceOrder serviceOrder = cancelledServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            awaitServiceOrderApprovalService.execute(COMMAND);

            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
            verify(serviceOrderRepository, never()).save(any());
            verify(processedEventRepository).save(any(ProcessedEvent.class));
        }

        @Test
        void shouldThrowSoEventIsRetriedWhenDiagnosticHasNotStarted() {
            ServiceOrder serviceOrder = receivedServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            assertThatThrownBy(() -> awaitServiceOrderApprovalService.execute(COMMAND))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);

            verify(serviceOrderRepository, never()).save(any());
            verify(processedEventRepository, never()).save(any());
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> awaitServiceOrderApprovalService.execute(COMMAND))
                    .isInstanceOf(ServiceOrderNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
            verify(processedEventRepository, never()).save(any());
        }
    }

    private ServiceOrder receivedServiceOrder() {
        return ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
    }

    private ServiceOrder inDiagnosticServiceOrder() {
        ServiceOrder order = receivedServiceOrder();
        order.startDiagnostic();
        return order;
    }

    private ServiceOrder awaitingApprovalServiceOrder() {
        ServiceOrder order = inDiagnosticServiceOrder();
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

    private ServiceOrder finishedServiceOrder() {
        ServiceOrder order = inProgressServiceOrder();
        order.finish();
        return order;
    }

    private ServiceOrder cancelledServiceOrder() {
        ServiceOrder order = receivedServiceOrder();
        order.cancel();
        return order;
    }
}
