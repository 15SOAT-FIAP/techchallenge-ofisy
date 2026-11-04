package br.com.ofisy.application.serviceorder.startexecution;

import java.util.UUID;

public interface StartServiceOrderExecutionUseCase {

    void execute(StartServiceOrderExecutionCommand cmd);

    record StartServiceOrderExecutionCommand(
            UUID eventId,
            String eventType,
            UUID serviceOrderId
    ) {}
}