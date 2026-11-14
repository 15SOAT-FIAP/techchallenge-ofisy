package br.com.ofisy.adapters.listeners.handlers;

import br.com.ofisy.adapters.listeners.EventHandler;
import br.com.ofisy.adapters.listeners.dto.EventEnvelope;
import br.com.ofisy.application.serviceorder.awaitapproval.AwaitServiceOrderApprovalUseCase;
import br.com.ofisy.application.serviceorder.awaitapproval.AwaitServiceOrderApprovalUseCase.AwaitServiceOrderApprovalCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class QuoteSentForApprovalHandler implements EventHandler {

    public static final String TYPE = "QuoteSentForApproval";

    private final AwaitServiceOrderApprovalUseCase awaitServiceOrderApprovalUseCase;

    @Override
    public String type() {
        return TYPE;
    }

    @Override
    public void handle(EventEnvelope envelope) {
        awaitServiceOrderApprovalUseCase.execute(
                new AwaitServiceOrderApprovalCommand(envelope.eventId(), envelope.type(), envelope.serviceOrderId()));
        log.info("QuoteSentForApprovalHandler.handle >> event {} handled for service order {}",
                envelope.eventId(), envelope.serviceOrderId());
    }
}
