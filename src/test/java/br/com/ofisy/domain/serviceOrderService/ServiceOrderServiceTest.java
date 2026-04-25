package br.com.ofisy.domain.serviceOrderService;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderServiceTest {

    private UUID createRandomId() {
        return UUID.randomUUID();
    }

    @Nested
    class CreateServiceOrderService {
        @Test
        void shouldCreateServiceOrderServiceWithValidData() {
            var serviceId = createRandomId();
            var serviceOrderId = createRandomId();
            
            var result = ServiceOrderService.create(serviceId, serviceOrderId);
            
            assertThat(result).isNotNull();
            assertThat(result.getServiceId()).isEqualTo(serviceId);
            assertThat(result.getServiceOrderId()).isEqualTo(serviceOrderId);
        }

        @Test
        void shouldCreateServiceOrderServiceWithPendingStatus() {
            var result = ServiceOrderService.create(createRandomId(), createRandomId());
            assertThat(result.getStatus()).isEqualTo(ServiceOrderServiceStatus.PENDING);
        }

        @Test
        void shouldSetCreatedAtAndUpdatedAt() {
            var beforeCreation = LocalDateTime.now();
            var result = ServiceOrderService.create(createRandomId(), createRandomId());
            var afterCreation = LocalDateTime.now();
            
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    class CompleteServiceOrderService {
        @Test
        void shouldCompleteServiceOrderService() {
            var service = ServiceOrderService.create(createRandomId(), createRandomId());
            service.complete();
            
            assertThat(service.getStatus()).isEqualTo(ServiceOrderServiceStatus.COMPLETED);
            assertThat(service.getFinishedAt()).isNotNull();
        }
    }

    @Nested
    class CancelServiceOrderService {
        @Test
        void shouldCancelServiceOrderService() {
            var service = ServiceOrderService.create(createRandomId(), createRandomId());
            service.cancel();
            
            assertThat(service.getStatus()).isEqualTo(ServiceOrderServiceStatus.CANCELLED);
            assertThat(service.getFinishedAt()).isNotNull();
        }
    }

    @Nested
    class StartServiceOrderService {
        @Test
        void shouldStartServiceOrderService() {
            var service = ServiceOrderService.create(createRandomId(), createRandomId());
            service.start();
            
            assertThat(service.getStatus()).isEqualTo(ServiceOrderServiceStatus.IN_PROGRESS);
            assertThat(service.getStartedAt()).isNotNull();
        }
    }

    @Nested
    class StatusTransitions {
        @Test
        void shouldTransitionProperly() {
            var service = ServiceOrderService.create(createRandomId(), createRandomId());
            assertThat(service.getStatus()).isEqualTo(ServiceOrderServiceStatus.PENDING);
            
            service.start();
            assertThat(service.getStatus()).isEqualTo(ServiceOrderServiceStatus.IN_PROGRESS);
            
            service.complete();
            assertThat(service.getStatus()).isEqualTo(ServiceOrderServiceStatus.COMPLETED);
        }
    }
}

