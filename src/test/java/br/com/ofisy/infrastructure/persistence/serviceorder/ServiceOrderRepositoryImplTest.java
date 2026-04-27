package br.com.ofisy.infrastructure.persistence.serviceorder;

import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderRepositoryImplTest {

    private static final UUID VALID_VEHICLE_ID = UUID.randomUUID();
    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final UUID VALID_CREATED_BY = UUID.randomUUID();
    private static final String VALID_REPORT = "Barulho na suspensão";

    @Mock
    private JpaServiceOrderRepository jpa;

    @InjectMocks
    private ServiceOrderRepositoryImpl repository;

    private ServiceOrder validServiceOrder() {
        return ServiceOrder.receive(VALID_VEHICLE_ID, VALID_CUSTOMER_ID, VALID_REPORT, VALID_CREATED_BY);
    }

    @Nested
    class Save {

        @Test
        void shouldDelegateToJpaAndReturnSavedServiceOrder() {
            var serviceOrder = validServiceOrder();
            when(jpa.save(serviceOrder)).thenReturn(serviceOrder);

            var result = repository.save(serviceOrder);

            assertThat(result).isSameAs(serviceOrder);
            verify(jpa).save(serviceOrder);
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnServiceOrderWhenFound() {
            var id = UUID.randomUUID();
            var serviceOrder = validServiceOrder();
            when(jpa.findById(id)).thenReturn(Optional.of(serviceOrder));

            var result = repository.findById(id);

            assertThat(result).isPresent().contains(serviceOrder);
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            var id = UUID.randomUUID();
            when(jpa.findById(id)).thenReturn(Optional.empty());

            var result = repository.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByStatus {

        @Test
        void shouldDelegateToJpaWithStatusAndPageable() {
            var pageable = PageRequest.of(0, 10);
            var page = new PageImpl<>(List.of(validServiceOrder()), pageable, 1);
            when(jpa.findByStatus(ServiceOrderStatus.RECEIVED, pageable)).thenReturn(page);

            var result = repository.findByStatus(ServiceOrderStatus.RECEIVED, pageable);

            assertThat(result).isEqualTo(page);
            verify(jpa).findByStatus(ServiceOrderStatus.RECEIVED, pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoServiceOrdersMatchStatus() {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<ServiceOrder>(List.of(), pageable, 0);
            when(jpa.findByStatus(ServiceOrderStatus.RECEIVED, pageable)).thenReturn(emptyPage);

            var result = repository.findByStatus(ServiceOrderStatus.RECEIVED, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }
}