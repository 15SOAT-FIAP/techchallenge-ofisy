package br.com.ofisy.application.serviceorder.awaitapproval;

import java.util.UUID;

public interface AwaitServiceOrderApprovalUseCase {

    void execute(AwaitServiceOrderApprovalCommand cmd);

    record AwaitServiceOrderApprovalCommand(
            UUID eventId,
            String eventType,
            UUID serviceOrderId
    ) {}
}
