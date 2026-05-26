package br.com.ofisy.adapters.controllers.customer;

import br.com.ofisy.adapters.controllers.customer.dto.CustomerRequestDTO;
import br.com.ofisy.adapters.controllers.customer.dto.CustomerResponseDTO;
import br.com.ofisy.adapters.presenter.customer.CustomerPresenter;
import br.com.ofisy.application.customer.identifybycpfcnpj.IdentifyByCpfCnpjCustomerUseCase;
import br.com.ofisy.application.customer.identifybyid.IdentifyByIdCustomerUseCase;
import br.com.ofisy.application.customer.list.ListRegisteredCustomerUseCase;
import br.com.ofisy.application.customer.register.RegisterCustomerUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController implements CustomerApi {

    private final RegisterCustomerUseCase registerCustomerUseCase;
    private final ListRegisteredCustomerUseCase listRegisteredCustomerUseCase;
    private final IdentifyByIdCustomerUseCase identifyByIdCustomerUseCase;
    private final IdentifyByCpfCnpjCustomerUseCase identifyByCpfCnpjCustomerUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<Page<CustomerResponseDTO>> getAllCustomers(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(listRegisteredCustomerUseCase.execute(pageable).map(CustomerPresenter::present));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable UUID id) {

        return ResponseEntity.ok(CustomerPresenter.present(identifyByIdCustomerUseCase.execute(id)));
    }

    @GetMapping(params = "cpfCnpj")
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<CustomerResponseDTO> getCustomerByCpfCnpj(
            @RequestParam(required = false) String cpfCnpj) {

        return ResponseEntity.ok(CustomerPresenter.present(identifyByCpfCnpjCustomerUseCase.execute(cpfCnpj)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ATTENDANT')")
    public ResponseEntity<CustomerResponseDTO> registerCustomer(@Valid @RequestBody CustomerRequestDTO requestDTO) {

        RegisterCustomerUseCase.RegisterCustomerCommand cmd = new RegisterCustomerUseCase.RegisterCustomerCommand(
                requestDTO.cpfCnpj(), requestDTO.name(), requestDTO.email(), requestDTO.phone());
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerPresenter.present(registerCustomerUseCase.execute(cmd)));
    }
}
