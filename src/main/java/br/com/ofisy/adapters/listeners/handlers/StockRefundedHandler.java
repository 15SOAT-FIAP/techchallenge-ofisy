package br.com.ofisy.adapters.listeners.handlers;

import br.com.ofisy.adapters.listeners.EventHandler;
import br.com.ofisy.adapters.listeners.dto.EventEnvelope;
import br.com.ofisy.application.serviceorder.cancelbyquote.CancelServiceOrderByQuoteUseCase;
import br.com.ofisy.application.serviceorder.cancelbyquote.CancelServiceOrderByQuoteUseCase.CancelServiceOrderByQuoteCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockRefundedHandler implements EventHandler {

    public static final String TYPE = "StockRefunded";

    private final CancelServiceOrderByQuoteUseCase cancelServiceOrderByQuoteUseCase;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void handle(EventEnvelope envelope) {
        cancelServiceOrderByQuoteUseCase.execute(
                new CancelServiceOrderByQuoteCommand(envelope.eventId(), envelope.type(), envelope.serviceOrderId()));
        log.info("StockRefundedHandler.handle >> event {} handled for service order {}",
                envelope.eventId(), envelope.serviceOrderId());
    }
}
