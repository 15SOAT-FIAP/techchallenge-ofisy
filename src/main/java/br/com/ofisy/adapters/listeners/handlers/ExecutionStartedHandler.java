package br.com.ofisy.adapters.listeners.handlers;

import br.com.ofisy.adapters.listeners.EventHandler;
import br.com.ofisy.adapters.listeners.dto.EventEnvelope;
import br.com.ofisy.application.serviceorder.startexecution.StartServiceOrderExecutionUseCase;
import br.com.ofisy.application.serviceorder.startexecution.StartServiceOrderExecutionUseCase.StartServiceOrderExecutionCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExecutionStartedHandler implements EventHandler {

    public static final String TYPE = "ExecutionStarted";

    private final StartServiceOrderExecutionUseCase startServiceOrderExecutionUseCase;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void handle(EventEnvelope envelope) {
        startServiceOrderExecutionUseCase.execute(
                new StartServiceOrderExecutionCommand(envelope.eventId(), envelope.type(), envelope.serviceOrderId()));
        log.info("ExecutionStartedHandler.handle >> event {} handled for service order {}",
                envelope.eventId(), envelope.serviceOrderId());
    }
}