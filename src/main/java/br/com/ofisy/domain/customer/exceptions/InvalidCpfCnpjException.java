package br.com.ofisy.domain.customer.exceptions;

public class InvalidCpfCnpjException extends RuntimeException {
    public InvalidCpfCnpjException(String value) {
        super("CPF ou CNPJ inválido: " + value);
    }
}
