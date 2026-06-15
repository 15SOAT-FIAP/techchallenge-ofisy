package br.com.ofisy.application.serviceorder.startdiagnostic;

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
class StartDiagnosticServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final UUID VALID_SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private StartDiagnosticService startDiagnosticService;

    @Nested
    class Execute {

        @Test
        void shouldStartDiagnosticSuccessfully() {
            ServiceOrder serviceOrder = receivedServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));
            when(serviceOrderRepository.save(serviceOrder)).thenAnswer(inv -> inv.getArgument(0));

            ServiceOrder result = startDiagnosticService.execute(VALID_SERVICE_ORDER_ID);

            assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.IN_DIAGNOSTIC);
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> startDiagnosticService.execute(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }

        @Test
        void shouldThrowInvalidTransitionWhenOrderIsNotReceived() {
            ServiceOrder serviceOrder = cancelledServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            assertThatThrownBy(() -> startDiagnosticService.execute(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);
        }
    }

    private ServiceOrder receivedServiceOrder() {
        return ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
    }

    private ServiceOrder cancelledServiceOrder() {
        ServiceOrder order = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
        order.cancel();
        return order;
    }
}