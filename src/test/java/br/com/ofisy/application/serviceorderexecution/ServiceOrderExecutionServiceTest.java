package br.com.ofisy.application.serviceorderexecution;

import br.com.ofisy.application.serviceorder.ServiceOrderFinalizationService;
import br.com.ofisy.application.serviceorder.ServiceOrderService;
import br.com.ofisy.application.serviceorderexecution.dto.ServiceOrderExecutionRequestDTO;
import br.com.ofisy.application.serviceorderexecution.exceptions.ServiceOrderExecutionNotFoundException;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ServiceOrderExecutionServiceTest {

    @Mock
    private ServiceOrderExecutionRepository repository;

    @Mock
    private ServiceOrderFinalizationService finalizationService;

    @Mock
    private ServiceOrderService serviceOrderService;

    @InjectMocks
    private ServiceOrderExecutionService application;

    private ServiceOrderExecution createValidServiceOrderExecution() {
        return ServiceOrderExecution.create(UUID.randomUUID(), UUID.randomUUID());
    }

    @Nested
    class CreateServiceOrderExecution {

        @Test
        void shouldCreateServiceOrderExecutionSuccessfully() {
            var serviceCatalogId = UUID.randomUUID();
            var serviceOrderId = UUID.randomUUID();
            var dto = new ServiceOrderExecutionRequestDTO(serviceCatalogId, serviceOrderId);

            when(repository.save(any(ServiceOrderExecution.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = application.create(dto);

            assertThat(result).isNotNull();
            assertThat(result.serviceCatalogId()).isEqualTo(serviceCatalogId);
            assertThat(result.serviceOrderId()).isEqualTo(serviceOrderId);
            assertThat(result.status()).isEqualTo(ServiceOrderExecutionStatus.PENDING);
            verify(repository).save(any(ServiceOrderExecution.class));
        }

        @Test
        void shouldSetCreatedAtOnCreation() {
            var serviceCatalogId = UUID.randomUUID();
            var serviceOrderId = UUID.randomUUID();
            var dto = new ServiceOrderExecutionRequestDTO(serviceCatalogId, serviceOrderId);

            when(repository.save(any(ServiceOrderExecution.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = application.create(dto);

            assertThat(result.createdAt()).isNotNull();
        }
    }

    @Nested
    class FindServiceOrderExecution {

        @Test
        void shouldFindServiceOrderExecutionById() {
            var serviceOrderExecution = createValidServiceOrderExecution();
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.of(serviceOrderExecution));

            var result = application.findById(id);

            assertThat(result)
                    .isNotNull()
                    .isEqualTo(ServiceOrderExecutionMapper.toDTO(serviceOrderExecution));
            verify(repository).findById(id);
        }

        @Test
        void shouldThrowExceptionWhenServiceOrderExecutionNotFound() {
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> application.findById(id))
                    .isInstanceOf(ServiceOrderExecutionNotFoundException.class);

            verify(repository).findById(id);
        }

        @Test
        void shouldFindAllServiceOrderExecutions() {
            var serviceOrderExecution1 = createValidServiceOrderExecution();
            var serviceOrderExecution2 = createValidServiceOrderExecution();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(serviceOrderExecution1, serviceOrderExecution2));

            when(repository.findAll(pageable)).thenReturn(page);

            var result = application.findAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(repository).findAll(pageable);
        }

        @Test
        void shouldFindServiceOrderExecutionsByServiceCatalogId() {
            var serviceCatalogId = UUID.randomUUID();
            var serviceOrderExecution = createValidServiceOrderExecution();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(serviceOrderExecution));

            when(repository.findByServiceCatalogId(serviceCatalogId, pageable)).thenReturn(page);

            var result = application.findByServiceCatalogId(serviceCatalogId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByServiceCatalogId(serviceCatalogId, pageable);
        }

        @Test
        void shouldFindServiceOrderExecutionsByServiceOrderId() {
            var serviceOrderId = UUID.randomUUID();
            var serviceOrderExecution = createValidServiceOrderExecution();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(serviceOrderExecution));

            when(repository.findByServiceOrderId(serviceOrderId, pageable)).thenReturn(page);

            var result = application.findByServiceOrderId(serviceOrderId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByServiceOrderId(serviceOrderId, pageable);
        }

        @Test
        void shouldFindServiceOrderExecutionsByStatus() {
            var status = "PENDING";
            var serviceOrderExecution = createValidServiceOrderExecution();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(serviceOrderExecution));

            when(repository.findByStatus(ServiceOrderExecutionStatus.PENDING, pageable)).thenReturn(page);

            var result = application.findByStatus(status, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByStatus(ServiceOrderExecutionStatus.PENDING, pageable);
        }
    }

    @Nested
    class CompleteServiceOrderExecution {

        @Test
        void shouldCompleteServiceOrderExecutionSuccessfully() {
            var serviceOrderExecution = createValidServiceOrderExecution();
            serviceOrderExecution.start();
            var id = UUID.randomUUID();
            var serviceOrderId = serviceOrderExecution.getServiceOrderId();

            when(repository.findById(id)).thenReturn(Optional.of(serviceOrderExecution));
            when(repository.save(any(ServiceOrderExecution.class))).thenAnswer(inv -> inv.getArgument(0));
            when(repository.countByServiceOrderId(serviceOrderId)).thenReturn(2L);
            when(repository.countByServiceOrderIdAndStatus(serviceOrderId, ServiceOrderExecutionStatus.COMPLETED))
                    .thenReturn(1L);

            var result = application.complete(id);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(ServiceOrderExecutionStatus.COMPLETED);
            assertThat(result.finishedAt()).isNotNull();
            verify(repository).findById(id);
            verify(repository).save(any(ServiceOrderExecution.class));
            verify(finalizationService, never()).finish(any());
        }

        @Test
        void shouldFinishServiceOrderWhenAllExecutionsCompleted() {
            var serviceOrderExecution = createValidServiceOrderExecution();
            serviceOrderExecution.start();
            var id = UUID.randomUUID();
            var serviceOrderId = serviceOrderExecution.getServiceOrderId();

            when(repository.findById(id)).thenReturn(Optional.of(serviceOrderExecution));
            when(repository.save(any(ServiceOrderExecution.class))).thenAnswer(inv -> inv.getArgument(0));
            when(repository.countByServiceOrderId(serviceOrderId)).thenReturn(2L);
            when(repository.countByServiceOrderIdAndStatus(serviceOrderId, ServiceOrderExecutionStatus.COMPLETED))
                    .thenReturn(2L);

            application.complete(id);

            verify(finalizationService).finish(serviceOrderId);
        }

        @Test
        void shouldNotFinishServiceOrderWhenNoExecutionsExist() {
            var serviceOrderExecution = createValidServiceOrderExecution();
            serviceOrderExecution.start();
            var id = UUID.randomUUID();
            var serviceOrderId = serviceOrderExecution.getServiceOrderId();

            when(repository.findById(id)).thenReturn(Optional.of(serviceOrderExecution));
            when(repository.save(any(ServiceOrderExecution.class))).thenAnswer(inv -> inv.getArgument(0));
            when(repository.countByServiceOrderId(serviceOrderId)).thenReturn(0L);
            when(repository.countByServiceOrderIdAndStatus(serviceOrderId, ServiceOrderExecutionStatus.COMPLETED))
                    .thenReturn(0L);

            application.complete(id);

            verify(finalizationService, never()).finish(any());
        }

        @Test
        void shouldThrowExceptionWhenCompleteNonExistentService() {
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> application.complete(id))
                    .isInstanceOf(ServiceOrderExecutionNotFoundException.class);

            verify(repository, never()).save(any(ServiceOrderExecution.class));
            verify(finalizationService, never()).finish(any());
        }
    }

    @Nested
    class CancelServiceOrderExecution {

        @Test
        void shouldCancelServiceOrderExecutionSuccessfully() {
            var serviceOrderExecution = createValidServiceOrderExecution();
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.of(serviceOrderExecution));
            when(repository.save(any(ServiceOrderExecution.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = application.cancel(id);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(ServiceOrderExecutionStatus.CANCELLED);
            assertThat(result.finishedAt()).isNotNull();
            verify(repository).findById(id);
            verify(repository).save(any(ServiceOrderExecution.class));
        }

        @Test
        void shouldThrowExceptionWhenCancelNonExistentService() {
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> application.cancel(id))
                    .isInstanceOf(ServiceOrderExecutionNotFoundException.class);

            verify(repository, never()).save(any(ServiceOrderExecution.class));
        }
    }

    @Nested
    class StartServiceOrderExecution {

        @Test
        void shouldStartServiceOrderExecutionSuccessfully() {
            var serviceOrderExecution = createValidServiceOrderExecution();
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.of(serviceOrderExecution));
            when(repository.save(any(ServiceOrderExecution.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = application.start(id);

            assertThat(result).isNotNull();
            assertThat(result.status()).isEqualTo(ServiceOrderExecutionStatus.IN_PROGRESS);
            assertThat(result.startedAt()).isNotNull();
            verify(repository).findById(id);
            verify(repository).save(any(ServiceOrderExecution.class));
            verify(serviceOrderService).startExecution(serviceOrderExecution.getServiceOrderId());
        }

        @Test
        void shouldThrowExceptionWhenStartNonExistentService() {
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> application.start(id))
                    .isInstanceOf(ServiceOrderExecutionNotFoundException.class);

            verify(repository, never()).save(any(ServiceOrderExecution.class));
            verify(serviceOrderService, never()).startExecution(any());
        }
    }

@Nested
    class GetAverageExecutionTimeByService {

        @Test
        void shouldReturnZeroWhenNoServiceOrderExecutions() {
            var serviceCatalogId = UUID.randomUUID();
            var emptyPage = new PageImpl<ServiceOrderExecution>(Collections.emptyList());

            when(repository.findByServiceCatalogId(serviceCatalogId, Pageable.unpaged())).thenReturn(emptyPage);

            var result = application.getAverageExecutionTimeByService(serviceCatalogId);

            assertThat(result).isZero();
        }

        @Test
        void shouldReturnZeroWhenNoCompletedServices() {
            var serviceCatalogId = UUID.randomUUID();
            var serviceOrderExecution = createValidServiceOrderExecution();
            var page = new PageImpl<>(List.of(serviceOrderExecution));

            when(repository.findByServiceCatalogId(serviceCatalogId, Pageable.unpaged())).thenReturn(page);

            var result = application.getAverageExecutionTimeByService(serviceCatalogId);

            assertThat(result).isZero();
        }

        @Test
        void shouldCalculateAverageExecutionTimeCorrectly() {
            var serviceCatalogId = UUID.randomUUID();
            var serviceOrderExecution = createValidServiceOrderExecution();

            var startTime = LocalDateTime.now().minusMinutes(10);
            var endTime = LocalDateTime.now();

            try {
                var startedAtField = ServiceOrderExecution.class.getDeclaredField("startedAt");
                startedAtField.setAccessible(true);
                startedAtField.set(serviceOrderExecution, startTime);

                var finishedAtField = ServiceOrderExecution.class.getDeclaredField("finishedAt");
                finishedAtField.setAccessible(true);
                finishedAtField.set(serviceOrderExecution, endTime);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            var page = new PageImpl<>(List.of(serviceOrderExecution));

            when(repository.findByServiceCatalogId(serviceCatalogId, Pageable.unpaged())).thenReturn(page);

            var result = application.getAverageExecutionTimeByService(serviceCatalogId);

            assertThat(result)
                    .isGreaterThan(0)
                            .isLessThanOrEqualTo(10.0);
        }

        @Test
        void shouldIgnoreIncompleteServices() {
            var serviceCatalogId = UUID.randomUUID();
            var completedService = createValidServiceOrderExecution();
            var incompleteService = createValidServiceOrderExecution();

            try {
                var startedAtField = ServiceOrderExecution.class.getDeclaredField("startedAt");
                startedAtField.setAccessible(true);
                startedAtField.set(completedService, LocalDateTime.now().minusMinutes(5));

                var finishedAtField = ServiceOrderExecution.class.getDeclaredField("finishedAt");
                finishedAtField.setAccessible(true);
                finishedAtField.set(completedService, LocalDateTime.now());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            var page = new PageImpl<>(List.of(completedService, incompleteService));

            when(repository.findByServiceCatalogId(serviceCatalogId, Pageable.unpaged())).thenReturn(page);

            var result = application.getAverageExecutionTimeByService(serviceCatalogId);

            assertThat(result).isGreaterThan(0);
        }

        @Test
        void shouldReturnCorrectAverageWithMultipleCompletedServices() {
            var serviceCatalogId = UUID.randomUUID();
            var service1 = createValidServiceOrderExecution();
            var service2 = createValidServiceOrderExecution();

            try {
                var startedAtField = ServiceOrderExecution.class.getDeclaredField("startedAt");
                startedAtField.setAccessible(true);

                var finishedAtField = ServiceOrderExecution.class.getDeclaredField("finishedAt");
                finishedAtField.setAccessible(true);

                startedAtField.set(service1, LocalDateTime.now().minusMinutes(10));
                finishedAtField.set(service1, LocalDateTime.now());

                startedAtField.set(service2, LocalDateTime.now().minusMinutes(20));
                finishedAtField.set(service2, LocalDateTime.now());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            var page = new PageImpl<>(List.of(service1, service2));

            when(repository.findByServiceCatalogId(serviceCatalogId, Pageable.unpaged())).thenReturn(page);

            var result = application.getAverageExecutionTimeByService(serviceCatalogId);

            assertThat(result).isGreaterThan(0);
        }
    }
}

