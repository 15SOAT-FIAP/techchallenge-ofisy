package br.com.ofisy.interfaces.api.customer;

import br.com.ofisy.application.customer.dto.CustomerRequestDTO;
import br.com.ofisy.application.customer.dto.CustomerResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.UUID;

@Tag(name = "API de Clientes")
public interface CustomerApi {

    @Operation(summary = "Listar clientes com paginação")
    @ApiResponse(responseCode = "200", description = "Clientes listados com sucesso")
    @GetMapping
    ResponseEntity<Page<CustomerResponseDTO>> getAllCustomers(Pageable pageable);

    @Operation(summary = "Obter cliente por ID")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @GetMapping("/{id}")
    ResponseEntity<CustomerResponseDTO> getCustomerById(UUID id);

    @Operation(summary = "Obter cliente por CPF/CNPJ")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @GetMapping(params = "cpfCnpj")
    ResponseEntity<CustomerResponseDTO> getCustomerByCpfCnpj(String cpfCnpj);

    @Operation(summary = "Registrar um novo cliente")
    @ApiResponse(responseCode = "201", description = "Cliente registrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de cliente inválidos")
    @PostMapping
    ResponseEntity<CustomerResponseDTO> registerCustomer(CustomerRequestDTO request);
}
