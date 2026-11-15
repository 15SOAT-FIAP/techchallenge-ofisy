package br.com.ofisy.application.serviceorder.confirmpayment;

import java.util.UUID;

public interface ConfirmServiceOrderPaymentUseCase {

    void execute(ConfirmServiceOrderPaymentCommand cmd);

    record ConfirmServiceOrderPaymentCommand(
            UUID eventId,
            String eventType,
            UUID serviceOrderId
    ) {}
}