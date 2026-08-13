package br.com.ofisy.adapters.gateways.customer;

import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomerMapper {

    public static Customer toDomain(CustomerEntity entity) {
        return Customer.reconstruct(
                entity.getId(),
                new CpfCnpj(entity.getCpfCnpj()),
                entity.getName(),
                entity.getEmail(),
                entity.getPhone(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static CustomerEntity toEntity(Customer customer) {
        return CustomerEntity.builder()
                .id(customer.getId())
                .cpfCnpj(customer.getCpfCnpj().getValue())
                .name(customer.getName())
                .email(customer.getEmail())
                .phone(customer.getPhone())
                .active(customer.isActive())
                .createdAt(customer.getCreatedAt())
                .updatedAt(customer.getUpdatedAt())
                .build();
    }
}
