package br.com.ofisy.application.serviceorder.getstatus;

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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetServiceOrderStatusServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();
    private static final UUID VALID_SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private GetServiceOrderStatusService getServiceOrderStatusService;

    @Nested
    class Execute {

        @Test
        void shouldReturnServiceOrderSuccessfully() {
            ServiceOrder serviceOrder = receivedServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            ServiceOrder result = getServiceOrderStatusService.execute(VALID_SERVICE_ORDER_ID);

            assertThat(result).isNotNull();
            assertThat(result.getVehicleId()).isEqualTo(VALID_VEHICLE_ID);
            assertThat(result.getCustomerId()).isEqualTo(VALID_CUSTOMER_ID);
            assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        }

        @Test
        void shouldReturnCurrentStatusForFinishedOrder() {
            ServiceOrder serviceOrder = finishedServiceOrder();
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.of(serviceOrder));

            ServiceOrder result = getServiceOrderStatusService.execute(VALID_SERVICE_ORDER_ID);

            assertThat(result.getStatus()).isEqualTo(ServiceOrderStatus.FINISHED);
        }

        @Test
        void shouldThrowServiceOrderNotFoundExceptionWhenOrderDoesNotExist() {
            when(serviceOrderRepository.findById(VALID_SERVICE_ORDER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> getServiceOrderStatusService.execute(VALID_SERVICE_ORDER_ID))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }
    }

    private ServiceOrder receivedServiceOrder() {
        return ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
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
}