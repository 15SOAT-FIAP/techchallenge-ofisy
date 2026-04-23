package br.com.ofisy.domain.service.exceptions;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(String id) {
        super("Serviço do catálogo não encontrado com ID: " + id);
    }
}

