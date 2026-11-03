package br.com.ofisy.adapters.listeners;

import br.com.ofisy.adapters.listeners.dto.EventEnvelope;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderEventsListenerTest {

    @Mock
    private EventHandler executionStartedHandler;

    @Mock
    private EventHandler otherHandler;

    private ServiceOrderEventsListener listener;

    @BeforeEach
    void setUp() {
        lenient().when(executionStartedHandler.type()).thenReturn("ExecutionStarted");
        lenient().when(otherHandler.type()).thenReturn("AllExecutionsCompleted");
        listener = new ServiceOrderEventsListener(List.of(executionStartedHandler, otherHandler));
    }

    private EventEnvelope envelope(String type) {
        return new EventEnvelope(UUID.randomUUID(), type, UUID.randomUUID(), Instant.parse("2026-10-13T10:00:00Z"), null);
    }

    @Nested
    class Constructor {

        @Test
        void shouldFailWhenTwoHandlersDeclareTheSameType() {
            when(otherHandler.type()).thenReturn("ExecutionStarted");

            assertThatThrownBy(() -> new ServiceOrderEventsListener(List.of(executionStartedHandler, otherHandler)))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldAcceptNoHandlers() {
            ServiceOrderEventsListener emptyListener = new ServiceOrderEventsListener(List.of());

            assertThatCode(() -> emptyListener.onMessage(envelope("ExecutionStarted"))).doesNotThrowAnyException();
        }
    }

    @Nested
    class OnMessage {

        @Test
        void shouldDispatchEventToHandlerOfItsType() {
            EventEnvelope envelope = envelope("ExecutionStarted");

            listener.onMessage(envelope);

            verify(executionStartedHandler).handle(envelope);
            verify(otherHandler, never()).handle(any());
        }

        @Test
        void shouldIgnoreEventWithoutHandler() {
            listener.onMessage(envelope("QuoteRejected"));

            verify(executionStartedHandler, never()).handle(any());
            verify(otherHandler, never()).handle(any());
        }

        @Test
        void shouldPropagateHandlerFailure() {
            EventEnvelope envelope = envelope("ExecutionStarted");
            doThrow(new IllegalStateException("boom")).when(executionStartedHandler).handle(envelope);

            assertThatThrownBy(() -> listener.onMessage(envelope))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("boom");
        }
    }
}