package br.com.ofisy.application.serviceorder.cancel;

import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.config.metrics.ServiceOrderMetrics;
import br.com.ofisy.domain.messaging.OutboxEvent;
import br.com.ofisy.domain.messaging.OutboxEventRepository;
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

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CancelServiceOrderServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final UUID VALID_SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderRepository serviceOrderRepository;
    @Mock
    private OutboxEventRepository outboxEventRepository;
    @Mock
    private ServiceOrderMetrics serviceOrderMetrics;
    @Captor
    private ArgumentCaptor<OutboxEvent> outboxEventCaptor;

    @InjectMocks
    private CancelServiceOrderService cancelServiceOrderService;

    @Nested
    class Execute {

        @Test
        void shouldCancelServiceOrderSuccessfully() {
            ServiceOrder serviceOrder = serviceOrderAwaitingApproval();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            ServiceOrder result = cancelServiceOrderService.execute(VALID_SERVICE_ORDER_ID);

            assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> cancelServiceOrderService.execute(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }

        @Test
        void shouldThrowInvalidTransitionWhenOrderIsAlreadyCancelled() {
            ServiceOrder serviceOrder = cancelledServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            assertThatThrownBy(() -> cancelServiceOrderService.execute(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);
        }

        @Test
        void shouldSaveServiceOrderCancelledEventInOutbox() {
            ServiceOrder serviceOrder = serviceOrderAwaitingApproval();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            cancelServiceOrderService.execute(VALID_SERVICE_ORDER_ID);

            verify(outboxEventRepository).save(outboxEventCaptor.capture());
            OutboxEvent event = outboxEventCaptor.getValue();
            assertThat(event.getEventType()).isEqualTo(CancelServiceOrderService.SERVICE_ORDER_CANCELLED);
            assertThat(event.getServiceOrderId()).isEqualTo(VALID_SERVICE_ORDER_ID);
            assertThat(event.getPayload()).isEqualTo(Map.of());
        }

        @Test
        void shouldNotSaveEventWhenTransitionIsInvalid() {
            ServiceOrder serviceOrder = cancelledServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            assertThatThrownBy(() -> cancelServiceOrderService.execute(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);

            verify(serviceOrderRepository, never()).save(any());
            verify(outboxEventRepository, never()).save(any());
        }
    }

    private ServiceOrder serviceOrderAwaitingApproval() {
        ServiceOrder order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
        order.startDiagnostic();
        order.sendToApproval();
        return order;
    }

    private ServiceOrder cancelledServiceOrder() {
        ServiceOrder order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
        order.cancel();
        return order;
    }
}