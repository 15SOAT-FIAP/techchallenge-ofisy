package br.com.ofisy.application.serviceorder.listfinished;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListFinishedServiceOrdersServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private ListFinishedServiceOrdersService listFinishedServiceOrdersService;

    @Nested
    class Execute {

        @Test
        void shouldReturnPageOfFinishedAndPaidServiceOrders() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ServiceOrder> page = new PageImpl<>(List.of(finishedServiceOrder(), paidServiceOrder()), pageable, 2);
            when(serviceOrderRepository.findByStatusIn(anyCollection(), eq(pageable))).thenReturn(page);

            Page<ServiceOrder> result = listFinishedServiceOrdersService.execute(pageable);

            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent()).extracting(ServiceOrder::getStatus)
                    .containsExactly(ServiceOrderStatus.FINISHED, ServiceOrderStatus.PAID);
        }

        @Test
        void shouldReturnEmptyPageWhenNoFinishedServiceOrders() {
            Pageable pageable = PageRequest.of(0, 10);
            when(serviceOrderRepository.findByStatusIn(anyCollection(), eq(pageable)))
                    .thenReturn(Page.empty(pageable));

            Page<ServiceOrder> result = listFinishedServiceOrdersService.execute(pageable);

            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        @SuppressWarnings("unchecked")
        void shouldQueryRepositoryWithFinishedAndPaidStatusesAndProvidedPageable() {
            Pageable pageable = PageRequest.of(2, 5);
            when(serviceOrderRepository.findByStatusIn(anyCollection(), any())).thenReturn(Page.empty(pageable));

            listFinishedServiceOrdersService.execute(pageable);

            ArgumentCaptor<Collection<ServiceOrderStatus>> statusesCaptor = ArgumentCaptor.forClass(Collection.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(serviceOrderRepository).findByStatusIn(statusesCaptor.capture(), pageableCaptor.capture());
            assertThat(statusesCaptor.getValue())
                    .containsExactlyInAnyOrder(ServiceOrderStatus.FINISHED, ServiceOrderStatus.PAID);
            assertThat(pageableCaptor.getValue()).isEqualTo(pageable);
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

    private ServiceOrder paidServiceOrder() {
        ServiceOrder order = finishedServiceOrder();
        order.confirmPayment();
        return order;
    }
}