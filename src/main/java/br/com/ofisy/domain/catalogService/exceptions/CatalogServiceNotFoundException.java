package br.com.ofisy.domain.catalogService.exceptions;

public class CatalogServiceNotFoundException extends RuntimeException {
    public CatalogServiceNotFoundException(String id) {
        super("Serviço do catálogo não encontrado com ID: " + id);
    }
}

