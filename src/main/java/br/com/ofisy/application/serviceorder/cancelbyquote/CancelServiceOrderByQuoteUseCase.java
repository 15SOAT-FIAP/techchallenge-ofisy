package br.com.ofisy.application.serviceorder.cancelbyquote;

import java.util.UUID;

public interface CancelServiceOrderByQuoteUseCase {

    void execute(CancelServiceOrderByQuoteCommand cmd);

    record CancelServiceOrderByQuoteCommand(
            UUID eventId,
            String eventType,
            UUID serviceOrderId
    ) {}
}
