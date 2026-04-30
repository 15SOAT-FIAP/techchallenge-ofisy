package br.com.ofisy.application.serviceorderexecution.exceptions;

import java.util.UUID;

public class ServiceOrderExecutionNotFoundException extends RuntimeException {
    public ServiceOrderExecutionNotFoundException(UUID id) {
        super("Execução de serviço não encontrado com ID: " + id);
    }
}

