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

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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
        void shouldReturnPageOfFinishedServiceOrders() {
            Pageable pageable = PageRequest.of(0, 10);
            ServiceOrder serviceOrder = finishedServiceOrder();
            Page<ServiceOrder> page = new PageImpl<>(List.of(serviceOrder), pageable, 1);
            when(serviceOrderRepository.findByStatus(ServiceOrderStatus.FINISHED, pageable)).thenReturn(page);

            Page<ServiceOrder> result = listFinishedServiceOrdersService.execute(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getStatus()).isEqualTo(ServiceOrderStatus.FINISHED);
        }

        @Test
        void shouldReturnEmptyPageWhenNoFinishedServiceOrders() {
            Pageable pageable = PageRequest.of(0, 10);
            when(serviceOrderRepository.findByStatus(ServiceOrderStatus.FINISHED, pageable))
                    .thenReturn(Page.empty(pageable));

            Page<ServiceOrder> result = listFinishedServiceOrdersService.execute(pageable);

            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        void shouldQueryRepositoryWithFinishedStatusAndProvidedPageable() {
            Pageable pageable = PageRequest.of(2, 5);
            when(serviceOrderRepository.findByStatus(any(), any())).thenReturn(Page.empty(pageable));

            listFinishedServiceOrdersService.execute(pageable);

            ArgumentCaptor<ServiceOrderStatus> statusCaptor = ArgumentCaptor.forClass(ServiceOrderStatus.class);
            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(serviceOrderRepository).findByStatus(statusCaptor.capture(), pageableCaptor.capture());
            assertThat(statusCaptor.getValue()).isEqualTo(ServiceOrderStatus.FINISHED);
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
}