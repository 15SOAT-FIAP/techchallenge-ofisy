package br.com.ofisy.adapters.gateways.messaging;

import br.com.ofisy.domain.messaging.OutboxEvent;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OutboxEventMapperTest {
    @Test
    void shouldConvertDomainToEntity() {
        OutboxEvent outboxEvent = OutboxEvent.create("ServiceOrderCancelled", UUID.randomUUID(), Map.of());
        String payloadJson = "{\"quoteId\":\"%s\"}".formatted(UUID.randomUUID());

        String traceContextJson = "{\"traceparent\":\"00-6af793d30000000025cb7731d653ab37-6d0c2e20f86d8721-01\"}";

        OutboxEventEntity response = OutboxEventMapper.toEntity(outboxEvent, payloadJson, traceContextJson);

        assertNotNull(response);
        assertEquals(outboxEvent.getEventId(), response.getId());
        assertEquals(outboxEvent.getEventType(), response.getEventType());
        assertEquals(outboxEvent.getServiceOrderId(), response.getServiceOrderId());
        assertEquals(payloadJson, response.getPayload());
        assertEquals(outboxEvent.getOccurredAt(), response.getOccurredAt());
        assertEquals(traceContextJson, response.getTraceContext());
        assertNull(response.getPublishedAt());
        assertTrue(response.isNew());
    }
}