package br.com.ofisy.domain.messaging;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class OutboxEventTest {

    @Nested
    class CreateOutboxEvent {
        @Test
        void shouldCreateOutboxEventWithGeneratedIdAndOccurredAt() {
            var serviceOrderId = UUID.randomUUID();
            var payload = Map.of("quoteId", UUID.randomUUID());

            var result = OutboxEvent.create("ServiceOrderCancelled", serviceOrderId, payload);

            assertThat(result.getEventId()).isNotNull();
            assertThat(result.getEventType()).isEqualTo("ServiceOrderCancelled");
            assertThat(result.getServiceOrderId()).isEqualTo(serviceOrderId);
            assertThat(result.getPayload()).isEqualTo(payload);
            assertThat(result.getOccurredAt()).isNotNull();
        }

        @Test
        void shouldGenerateDifferentIdsForEachEvent() {
            var serviceOrderId = UUID.randomUUID();

            var first = OutboxEvent.create("ServiceOrderCancelled", serviceOrderId, Map.of());
            var second = OutboxEvent.create("ServiceOrderCancelled", serviceOrderId, Map.of());

            assertThat(first.getEventId()).isNotEqualTo(second.getEventId());
        }
    }
}