package br.com.ofisy.application.customer;

import br.com.ofisy.application.customer.dto.CustomerRequestDTO;
import br.com.ofisy.application.customer.dto.CustomerResponseDTO;
import br.com.ofisy.application.customer.exceptions.CustomerCpfCnpjNotFoundException;
import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    // Essa injeçao de dependencia vai ficar com erro ate implementar o repository, mas nao tem problema
    private final CustomerRepository customerRepository;

    public void registerCustomer(CustomerRequestDTO request) {
        var customer = CustomerMapper.toDomain(request);
        customerRepository.save(customer);
    }

    public Page<CustomerResponseDTO> listRegisteredCustomers(Pageable pageable) {
        var customers = customerRepository.findAll(pageable);
        return customers.map(CustomerMapper::toDTO);
    }

    public CustomerResponseDTO identifyCustomerById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        var customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        return CustomerMapper.toDTO(customer);
    }

    public CustomerResponseDTO identifyCustomerByCpfCnpj(String cpfCnpj) {
        var customer = customerRepository.findByCpfCnpj(new CpfCnpj(cpfCnpj))
                .orElseThrow(() -> new CustomerCpfCnpjNotFoundException(cpfCnpj));

        return CustomerMapper.toDTO(customer);
    }
}
