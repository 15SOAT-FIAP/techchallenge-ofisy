package br.com.ofisy.domain.customer;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
public class Customer {

    private UUID id;
    private CpfCnpj cpfCnpj;
    private String name;
    private String email;
    private String phone;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private Customer(CpfCnpj cpfCnpj, String name, String email, String phone) {
        this.cpfCnpj = cpfCnpj;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public static Customer create(CpfCnpj cpfCnpj, String name, String email, String phone) {
        Customer customer = new Customer(cpfCnpj, name, email, phone);
        customer.createdAt = LocalDateTime.now();
        customer.updatedAt = LocalDateTime.now();
        return customer;
    }

    public static Customer reconstruct(UUID id, CpfCnpj cpfCnpj, String name,
                                       String email, String phone, LocalDateTime createdAt,
                                       LocalDateTime updatedAt) {
        Customer customer = new Customer(cpfCnpj, name, email, phone);
        customer.id = id;
        customer.createdAt = createdAt;
        customer.updatedAt = updatedAt;
        return customer;
    }
}
