package br.com.ofisy.application.serviceorder.delivertocustomer;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeliverToCustomerServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final UUID VALID_SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private DeliverToCustomerService deliverToCustomerService;

    @Nested
    class Execute {

        @Test
        void shouldDeliverServiceOrderSuccessfully() {
            ServiceOrder serviceOrder = finishedServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            ServiceOrder result = deliverToCustomerService.execute(VALID_SERVICE_ORDER_ID);

            assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.DELIVERED);
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> deliverToCustomerService.execute(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }

        @Test
        void shouldThrowInvalidTransitionWhenOrderIsNotFinished() {
            ServiceOrder serviceOrder = cancelledServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            assertThatThrownBy(() -> deliverToCustomerService.execute(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);
        }
    }

    private ServiceOrder finishedServiceOrder() {
        ServiceOrder order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
        order.startDiagnostic();
        order.sendToApproval();
        order.approve();
        order.startExecution();
        order.finish();
        return order;
    }

    private ServiceOrder cancelledServiceOrder() {
        ServiceOrder order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
        order.cancel();
        return order;
    }
}