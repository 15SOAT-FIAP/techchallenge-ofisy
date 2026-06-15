package br.com.ofisy.application.serviceorder.startexecution;

import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StartServiceOrderExecutionServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final UUID VALID_SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private StartServiceOrderExecutionService startServiceOrderExecutionService;

    @Nested
    class Execute {

        @Test
        void shouldTransitionToInProgressWhenOrderIsAwaitingExecution() {
            ServiceOrder serviceOrder = awaitingExecutionServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            startServiceOrderExecutionService.execute(VALID_SERVICE_ORDER_ID);

            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.IN_PROGRESS);
            verify(serviceOrderRepository).save(serviceOrder);
        }

        @Test
        void shouldNotSaveWhenOrderIsAlreadyInProgress() {
            ServiceOrder serviceOrder = inProgressServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            startServiceOrderExecutionService.execute(VALID_SERVICE_ORDER_ID);

            assertThat(serviceOrder.getStatus()).isEqualTo(ServiceOrderStatus.IN_PROGRESS);
            verify(serviceOrderRepository, never()).save(any());
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> startServiceOrderExecutionService.execute(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(ServiceOrderNotFoundException.class);

            verify(serviceOrderRepository, never()).save(any());
        }
    }

    private ServiceOrder awaitingExecutionServiceOrder() {
        ServiceOrder order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
        order.startDiagnostic();
        order.sendToApproval();
        order.approve();
        return order;
    }

    private ServiceOrder inProgressServiceOrder() {
        ServiceOrder order = awaitingExecutionServiceOrder();
        order.startExecution();
        return order;
    }
}