package br.com.ofisy.adapters.listeners.handlers;

import br.com.ofisy.adapters.listeners.EventHandler;
import br.com.ofisy.adapters.listeners.dto.EventEnvelope;
import br.com.ofisy.application.serviceorder.confirmpayment.ConfirmServiceOrderPaymentUseCase;
import br.com.ofisy.application.serviceorder.confirmpayment.ConfirmServiceOrderPaymentUseCase.ConfirmServiceOrderPaymentCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentConfirmedHandler implements EventHandler {

    public static final String TYPE = "PaymentConfirmed";

    private final ConfirmServiceOrderPaymentUseCase confirmServiceOrderPaymentUseCase;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void handle(EventEnvelope envelope) {
        confirmServiceOrderPaymentUseCase.execute(
                new ConfirmServiceOrderPaymentCommand(envelope.eventId(), envelope.type(), envelope.serviceOrderId()));
        log.info("PaymentConfirmedHandler.handle >> event {} handled for service order {}",
                envelope.eventId(), envelope.serviceOrderId());
    }
}