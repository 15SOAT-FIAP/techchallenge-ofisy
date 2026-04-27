package br.com.ofisy.application.serviceorder.exceptions;

import java.util.UUID;

public class ServiceOrderNotFoundException extends RuntimeException {
    public ServiceOrderNotFoundException(UUID id) {
        super("Ordem de serviço com ID " + id + " não encontrada.");
    }
}
