package br.com.ofisy.adapters.tracing;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Sem o dd-java-agent a API do OpenTelemetry é no-op: o comportamento esperado é não capturar nada e nunca impedir a ação
class TraceContextPropagatorTest {

    private static final Map<String, String> PARENT_CONTEXT =
            Map.of("traceparent", "00-6af793d30000000025cb7731d653ab37-6d0c2e20f86d8721-01");

    private final TraceContextPropagator propagator = new TraceContextPropagator();

    @Test
    void shouldCaptureNothingWhenThereIsNoTracer() {
        assertThat(propagator.captureCurrent()).isEmpty();
    }

    @Test
    void shouldRunActionWhenThereIsNoParentContext() {
        AtomicBoolean ran = new AtomicBoolean();

        propagator.runWithParent(Map.of(), "outbox.publish", Map.of(), () -> ran.set(true));

        assertThat(ran).isTrue();
    }

    @Test
    void shouldRunActionInsideParentContext() {
        AtomicBoolean ran = new AtomicBoolean();

        propagator.runWithParent(PARENT_CONTEXT, "outbox.publish", Map.of("ofisy.event.type", "ServiceOrderCancelled"), () -> ran.set(true));

        assertThat(ran).isTrue();
    }

    @Test
    void shouldRunConsumerActionInsideParentContext() {
        AtomicBoolean ran = new AtomicBoolean();

        propagator.runAsConsumer(PARENT_CONTEXT, "event.consume", Map.of("ofisy.event.type", "ExecutionStarted"), () -> ran.set(true));

        assertThat(ran).isTrue();
    }

    @Test
    void shouldRunConsumerActionWhenThereIsNoParentContext() {
        AtomicBoolean ran = new AtomicBoolean();

        propagator.runAsConsumer(Map.of(), "event.consume", Map.of(), () -> ran.set(true));

        assertThat(ran).isTrue();
    }

    @Test
    void shouldPropagateFailureOfAction() {
        RuntimeException failure = new RuntimeException("SNS unavailable");

        assertThatThrownBy(() -> propagator.runWithParent(PARENT_CONTEXT, "outbox.publish", Map.of(), () -> {
            throw failure;
        })).isSameAs(failure);
    }
}
