package br.com.ofisy.interfaces.api.customer;

import br.com.ofisy.application.customer.CustomerService;
import br.com.ofisy.application.customer.dto.CustomerRequestDTO;
import br.com.ofisy.application.customer.dto.CustomerResponseDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    public ResponseEntity<Page<CustomerResponseDTO>> getAllCustomers(
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(customerService.listRegisteredCustomers(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable UUID id) {

        return ResponseEntity.ok(customerService.identifyCustomerById(id));
    }

    @GetMapping("/cpfcnpj/{cpfCnpj}")
    public ResponseEntity<CustomerResponseDTO> getCustomerByCpfCnpj(@PathVariable String cpfCnpj) {

        return ResponseEntity.ok(customerService.identifyCustomerByCpfCnpj(cpfCnpj));
    }

    @PostMapping
    public ResponseEntity<CustomerResponseDTO> registerCustomer(@Valid @RequestBody CustomerRequestDTO requestDTO) {

        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.registerCustomer(requestDTO));
    }
}
