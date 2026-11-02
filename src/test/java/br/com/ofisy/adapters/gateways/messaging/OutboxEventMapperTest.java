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

        OutboxEventEntity response = OutboxEventMapper.toEntity(outboxEvent, payloadJson);

        assertNotNull(response);
        assertEquals(outboxEvent.getEventId(), response.getId());
        assertEquals(outboxEvent.getEventType(), response.getEventType());
        assertEquals(outboxEvent.getServiceOrderId(), response.getServiceOrderId());
        assertEquals(payloadJson, response.getPayload());
        assertEquals(outboxEvent.getOccurredAt(), response.getOccurredAt());
        assertNull(response.getPublishedAt());
        assertTrue(response.isNew());
    }
}