package br.com.ofisy.adapters.listeners;

import br.com.ofisy.adapters.listeners.dto.EventEnvelope;
import br.com.ofisy.adapters.tracing.MessageTraceContextReader;
import br.com.ofisy.adapters.tracing.TraceContextPropagator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceOrderEventsListenerTest {

    private static final String TRACEPARENT = "00-6af79dd30000000039a72b7697c5509c-63cc4ef521ebfd0c-01";

    @Mock
    private EventHandler executionStartedHandler;

    @Mock
    private EventHandler otherHandler;

    @Mock
    private MessageTraceContextReader traceContextReader;

    @Mock
    private TraceContextPropagator traceContextPropagator;

    @Captor
    private ArgumentCaptor<Map<String, String>> spanAttributesCaptor;

    private ServiceOrderEventsListener listener;

    @BeforeEach
    void setUp() {
        lenient().when(executionStartedHandler.type()).thenReturn("ExecutionStarted");
        lenient().when(otherHandler.type()).thenReturn("AllExecutionsCompleted");
        lenient().when(traceContextReader.read(any())).thenReturn(Map.of());
        lenient().doAnswer(invocation -> {
            invocation.getArgument(3, Runnable.class).run();
            return null;
        }).when(traceContextPropagator).runAsConsumer(any(), any(), any(), any());
        listener = new ServiceOrderEventsListener(List.of(executionStartedHandler, otherHandler), traceContextReader, traceContextPropagator);
    }

    private EventEnvelope envelope(String type) {
        return new EventEnvelope(UUID.randomUUID(), type, UUID.randomUUID(), Instant.parse("2026-10-13T10:00:00Z"), null);
    }

    @Nested
    class Constructor {

        @Test
        void shouldFailWhenTwoHandlersDeclareTheSameType() {
            when(otherHandler.type()).thenReturn("ExecutionStarted");

            assertThatThrownBy(() -> new ServiceOrderEventsListener(List.of(executionStartedHandler, otherHandler), traceContextReader,
                    traceContextPropagator))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        void shouldAcceptNoHandlers() {
            ServiceOrderEventsListener emptyListener = new ServiceOrderEventsListener(List.of(), traceContextReader, traceContextPropagator);

            assertThatCode(() -> emptyListener.onMessage(envelope("ExecutionStarted"), null)).doesNotThrowAnyException();
        }
    }

    @Nested
    class OnMessage {

        @Test
        void shouldDispatchEventToHandlerOfItsType() {
            EventEnvelope envelope = envelope("ExecutionStarted");

            listener.onMessage(envelope, null);

            verify(executionStartedHandler).handle(envelope);
            verify(otherHandler, never()).handle(any());
        }

        @Test
        void shouldIgnoreEventWithoutHandler() {
            listener.onMessage(envelope("QuoteRejected"), null);

            verify(executionStartedHandler, never()).handle(any());
            verify(otherHandler, never()).handle(any());
        }

        @Test
        void shouldPropagateHandlerFailure() {
            EventEnvelope envelope = envelope("ExecutionStarted");
            doThrow(new IllegalStateException("boom")).when(executionStartedHandler).handle(envelope);

            assertThatThrownBy(() -> listener.onMessage(envelope, null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessage("boom");
        }

        @Test
        void shouldHandleEventInsideTraceContextOfMessage() {
            EventEnvelope envelope = envelope("ExecutionStarted");
            Object traceAttribute = "{\"traceparent\":\"%s\"}".formatted(TRACEPARENT);
            Map<String, String> traceContext = Map.of("traceparent", TRACEPARENT);
            when(traceContextReader.read(traceAttribute)).thenReturn(traceContext);

            listener.onMessage(envelope, traceAttribute);

            verify(traceContextPropagator).runAsConsumer(eq(traceContext), eq("event.consume"), spanAttributesCaptor.capture(), any());
            assertThat(spanAttributesCaptor.getValue())
                    .containsEntry("ofisy.event.id", envelope.eventId().toString())
                    .containsEntry("ofisy.event.type", "ExecutionStarted")
                    .containsEntry("ofisy.service_order.id", envelope.serviceOrderId().toString());
            verify(executionStartedHandler).handle(envelope);
        }

        @Test
        void shouldNotTagServiceOrderOnSpanOfEventWithoutServiceOrder() {
            EventEnvelope envelope = new EventEnvelope(UUID.randomUUID(), "ExecutionStarted", null, Instant.now(), null);

            listener.onMessage(envelope, null);

            verify(traceContextPropagator).runAsConsumer(any(), any(), spanAttributesCaptor.capture(), any());
            assertThat(spanAttributesCaptor.getValue()).doesNotContainKey("ofisy.service_order.id");
        }
    }
}
