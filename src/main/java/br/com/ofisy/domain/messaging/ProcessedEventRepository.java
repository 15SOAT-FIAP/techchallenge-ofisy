package br.com.ofisy.domain.messaging;

import java.util.UUID;

public interface ProcessedEventRepository {

    boolean existsById(UUID eventId);

    ProcessedEvent save(ProcessedEvent processedEvent);
}