package br.com.ofisy.infrastructure.persistence.serviceOrderService;

import br.com.ofisy.domain.serviceOrderService.ServiceOrderService;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderServiceStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderServiceRepositoryImplTest {

    @Mock
    private JpaServiceOrderServiceRepository jpa;

    @InjectMocks
    private ServiceOrderServiceRepositoryImpl repository;

    private ServiceOrderService createValidServiceOrderService() {
        return ServiceOrderService.create(UUID.randomUUID(), UUID.randomUUID());
    }

    @Nested
    class SaveServiceOrderService {

        @Test
        void shouldSaveServiceOrderServiceSuccessfully() {
            var serviceOrderService = createValidServiceOrderService();

            when(jpa.save(any(ServiceOrderService.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = repository.save(serviceOrderService);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(serviceOrderService);
            verify(jpa).save(serviceOrderService);
        }

        @Test
        void shouldSaveServiceOrderServiceWithAllFields() {
            var serviceOrderService = createValidServiceOrderService();

            when(jpa.save(any(ServiceOrderService.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = repository.save(serviceOrderService);

            assertThat(result.getServiceId()).isNotNull();
            assertThat(result.getServiceOrderId()).isNotNull();
            assertThat(result.getStatus()).isEqualTo(ServiceOrderServiceStatus.PENDING);
            assertThat(result.getCreatedAt()).isNotNull();
            verify(jpa).save(serviceOrderService);
        }
    }

    @Nested
    class FindAllServiceOrderServices {

        @Test
        void shouldFindAllServiceOrderServices() {
            var service1 = createValidServiceOrderService();
            var service2 = createValidServiceOrderService();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(service1, service2));

            when(jpa.findAll(pageable)).thenReturn(page);

            var result = repository.findAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(jpa).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoResults() {
            var pageable = Pageable.unpaged();
            var page = new PageImpl<ServiceOrderService>(Collections.emptyList());

            when(jpa.findAll(pageable)).thenReturn(page);

            var result = repository.findAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verify(jpa).findAll(pageable);
        }

        @Test
        void shouldRespectPagination() {
            var service1 = createValidServiceOrderService();
            var service2 = createValidServiceOrderService();
            var pageable = Pageable.ofSize(1);
            var page = new PageImpl<>(List.of(service1), pageable, 2);

            when(jpa.findAll(pageable)).thenReturn(page);

            var result = repository.findAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(2);
            verify(jpa).findAll(pageable);
        }
    }

    @Nested
    class FindByIdServiceOrderService {

        @Test
        void shouldFindServiceOrderServiceById() {
            var serviceOrderService = createValidServiceOrderService();
            var id = UUID.randomUUID();

            when(jpa.findById(id)).thenReturn(Optional.of(serviceOrderService));

            var result = repository.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(serviceOrderService);
            verify(jpa).findById(id);
        }

        @Test
        void shouldReturnEmptyWhenServiceOrderServiceNotFound() {
            var id = UUID.randomUUID();

            when(jpa.findById(id)).thenReturn(Optional.empty());

            var result = repository.findById(id);

            assertThat(result).isEmpty();
            verify(jpa).findById(id);
        }
    }

    @Nested
    class FindByServiceIdServiceOrderService {

        @Test
        void shouldFindServiceOrderServicesByServiceId() {
            var serviceId = UUID.randomUUID();
            var service = createValidServiceOrderService();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(service));

            when(jpa.findByServiceId(serviceId, pageable)).thenReturn(page);

            var result = repository.findByServiceId(serviceId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(jpa).findByServiceId(serviceId, pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoServiceOrderServicesForServiceId() {
            var serviceId = UUID.randomUUID();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<ServiceOrderService>(Collections.emptyList());

            when(jpa.findByServiceId(serviceId, pageable)).thenReturn(page);

            var result = repository.findByServiceId(serviceId, pageable);

            assertThat(result.getContent()).isEmpty();
            verify(jpa).findByServiceId(serviceId, pageable);
        }
    }

    @Nested
    class FindByServiceOrderIdServiceOrderService {

        @Test
        void shouldFindServiceOrderServicesByServiceOrderId() {
            var serviceOrderId = UUID.randomUUID();
            var service = createValidServiceOrderService();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(service));

            when(jpa.findByServiceOrderId(serviceOrderId, pageable)).thenReturn(page);

            var result = repository.findByServiceOrderId(serviceOrderId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(jpa).findByServiceOrderId(serviceOrderId, pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoServiceOrderServicesForServiceOrderId() {
            var serviceOrderId = UUID.randomUUID();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<ServiceOrderService>(Collections.emptyList());

            when(jpa.findByServiceOrderId(serviceOrderId, pageable)).thenReturn(page);

            var result = repository.findByServiceOrderId(serviceOrderId, pageable);

            assertThat(result.getContent()).isEmpty();
            verify(jpa).findByServiceOrderId(serviceOrderId, pageable);
        }
    }

    @Nested
    class FindByStatusServiceOrderService {

        @Test
        void shouldFindServiceOrderServicesByStatus() {
            var status = ServiceOrderServiceStatus.PENDING;
            var service = createValidServiceOrderService();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(service));

            when(jpa.findByStatus(status, pageable)).thenReturn(page);

            var result = repository.findByStatus(status, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(jpa).findByStatus(status, pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoServiceOrderServicesForStatus() {
            var status = ServiceOrderServiceStatus.COMPLETED;
            var pageable = Pageable.unpaged();
            var page = new PageImpl<ServiceOrderService>(Collections.emptyList());

            when(jpa.findByStatus(status, pageable)).thenReturn(page);

            var result = repository.findByStatus(status, pageable);

            assertThat(result.getContent()).isEmpty();
            verify(jpa).findByStatus(status, pageable);
        }

        @Test
        void shouldFindServiceOrderServicesByDifferentStatuses() {
            var pageable = Pageable.unpaged();
            var service = createValidServiceOrderService();
            var page = new PageImpl<>(List.of(service));

            for (ServiceOrderServiceStatus status : ServiceOrderServiceStatus.values()) {
                when(jpa.findByStatus(status, pageable)).thenReturn(page);

                var result = repository.findByStatus(status, pageable);

                assertThat(result.getContent()).hasSize(1);
                verify(jpa).findByStatus(status, pageable);
            }
        }
    }
}

