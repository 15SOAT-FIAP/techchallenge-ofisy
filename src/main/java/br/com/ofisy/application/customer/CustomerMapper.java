package br.com.ofisy.application.customer;

import br.com.ofisy.application.customer.dto.CustomerRequestDTO;
import br.com.ofisy.application.customer.dto.CustomerResponseDTO;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;

public class CustomerMapper {

    private CustomerMapper() {
        /* This utility class should not be instantiated */
    }


    public static Customer toDomain(CustomerRequestDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("CustomerRequestDTO não pode ser nulo");
        }
        return Customer.create(
                new CpfCnpj(request.cpfCnpj()),
                request.name(),
                request.email(),
                request.phone()
        );
    }

    public static CustomerResponseDTO toDTO(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Cliente não pode ser nulo");
        }
        if (customer.getCpfCnpj() == null) {
            throw new IllegalArgumentException("CpfCnpj do cliente não pode ser nulo");
        }
        return new CustomerResponseDTO(
                customer.getId(),
                customer.getCpfCnpj().getValue(),
                customer.getName(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
