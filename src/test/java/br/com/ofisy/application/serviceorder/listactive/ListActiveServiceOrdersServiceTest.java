package br.com.ofisy.application.serviceorder.listactive;

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
class ListActiveServiceOrdersServiceTest {

    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_USER_ID = UUID.randomUUID();

    @Mock
    private ServiceOrderRepository serviceOrderRepository;

    @InjectMocks
    private ListActiveServiceOrdersService listActiveServiceOrdersService;

    @Nested
    class Execute {

        @Test
        void shouldReturnPageOfActiveServiceOrders() {
            Pageable pageable = PageRequest.of(0, 10);
            ServiceOrder serviceOrder = ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, "Relatório", VALID_USER_ID);
            Page<ServiceOrder> page = new PageImpl<>(List.of(serviceOrder), pageable, 1);
            when(serviceOrderRepository.findActive(pageable)).thenReturn(page);

            Page<ServiceOrder> result = listActiveServiceOrdersService.execute(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().getStatus()).isNotIn(
                    ServiceOrderStatus.FINISHED, ServiceOrderStatus.DELIVERED, ServiceOrderStatus.CANCELLED);
        }

        @Test
        void shouldReturnEmptyPageWhenNoActiveServiceOrders() {
            Pageable pageable = PageRequest.of(0, 10);
            when(serviceOrderRepository.findActive(pageable)).thenReturn(Page.empty(pageable));

            Page<ServiceOrder> result = listActiveServiceOrdersService.execute(pageable);

            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getContent()).isEmpty();
        }

        @Test
        void shouldCallRepositoryFindActiveWithProvidedPageable() {
            Pageable pageable = PageRequest.of(2, 5);
            when(serviceOrderRepository.findActive(any())).thenReturn(Page.empty(pageable));

            listActiveServiceOrdersService.execute(pageable);

            ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
            verify(serviceOrderRepository).findActive(pageableCaptor.capture());
            assertThat(pageableCaptor.getValue().getPageNumber()).isEqualTo(2);
            assertThat(pageableCaptor.getValue().getPageSize()).isEqualTo(5);
        }
    }
}
