package br.com.ofisy.infrastructure.persistence.serviceOrderExecution;

import br.com.ofisy.domain.serviceOrderExecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceOrderExecution.ServiceOrderExecutionStatus;
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
class ServiceOrderExecutionRepositoryImplTest {

    @Mock
    private JpaServiceOrderExecutionRepository jpa;

    @InjectMocks
    private ServiceOrderExecutionRepositoryImpl repository;

    private ServiceOrderExecution createValidServiceOrderExecution() {
        return ServiceOrderExecution.create(UUID.randomUUID(), UUID.randomUUID());
    }

    @Nested
    class SaveServiceOrderExecution {

        @Test
        void shouldSaveServiceOrderExecutionSuccessfully() {
            var serviceOrderExecution = createValidServiceOrderExecution();

            when(jpa.save(any(ServiceOrderExecution.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = repository.save(serviceOrderExecution);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(serviceOrderExecution);
            verify(jpa).save(serviceOrderExecution);
        }

        @Test
        void shouldSaveServiceOrderExecutionWithAllFields() {
            var serviceOrderExecution = createValidServiceOrderExecution();

            when(jpa.save(any(ServiceOrderExecution.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = repository.save(serviceOrderExecution);

            assertThat(result.getServiceCatalogId()).isNotNull();
            assertThat(result.getServiceOrderId()).isNotNull();
            assertThat(result.getStatus()).isEqualTo(ServiceOrderExecutionStatus.PENDING);
            assertThat(result.getCreatedAt()).isNotNull();
            verify(jpa).save(serviceOrderExecution);
        }
    }

    @Nested
    class FindAllServicesOrderExecution {

        @Test
        void shouldFindAllServiceOrderExecutions() {
            var service1 = createValidServiceOrderExecution();
            var service2 = createValidServiceOrderExecution();
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
            var page = new PageImpl<ServiceOrderExecution>(Collections.emptyList());

            when(jpa.findAll(pageable)).thenReturn(page);

            var result = repository.findAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            verify(jpa).findAll(pageable);
        }

        @Test
        void shouldRespectPagination() {
            var service1 = createValidServiceOrderExecution();
            var service2 = createValidServiceOrderExecution();
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
    class FindByIdServiceOrderExecution {

        @Test
        void shouldFindServiceOrderExecutionById() {
            var serviceOrderExecution = createValidServiceOrderExecution();
            var id = UUID.randomUUID();

            when(jpa.findById(id)).thenReturn(Optional.of(serviceOrderExecution));

            var result = repository.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(serviceOrderExecution);
            verify(jpa).findById(id);
        }

        @Test
        void shouldReturnEmptyWhenServiceOrderExecutionNotFound() {
            var id = UUID.randomUUID();

            when(jpa.findById(id)).thenReturn(Optional.empty());

            var result = repository.findById(id);

            assertThat(result).isEmpty();
            verify(jpa).findById(id);
        }
    }

    @Nested
    class findByServiceCatalogIdServiceOrderExecution {

        @Test
        void shouldFindServiceOrderExecutionsByServiceCatalogId() {
            var serviceCatalogId = UUID.randomUUID();
            var service = createValidServiceOrderExecution();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(service));

            when(jpa.findByServiceCatalogId(serviceCatalogId, pageable)).thenReturn(page);

            var result = repository.findByServiceCatalogId(serviceCatalogId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(jpa).findByServiceCatalogId(serviceCatalogId, pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoServiceOrderExecutionsForServiceCatalogId() {
            var serviceCatalogId = UUID.randomUUID();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<ServiceOrderExecution>(Collections.emptyList());

            when(jpa.findByServiceCatalogId(serviceCatalogId, pageable)).thenReturn(page);

            var result = repository.findByServiceCatalogId(serviceCatalogId, pageable);

            assertThat(result.getContent()).isEmpty();
            verify(jpa).findByServiceCatalogId(serviceCatalogId, pageable);
        }
    }

    @Nested
    class FindByServiceOrderIdServiceOrderExecution {

        @Test
        void shouldFindServiceOrderExecutionsByServiceOrderId() {
            var serviceOrderId = UUID.randomUUID();
            var service = createValidServiceOrderExecution();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(service));

            when(jpa.findByServiceOrderId(serviceOrderId, pageable)).thenReturn(page);

            var result = repository.findByServiceOrderId(serviceOrderId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(jpa).findByServiceOrderId(serviceOrderId, pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoServiceOrderExecutionsForServiceOrderId() {
            var serviceOrderId = UUID.randomUUID();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<ServiceOrderExecution>(Collections.emptyList());

            when(jpa.findByServiceOrderId(serviceOrderId, pageable)).thenReturn(page);

            var result = repository.findByServiceOrderId(serviceOrderId, pageable);

            assertThat(result.getContent()).isEmpty();
            verify(jpa).findByServiceOrderId(serviceOrderId, pageable);
        }
    }

    @Nested
    class FindByStatusServiceOrderExecution {

        @Test
        void shouldFindServiceOrderExecutionsByStatus() {
            var status = ServiceOrderExecutionStatus.PENDING;
            var service = createValidServiceOrderExecution();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(service));

            when(jpa.findByStatus(status, pageable)).thenReturn(page);

            var result = repository.findByStatus(status, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(jpa).findByStatus(status, pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoServiceOrderExecutionsForStatus() {
            var status = ServiceOrderExecutionStatus.COMPLETED;
            var pageable = Pageable.unpaged();
            var page = new PageImpl<ServiceOrderExecution>(Collections.emptyList());

            when(jpa.findByStatus(status, pageable)).thenReturn(page);

            var result = repository.findByStatus(status, pageable);

            assertThat(result.getContent()).isEmpty();
            verify(jpa).findByStatus(status, pageable);
        }

        @Test
        void shouldFindServiceOrderExecutionsByDifferentStatuses() {
            var pageable = Pageable.unpaged();
            var service = createValidServiceOrderExecution();
            var page = new PageImpl<>(List.of(service));

            for (ServiceOrderExecutionStatus status : ServiceOrderExecutionStatus.values()) {
                when(jpa.findByStatus(status, pageable)).thenReturn(page);

                var result = repository.findByStatus(status, pageable);

                assertThat(result.getContent()).hasSize(1);
                verify(jpa).findByStatus(status, pageable);
            }
        }
    }
}

