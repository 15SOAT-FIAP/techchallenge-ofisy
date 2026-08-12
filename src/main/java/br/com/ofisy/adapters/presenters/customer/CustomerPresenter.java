package br.com.ofisy.adapters.presenters.customer;

import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.adapters.controllers.customer.dto.CustomerResponseDTO;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CustomerPresenter {

    public static CustomerResponseDTO present(Customer customer) {
        return new CustomerResponseDTO(
                customer.getId(),
                customer.getCpfCnpj().getValue(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
