package br.com.ofisy.adapters.listeners.handlers;

import br.com.ofisy.adapters.listeners.dto.EventEnvelope;
import br.com.ofisy.application.serviceorder.finish.FinishServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.finish.FinishServiceOrderUseCase.FinishServiceOrderCommand;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AllExecutionsCompletedHandlerTest {

    private static final UUID EVENT_ID = UUID.randomUUID();
    private static final UUID SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock
    private FinishServiceOrderUseCase finishServiceOrderUseCase;

    @Captor
    private ArgumentCaptor<FinishServiceOrderCommand> commandCaptor;

    @InjectMocks
    private AllExecutionsCompletedHandler handler;

    @Test
    void shouldHandleAllExecutionsCompletedType() {
        assertThat(handler.type()).isEqualTo("AllExecutionsCompleted");
    }

    @Nested
    class Handle {

        @Test
        void shouldMapEnvelopeToFinishServiceOrderCommand() {
            EventEnvelope envelope = new EventEnvelope(EVENT_ID, AllExecutionsCompletedHandler.TYPE, SERVICE_ORDER_ID,
                    Instant.parse("2026-10-20T10:00:00Z"), JsonMapper.builder().build().readTree("{}"));

            handler.handle(envelope);

            verify(finishServiceOrderUseCase).execute(commandCaptor.capture());
            FinishServiceOrderCommand command = commandCaptor.getValue();
            assertThat(command.eventId()).isEqualTo(EVENT_ID);
            assertThat(command.eventType()).isEqualTo(AllExecutionsCompletedHandler.TYPE);
            assertThat(command.serviceOrderId()).isEqualTo(SERVICE_ORDER_ID);
        }
    }
}