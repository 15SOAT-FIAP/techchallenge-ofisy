package br.com.ofisy.adapters.gateways.messaging;

import br.com.ofisy.domain.messaging.OutboxEvent;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class OutboxEventMapper {

    public static OutboxEventEntity toEntity(OutboxEvent domain, String payloadJson) {
        return OutboxEventEntity.builder()
                .eventId(domain.getEventId())
                .eventType(domain.getEventType())
                .serviceOrderId(domain.getServiceOrderId())
                .payload(payloadJson)
                .occurredAt(domain.getOccurredAt())
                .build();
    }
}