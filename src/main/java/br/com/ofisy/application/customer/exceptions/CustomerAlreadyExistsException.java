package br.com.ofisy.application.customer.exceptions;

public class CustomerAlreadyExistsException extends RuntimeException {
    public CustomerAlreadyExistsException(String cpfCnpj) {
        super("Cliente com CPF/CNPJ " + cpfCnpj + " já existe.");
    }
}
