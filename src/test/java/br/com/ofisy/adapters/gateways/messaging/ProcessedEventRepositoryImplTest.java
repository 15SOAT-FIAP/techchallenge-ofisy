package br.com.ofisy.adapters.gateways.messaging;

import br.com.ofisy.domain.messaging.ProcessedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessedEventRepositoryImplTest {

    @Mock
    private JpaProcessedEventRepository jpa;

    private ProcessedEventRepositoryImpl repository;

    @BeforeEach
    void setUp() {
        repository = new ProcessedEventRepositoryImpl(jpa);
    }

    @Test
    void shouldCheckIfEventWasProcessed() {
        UUID eventId = UUID.randomUUID();
        when(jpa.existsById(eventId)).thenReturn(true);

        boolean result = repository.existsById(eventId);

        assertTrue(result);
        verify(jpa).existsById(eventId);
    }

    @Test
    void shouldSaveProcessedEvent() {
        ProcessedEvent domain = ProcessedEvent.create(UUID.randomUUID(), "ExecutionStarted");
        when(jpa.save(any(ProcessedEventEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        ProcessedEvent result = repository.save(domain);

        assertEquals(domain.getEventId(), result.getEventId());
        assertEquals(domain.getEventType(), result.getEventType());
        verify(jpa).save(any(ProcessedEventEntity.class));
    }
}