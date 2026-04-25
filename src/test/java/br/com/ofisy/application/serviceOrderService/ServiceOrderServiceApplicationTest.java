package br.com.ofisy.application.serviceOrderService;

import br.com.ofisy.application.service.ServiceApplication;
import br.com.ofisy.application.serviceOrderService.dto.ServiceOrderServiceRequestDTO;
import br.com.ofisy.application.serviceOrderService.exceptions.ServiceOrderServiceNotFoundException;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderService;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderServiceRepository;
import br.com.ofisy.domain.serviceOrderService.ServiceOrderServiceStatus;
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
class ServiceOrderServiceApplicationTest {

    @Mock
    private ServiceOrderServiceRepository repository;

    @Mock
    private ServiceApplication serviceApplication;

    @InjectMocks
    private ServiceOrderServiceApplication application;

    private ServiceOrderService createValidServiceOrderService() {
        return ServiceOrderService.create(UUID.randomUUID(), UUID.randomUUID());
    }

    @Nested
    class CreateServiceOrderService {

        @Test
        void shouldCreateServiceOrderServiceSuccessfully() {
            var serviceId = UUID.randomUUID();
            var serviceOrderId = UUID.randomUUID();
            var dto = new ServiceOrderServiceRequestDTO(serviceId, serviceOrderId);
            var expectedService = ServiceOrderService.create(serviceId, serviceOrderId);

            when(repository.save(any(ServiceOrderService.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = application.create(dto);

            assertThat(result).isNotNull();
            assertThat(result.getServiceId()).isEqualTo(serviceId);
            assertThat(result.getServiceOrderId()).isEqualTo(serviceOrderId);
            assertThat(result.getStatus()).isEqualTo(ServiceOrderServiceStatus.PENDING);
            verify(repository).save(any(ServiceOrderService.class));
        }

        @Test
        void shouldSetCreatedAtOnCreation() {
            var serviceId = UUID.randomUUID();
            var serviceOrderId = UUID.randomUUID();
            var dto = new ServiceOrderServiceRequestDTO(serviceId, serviceOrderId);

            when(repository.save(any(ServiceOrderService.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = application.create(dto);

            assertThat(result.getCreatedAt()).isNotNull();
        }
    }

    @Nested
    class FindServiceOrderService {

        @Test
        void shouldFindServiceOrderServiceById() {
            var serviceOrderService = createValidServiceOrderService();
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.of(serviceOrderService));

            var result = application.findById(id);

            assertThat(result).isNotNull();
            assertThat(result).isEqualTo(serviceOrderService);
            verify(repository).findById(id);
        }

        @Test
        void shouldThrowExceptionWhenServiceOrderServiceNotFound() {
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> application.findById(id))
                    .isInstanceOf(ServiceOrderServiceNotFoundException.class);

            verify(repository).findById(id);
        }

        @Test
        void shouldFindAllServiceOrderServices() {
            var serviceOrderService1 = createValidServiceOrderService();
            var serviceOrderService2 = createValidServiceOrderService();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(serviceOrderService1, serviceOrderService2));

            when(repository.findAll(pageable)).thenReturn(page);

            var result = application.findAll(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            verify(repository).findAll(pageable);
        }

        @Test
        void shouldFindServiceOrderServicesByServiceId() {
            var serviceId = UUID.randomUUID();
            var serviceOrderService = createValidServiceOrderService();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(serviceOrderService));

            when(repository.findByServiceId(serviceId, pageable)).thenReturn(page);

            var result = application.findByServiceId(serviceId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByServiceId(serviceId, pageable);
        }

        @Test
        void shouldFindServiceOrderServicesByServiceOrderId() {
            var serviceOrderId = UUID.randomUUID();
            var serviceOrderService = createValidServiceOrderService();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(serviceOrderService));

            when(repository.findByServiceOrderId(serviceOrderId, pageable)).thenReturn(page);

            var result = application.findByServiceOrderId(serviceOrderId, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByServiceOrderId(serviceOrderId, pageable);
        }

        @Test
        void shouldFindServiceOrderServicesByStatus() {
            var status = "PENDING";
            var serviceOrderService = createValidServiceOrderService();
            var pageable = Pageable.unpaged();
            var page = new PageImpl<>(List.of(serviceOrderService));

            when(repository.findByStatus(ServiceOrderServiceStatus.PENDING, pageable)).thenReturn(page);

            var result = application.findByStatus(status, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findByStatus(ServiceOrderServiceStatus.PENDING, pageable);
        }
    }

    @Nested
    class CompleteServiceOrderService {

        @Test
        void shouldCompleteServiceOrderServiceSuccessfully() {
            var serviceOrderService = createValidServiceOrderService();
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.of(serviceOrderService));
            when(repository.save(any(ServiceOrderService.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = application.complete(id);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(ServiceOrderServiceStatus.COMPLETED);
            assertThat(result.getFinishedAt()).isNotNull();
            verify(repository).findById(id);
            verify(repository).save(any(ServiceOrderService.class));
        }

        @Test
        void shouldThrowExceptionWhenCompleteNonExistentService() {
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> application.complete(id))
                    .isInstanceOf(ServiceOrderServiceNotFoundException.class);

            verify(repository, never()).save(any(ServiceOrderService.class));
        }
    }

    @Nested
    class CancelServiceOrderService {

        @Test
        void shouldCancelServiceOrderServiceSuccessfully() {
            var serviceOrderService = createValidServiceOrderService();
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.of(serviceOrderService));
            when(repository.save(any(ServiceOrderService.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = application.cancel(id);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(ServiceOrderServiceStatus.CANCELLED);
            assertThat(result.getFinishedAt()).isNotNull();
            verify(repository).findById(id);
            verify(repository).save(any(ServiceOrderService.class));
        }

        @Test
        void shouldThrowExceptionWhenCancelNonExistentService() {
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> application.cancel(id))
                    .isInstanceOf(ServiceOrderServiceNotFoundException.class);

            verify(repository, never()).save(any(ServiceOrderService.class));
        }
    }

    @Nested
    class StartServiceOrderService {

        @Test
        void shouldStartServiceOrderServiceSuccessfully() {
            var serviceOrderService = createValidServiceOrderService();
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.of(serviceOrderService));
            when(repository.save(any(ServiceOrderService.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = application.start(id);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(ServiceOrderServiceStatus.IN_PROGRESS);
            assertThat(result.getStartedAt()).isNotNull();
            verify(repository).findById(id);
            verify(repository).save(any(ServiceOrderService.class));
        }

        @Test
        void shouldThrowExceptionWhenStartNonExistentService() {
            var id = UUID.randomUUID();

            when(repository.findById(id)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> application.start(id))
                    .isInstanceOf(ServiceOrderServiceNotFoundException.class);

            verify(repository, never()).save(any(ServiceOrderService.class));
        }
    }

    @Nested
    class GetAverageExecutionTimeByService {

        @Test
        void shouldReturnZeroWhenNoServiceOrderServices() {
            var serviceId = UUID.randomUUID();
            var emptyPage = new PageImpl<ServiceOrderService>(Collections.emptyList());

            when(repository.findByServiceId(serviceId, Pageable.unpaged())).thenReturn(emptyPage);

            var result = application.getAverageExecutionTimeByService(serviceId);

            assertThat(result).isZero();
        }

        @Test
        void shouldReturnZeroWhenNoCompletedServices() {
            var serviceId = UUID.randomUUID();
            var serviceOrderService = createValidServiceOrderService();
            var page = new PageImpl<>(List.of(serviceOrderService));

            when(repository.findByServiceId(serviceId, Pageable.unpaged())).thenReturn(page);

            var result = application.getAverageExecutionTimeByService(serviceId);

            assertThat(result).isZero();
        }

        @Test
        void shouldCalculateAverageExecutionTimeCorrectly() {
            var serviceId = UUID.randomUUID();
            var serviceOrderService = createValidServiceOrderService();

            var startTime = LocalDateTime.now().minusMinutes(10);
            var endTime = LocalDateTime.now();

            try {
                var startedAtField = ServiceOrderService.class.getDeclaredField("startedAt");
                startedAtField.setAccessible(true);
                startedAtField.set(serviceOrderService, startTime);

                var finishedAtField = ServiceOrderService.class.getDeclaredField("finishedAt");
                finishedAtField.setAccessible(true);
                finishedAtField.set(serviceOrderService, endTime);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            var page = new PageImpl<>(List.of(serviceOrderService));

            when(repository.findByServiceId(serviceId, Pageable.unpaged())).thenReturn(page);

            var result = application.getAverageExecutionTimeByService(serviceId);

            assertThat(result).isGreaterThan(0);
            assertThat(result).isLessThanOrEqualTo(10.0);
        }

        @Test
        void shouldIgnoreIncompleteServices() {
            var serviceId = UUID.randomUUID();
            var completedService = createValidServiceOrderService();
            var incompleteService = createValidServiceOrderService();

            try {
                var startedAtField = ServiceOrderService.class.getDeclaredField("startedAt");
                startedAtField.setAccessible(true);
                startedAtField.set(completedService, LocalDateTime.now().minusMinutes(5));

                var finishedAtField = ServiceOrderService.class.getDeclaredField("finishedAt");
                finishedAtField.setAccessible(true);
                finishedAtField.set(completedService, LocalDateTime.now());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            var page = new PageImpl<>(List.of(completedService, incompleteService));

            when(repository.findByServiceId(serviceId, Pageable.unpaged())).thenReturn(page);

            var result = application.getAverageExecutionTimeByService(serviceId);

            assertThat(result).isGreaterThan(0);
        }

        @Test
        void shouldReturnCorrectAverageWithMultipleCompletedServices() {
            var serviceId = UUID.randomUUID();
            var service1 = createValidServiceOrderService();
            var service2 = createValidServiceOrderService();

            try {
                var startedAtField = ServiceOrderService.class.getDeclaredField("startedAt");
                startedAtField.setAccessible(true);

                var finishedAtField = ServiceOrderService.class.getDeclaredField("finishedAt");
                finishedAtField.setAccessible(true);

                startedAtField.set(service1, LocalDateTime.now().minusMinutes(10));
                finishedAtField.set(service1, LocalDateTime.now());

                startedAtField.set(service2, LocalDateTime.now().minusMinutes(20));
                finishedAtField.set(service2, LocalDateTime.now());
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            var page = new PageImpl<>(List.of(service1, service2));

            when(repository.findByServiceId(serviceId, Pageable.unpaged())).thenReturn(page);

            var result = application.getAverageExecutionTimeByService(serviceId);

            assertThat(result).isGreaterThan(0);
        }
    }
}

