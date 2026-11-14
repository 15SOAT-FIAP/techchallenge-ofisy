package br.com.ofisy.application.serviceorder.approve;

import java.util.UUID;

public interface ApproveServiceOrderUseCase {

    void execute(ApproveServiceOrderCommand cmd);

    record ApproveServiceOrderCommand(
            UUID eventId,
            String eventType,
            UUID serviceOrderId
    ) {}
}
