package br.com.ofisy.application.servicecatalog.exceptions;

public class ServiceCatalogNotFoundException extends RuntimeException {
    public ServiceCatalogNotFoundException(String message) {
        super(message);
    }

    public static ServiceCatalogNotFoundException ofId(String id) {
        return new ServiceCatalogNotFoundException("Serviço não encontrado com ID: " + id);
    }

    public static ServiceCatalogNotFoundException ofName(String name) {
        return new ServiceCatalogNotFoundException("Serviço não encontrado com nome: " + name);
    }
}
