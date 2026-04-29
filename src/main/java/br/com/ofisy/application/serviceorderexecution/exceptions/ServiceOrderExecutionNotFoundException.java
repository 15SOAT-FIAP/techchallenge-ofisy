package br.com.ofisy.application.serviceorderexecution.exceptions;

public class ServiceOrderExecutionNotFoundException extends RuntimeException {
    public ServiceOrderExecutionNotFoundException(String id) {
        super("Serviço não encontrado com ID: " + id);
    }
}

