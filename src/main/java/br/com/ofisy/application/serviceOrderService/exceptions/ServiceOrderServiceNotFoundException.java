package br.com.ofisy.application.serviceOrderService.exceptions;

public class ServiceOrderServiceNotFoundException extends RuntimeException {
    public ServiceOrderServiceNotFoundException(String id) {
        super("Serviço não encontrado com ID: " + id);
    }
}

