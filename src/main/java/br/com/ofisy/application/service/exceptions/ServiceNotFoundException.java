package br.com.ofisy.application.service.exceptions;

public class ServiceNotFoundException extends RuntimeException {
    public ServiceNotFoundException(String id) {
        super("Serviço não encontrado com ID: " + id);
    }
}

