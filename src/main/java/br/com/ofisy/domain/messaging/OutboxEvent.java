package br.com.ofisy.domain.messaging;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
public class OutboxEvent {

    private final UUID eventId;
    private final String eventType;
    private final UUID serviceOrderId;
    private final Object payload;
    private final Instant occurredAt;

    private OutboxEvent(UUID eventId, String eventType, UUID serviceOrderId, Object payload, Instant occurredAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.serviceOrderId = serviceOrderId;
        this.payload = payload;
        this.occurredAt = occurredAt;
    }

    public static OutboxEvent create(String eventType, UUID serviceOrderId, Object payload) {
        return new OutboxEvent(UUID.randomUUID(), eventType, serviceOrderId, payload, Instant.now());
    }
}