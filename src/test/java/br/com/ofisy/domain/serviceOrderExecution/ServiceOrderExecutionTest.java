package br.com.ofisy.domain.serviceOrderExecution;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderExecutionTest {

    private UUID createRandomId() {
        return UUID.randomUUID();
    }

    @Nested
    class CreateServiceOrderExecution {
        @Test
        void shouldCreateServiceOrderExecutionWithValidData() {
            var serviceCatalogId = createRandomId();
            var serviceOrderId = createRandomId();
            
            var result = ServiceOrderExecution.create(serviceCatalogId, serviceOrderId);
            
            assertThat(result).isNotNull();
            assertThat(result.getServiceCatalogId()).isEqualTo(serviceCatalogId);
            assertThat(result.getServiceOrderId()).isEqualTo(serviceOrderId);
        }

        @Test
        void shouldCreateServiceOrderExecutionWithPendingStatus() {
            var result = ServiceOrderExecution.create(createRandomId(), createRandomId());
            assertThat(result.getStatus()).isEqualTo(ServiceOrderExecutionStatus.PENDING);
        }

        @Test
        void shouldSetCreatedAtAndUpdatedAt() {
            var beforeCreation = LocalDateTime.now();
            var result = ServiceOrderExecution.create(createRandomId(), createRandomId());
            var afterCreation = LocalDateTime.now();
            
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    class CompleteServiceOrderExecution {
        @Test
        void shouldCompleteServiceOrderExecution() {
            var service = ServiceOrderExecution.create(createRandomId(), createRandomId());
            service.complete();
            
            assertThat(service.getStatus()).isEqualTo(ServiceOrderExecutionStatus.COMPLETED);
            assertThat(service.getFinishedAt()).isNotNull();
        }
    }

    @Nested
    class CancelServiceOrderExecution {
        @Test
        void shouldCancelServiceOrderExecution() {
            var service = ServiceOrderExecution.create(createRandomId(), createRandomId());
            service.cancel();
            
            assertThat(service.getStatus()).isEqualTo(ServiceOrderExecutionStatus.CANCELLED);
            assertThat(service.getFinishedAt()).isNotNull();
        }
    }

    @Nested
    class StartServiceOrderExecution {
        @Test
        void shouldStartServiceOrderExecution() {
            var service = ServiceOrderExecution.create(createRandomId(), createRandomId());
            service.start();
            
            assertThat(service.getStatus()).isEqualTo(ServiceOrderExecutionStatus.IN_PROGRESS);
            assertThat(service.getStartedAt()).isNotNull();
        }
    }

    @Nested
    class StatusTransitions {
        @Test
        void shouldTransitionProperly() {
            var service = ServiceOrderExecution.create(createRandomId(), createRandomId());
            assertThat(service.getStatus()).isEqualTo(ServiceOrderExecutionStatus.PENDING);
            
            service.start();
            assertThat(service.getStatus()).isEqualTo(ServiceOrderExecutionStatus.IN_PROGRESS);
            
            service.complete();
            assertThat(service.getStatus()).isEqualTo(ServiceOrderExecutionStatus.COMPLETED);
        }
    }
}

