package br.com.ofisy.adapters.tracing;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.context.propagation.TextMapPropagator;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

// Guarda e restaura o contexto do trace entre a requisicao que grava o evento no outbox e o job que publica depois.
// Com o dd-java-agent e DD_TRACE_OTEL_ENABLED=true a API do OpenTelemetry enxerga os spans do Datadog;
// sem o agent (local e testes) ela é no-op: nada é capturado e a publicação segue sem span.
@Component
public class TraceContextPropagator {

    private static final String INSTRUMENTATION_NAME = "ofisy-api.outbox";

    private static final TextMapGetter<Map<String, String>> MAP_GETTER = new TextMapGetter<>() {
        @Override
        public Iterable<String> keys(Map<String, String> carrier) {
            return carrier.keySet();
        }

        @Override
        public String get(Map<String, String> carrier, String key) {
            return carrier == null ? null : carrier.get(key);
        }
    };

    public Map<String, String> captureCurrent() {
        Map<String, String> carrier = new HashMap<>();
        propagator().inject(Context.current(), carrier, Map::put);
        return carrier;
    }

    // Publicação do outbox: o span de envio fica como filho do trace que gerou o evento
    public void runWithParent(Map<String, String> parentContext, String spanName, Map<String, String> attributes, Runnable action) {
        runInSpan(parentContext, spanName, SpanKind.PRODUCER, attributes, action);
    }

    // Consumo da fila: o processamento continua o trace de quem publicou a mensagem
    public void runAsConsumer(Map<String, String> parentContext, String spanName, Map<String, String> attributes, Runnable action) {
        runInSpan(parentContext, spanName, SpanKind.CONSUMER, attributes, action);
    }

    private void runInSpan(Map<String, String> parentContext, String spanName, SpanKind kind, Map<String, String> attributes,
                           Runnable action) {
        if (parentContext == null || parentContext.isEmpty()) {
            action.run();
            return;
        }

        Context parent = propagator().extract(Context.root(), parentContext, MAP_GETTER);
        SpanBuilder builder = GlobalOpenTelemetry.getTracer(INSTRUMENTATION_NAME)
                .spanBuilder(spanName)
                .setParent(parent)
                .setSpanKind(kind);
        attributes.forEach(builder::setAttribute);
        Span span = builder.startSpan();

        try (Scope ignored = span.makeCurrent()) {
            action.run();
        } catch (RuntimeException e) {
            span.recordException(e);
            throw e;
        } finally {
            span.end();
        }
    }

    private TextMapPropagator propagator() {
        return GlobalOpenTelemetry.getPropagators().getTextMapPropagator();
    }
}
