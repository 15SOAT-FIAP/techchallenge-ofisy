package br.com.ofisy.domain.messaging;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class ProcessedEvent {

    private final UUID eventId;
    private final String eventType;
    private LocalDateTime processedAt;

    public ProcessedEvent(UUID eventId, String eventType) {
        this.eventId = eventId;
        this.eventType = eventType;
    }

    public static ProcessedEvent create(UUID eventId, String eventType) {
        ProcessedEvent event = new ProcessedEvent(eventId, eventType);
        event.processedAt = LocalDateTime.now();
        return event;
    }

    public static ProcessedEvent reconstruct(UUID eventId, String eventType, LocalDateTime processedAt) {
        ProcessedEvent event = new ProcessedEvent(eventId, eventType);
        event.processedAt = processedAt;
        return event;
    }
}