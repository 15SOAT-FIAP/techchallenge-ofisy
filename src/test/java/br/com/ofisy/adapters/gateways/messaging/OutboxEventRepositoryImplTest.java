package br.com.ofisy.adapters.gateways.messaging;

import br.com.ofisy.domain.messaging.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxEventRepositoryImplTest {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock
    private JpaOutboxEventRepository jpa;

    @Captor
    private ArgumentCaptor<OutboxEventEntity> entityCaptor;

    private OutboxEventRepositoryImpl repository;

    record ServiceOrderCancelledPayload(UUID quoteId) {}

    @BeforeEach
    void setUp() {
        repository = new OutboxEventRepositoryImpl(jpa, jsonMapper);
    }

    @Test
    void shouldSaveOutboxEventWithPayloadSerializedAsJson() {
        UUID quoteId = UUID.randomUUID();
        OutboxEvent outboxEvent = OutboxEvent.create("ServiceOrderCancelled", UUID.randomUUID(), new ServiceOrderCancelledPayload(quoteId));

        repository.save(outboxEvent);

        verify(jpa).save(entityCaptor.capture());
        OutboxEventEntity entity = entityCaptor.getValue();
        assertEquals(outboxEvent.getEventId(), entity.getEventId());
        assertEquals("{\"quoteId\":\"%s\"}".formatted(quoteId), entity.getPayload());
    }
}