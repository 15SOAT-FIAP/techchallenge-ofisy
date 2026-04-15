package br.com.ofisy.application.customer;

import br.com.ofisy.application.customer.dto.CustomerRequestDTO;
import br.com.ofisy.application.customer.dto.CustomerResponseDTO;
import br.com.ofisy.application.customer.exceptions.CustomerAlreadyExistsException;
import br.com.ofisy.application.customer.exceptions.CustomerCpfCnpjNotFoundException;
import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponseDTO registerCustomer(CustomerRequestDTO request) {
        var customer = CustomerMapper.toDomain(request);
        if (customerRepository.findByCpfCnpj(customer.getCpfCnpj()).isPresent()) {
            throw new CustomerAlreadyExistsException(customer.getCpfCnpj().getValue());
        }
        var savedCustomer = customerRepository.save(customer);

        return CustomerMapper.toDTO(savedCustomer);
    }

    @Transactional(readOnly = true)
    public Page<CustomerResponseDTO> listRegisteredCustomers(Pageable pageable) {
        var customers = customerRepository.findAll(pageable);
        return customers.map(CustomerMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDTO identifyCustomerById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        var customer = customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException(id));

        return CustomerMapper.toDTO(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDTO identifyCustomerByCpfCnpj(String cpfCnpj) {
        var customer = customerRepository.findByCpfCnpj(new CpfCnpj(cpfCnpj))
                .orElseThrow(() -> new CustomerCpfCnpjNotFoundException(cpfCnpj));

        return CustomerMapper.toDTO(customer);
    }
}
