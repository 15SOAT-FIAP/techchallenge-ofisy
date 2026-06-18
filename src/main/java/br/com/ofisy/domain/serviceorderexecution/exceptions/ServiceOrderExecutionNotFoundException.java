package br.com.ofisy.domain.serviceorderexecution.exceptions;

import java.util.UUID;

public class ServiceOrderExecutionNotFoundException extends RuntimeException {
    public ServiceOrderExecutionNotFoundException(UUID serviceExecutionId) {
      super("Execução com id " + serviceExecutionId + " não encontrada");
    }
}
