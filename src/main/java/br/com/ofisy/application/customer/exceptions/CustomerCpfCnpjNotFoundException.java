package br.com.ofisy.application.customer.exceptions;

public class CustomerCpfCnpjNotFoundException extends RuntimeException {
    public CustomerCpfCnpjNotFoundException(String cpfCnpj) {
        super("Cliente com CPF/CNPJ " + cpfCnpj + " não encontrado.");
    }
}
