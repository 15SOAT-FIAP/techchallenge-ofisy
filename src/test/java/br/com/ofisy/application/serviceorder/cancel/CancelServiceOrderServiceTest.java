package br.com.ofisy.application.serviceorder.cancel;

import br.com.ofisy.application.serviceorder.cancelpending.CancelPendingExecutionsUseCase;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
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
import static org.mockito.Mockito.doThrow;
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
    private CancelPendingExecutionsUseCase cancelPendingExecutionsUseCase;

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
        void shouldCallCancelPendingBeforeCancellingOrder() {
            ServiceOrder serviceOrder = serviceOrderAwaitingApproval();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            cancelServiceOrderService.execute(VALID_SERVICE_ORDER_ID);

            verify(cancelPendingExecutionsUseCase).execute(VALID_SERVICE_ORDER_ID);
            verify(serviceOrderRepository).save(any());
        }

        @Test
        void shouldNotSaveOrderWhenCancelPendingThrows() {
            ServiceOrder serviceOrder = serviceOrderAwaitingApproval();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            doThrow(new RuntimeException("cancellation failed"))
                    .when(cancelPendingExecutionsUseCase).execute(VALID_SERVICE_ORDER_ID);

            assertThatThrownBy(() -> cancelServiceOrderService.execute(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(RuntimeException.class);

            verify(serviceOrderRepository, never()).save(any());
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