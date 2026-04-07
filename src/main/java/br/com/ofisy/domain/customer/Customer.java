package br.com.ofisy.domain.customer;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "cpf_cnpj", nullable = false, unique = true))
    private CpfCnpj cpfCnpj;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String phone;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    private Customer(CpfCnpj cpfCnpj, String name, String email, String phone) {
        this.cpfCnpj = cpfCnpj;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    @PrePersist
    private void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public static Customer create(CpfCnpj cpfCnpj, String name, String email, String phone) {
        return new Customer(cpfCnpj, name, email, phone);
    }
}
