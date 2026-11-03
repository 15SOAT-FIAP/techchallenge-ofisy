package br.com.ofisy.application.serviceorder.finish;

import java.util.UUID;

public interface FinishServiceOrderUseCase {

    void execute(FinishServiceOrderCommand cmd);

    record FinishServiceOrderCommand(
            UUID eventId,
            String eventType,
            UUID serviceOrderId
    ) {}
}