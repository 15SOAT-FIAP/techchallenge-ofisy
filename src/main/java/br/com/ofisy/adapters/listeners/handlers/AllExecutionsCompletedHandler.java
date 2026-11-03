package br.com.ofisy.adapters.listeners.handlers;

import br.com.ofisy.adapters.listeners.EventHandler;
import br.com.ofisy.adapters.listeners.dto.EventEnvelope;
import br.com.ofisy.application.serviceorder.finish.FinishServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.finish.FinishServiceOrderUseCase.FinishServiceOrderCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AllExecutionsCompletedHandler implements EventHandler {

    public static final String TYPE = "AllExecutionsCompleted";

    private final FinishServiceOrderUseCase finishServiceOrderUseCase;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void handle(EventEnvelope envelope) {
        finishServiceOrderUseCase.execute(
                new FinishServiceOrderCommand(envelope.eventId(), envelope.type(), envelope.serviceOrderId()));
        log.info("AllExecutionsCompletedHandler.handle >> event {} handled for service order {}",
                envelope.eventId(), envelope.serviceOrderId());
    }
}