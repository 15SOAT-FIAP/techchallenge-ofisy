package br.com.ofisy.adapters.listeners;

import br.com.ofisy.adapters.listeners.dto.EventEnvelope;
import br.com.ofisy.adapters.tracing.MessageTraceContextReader;
import br.com.ofisy.adapters.tracing.TraceContextPropagator;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ServiceOrderEventsListener {

    private static final String CONSUME_SPAN_NAME = "event.consume";

    private final Map<String, EventHandler> handlers;
    private final MessageTraceContextReader traceContextReader;
    private final TraceContextPropagator traceContextPropagator;

    public ServiceOrderEventsListener(List<EventHandler> handlers,
                                      MessageTraceContextReader traceContextReader,
                                      TraceContextPropagator traceContextPropagator) {
        this.handlers = handlers.stream()
                .collect(Collectors.toMap(EventHandler::type, Function.identity()));
        this.traceContextReader = traceContextReader;
        this.traceContextPropagator = traceContextPropagator;
    }

    @SqsListener("${ofisy.messaging.queues.service-order}")
    public void onMessage(EventEnvelope envelope,
                          @Header(name = MessageTraceContextReader.TRACE_CONTEXT_ATTRIBUTE, required = false) Object traceContext) {
        // Continua o trace de quem publicou o evento, para o fluxo da OS seguir no mesmo trace entre os microsserviços
        traceContextPropagator.runAsConsumer(traceContextReader.read(traceContext), CONSUME_SPAN_NAME, spanAttributes(envelope),
                () -> dispatch(envelope));
    }

    private void dispatch(EventEnvelope envelope) {
        EventHandler handler = handlers.get(envelope.type());
        if (handler == null) {
            log.warn("ServiceOrderEventsListener.onMessage >> ignoring event {} of type {}", envelope.eventId(), envelope.type());
            return;
        }

        handler.handle(envelope);
    }

    private Map<String, String> spanAttributes(EventEnvelope envelope) {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("ofisy.event.id", String.valueOf(envelope.eventId()));
        attributes.put("ofisy.event.type", String.valueOf(envelope.type()));
        if (envelope.serviceOrderId() != null) {
            attributes.put("ofisy.service_order.id", envelope.serviceOrderId().toString());
        }
        return attributes;
    }
}
