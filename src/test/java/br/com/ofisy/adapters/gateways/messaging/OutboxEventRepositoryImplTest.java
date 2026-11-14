package br.com.ofisy.adapters.gateways.messaging;

import br.com.ofisy.adapters.tracing.TraceContextPropagator;
import br.com.ofisy.domain.messaging.OutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboxEventRepositoryImplTest {

    private static final String TRACEPARENT = "00-6af793d30000000025cb7731d653ab37-6d0c2e20f86d8721-01";

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Mock
    private JpaOutboxEventRepository jpa;

    @Mock
    private TraceContextPropagator traceContextPropagator;

    @Captor
    private ArgumentCaptor<OutboxEventEntity> entityCaptor;

    private OutboxEventRepositoryImpl repository;

    record ServiceOrderCancelledPayload(UUID quoteId) {}

    @BeforeEach
    void setUp() {
        repository = new OutboxEventRepositoryImpl(jpa, jsonMapper, traceContextPropagator);
    }

    @Test
    void shouldSaveOutboxEventWithPayloadSerializedAsJson() {
        UUID quoteId = UUID.randomUUID();
        OutboxEvent outboxEvent = OutboxEvent.create("ServiceOrderCancelled", UUID.randomUUID(), new ServiceOrderCancelledPayload(quoteId));
        when(traceContextPropagator.captureCurrent()).thenReturn(Map.of());

        repository.save(outboxEvent);

        verify(jpa).save(entityCaptor.capture());
        OutboxEventEntity entity = entityCaptor.getValue();
        assertEquals(outboxEvent.getEventId(), entity.getEventId());
        assertEquals("{\"quoteId\":\"%s\"}".formatted(quoteId), entity.getPayload());
    }

    @Test
    void shouldSaveCurrentTraceContextSerializedAsJson() {
        OutboxEvent outboxEvent = OutboxEvent.create("ServiceOrderCancelled", UUID.randomUUID(), Map.of());
        when(traceContextPropagator.captureCurrent()).thenReturn(Map.of("traceparent", TRACEPARENT));

        repository.save(outboxEvent);

        verify(jpa).save(entityCaptor.capture());
        assertEquals("{\"traceparent\":\"%s\"}".formatted(TRACEPARENT), entityCaptor.getValue().getTraceContext());
    }

    @Test
    void shouldSaveWithoutTraceContextWhenThereIsNoActiveTrace() {
        OutboxEvent outboxEvent = OutboxEvent.create("ServiceOrderCancelled", UUID.randomUUID(), Map.of());
        when(traceContextPropagator.captureCurrent()).thenReturn(Map.of());

        repository.save(outboxEvent);

        verify(jpa).save(entityCaptor.capture());
        assertNull(entityCaptor.getValue().getTraceContext());
    }
}
