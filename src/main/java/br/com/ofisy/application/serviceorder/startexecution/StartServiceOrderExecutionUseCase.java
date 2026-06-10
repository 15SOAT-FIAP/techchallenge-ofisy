package br.com.ofisy.application.serviceorder.startexecution;

import java.util.UUID;

public interface StartServiceOrderExecutionUseCase {

    void execute(UUID id);
}