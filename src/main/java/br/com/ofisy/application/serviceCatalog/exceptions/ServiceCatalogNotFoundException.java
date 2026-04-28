package br.com.ofisy.application.serviceCatalog.exceptions;

public class ServiceCatalogNotFoundException extends RuntimeException {
    public ServiceCatalogNotFoundException(String id) {
        super("Serviço não encontrado com ID: " + id);
    }
}
