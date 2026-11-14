package br.com.ofisy.adapters.listeners.handlers;

import br.com.ofisy.adapters.listeners.EventHandler;
import br.com.ofisy.adapters.listeners.dto.EventEnvelope;
import br.com.ofisy.application.serviceorder.approve.ApproveServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.approve.ApproveServiceOrderUseCase.ApproveServiceOrderCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteApprovedHandler implements EventHandler {

    public static final String TYPE = "QuoteApproved";

    private final ApproveServiceOrderUseCase approveServiceOrderUseCase;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void handle(EventEnvelope envelope) {
        approveServiceOrderUseCase.execute(
                new ApproveServiceOrderCommand(envelope.eventId(), envelope.type(), envelope.serviceOrderId()));
        log.info("QuoteApprovedHandler.handle >> event {} handled for service order {}",
                envelope.eventId(), envelope.serviceOrderId());
    }
}
