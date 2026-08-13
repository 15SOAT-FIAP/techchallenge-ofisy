package br.com.ofisy.adapters.controllers.customer;

import br.com.ofisy.adapters.controllers.customer.dto.CustomerRequestDTO;
import br.com.ofisy.adapters.controllers.customer.dto.CustomerResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Tag(name = "API de Clientes")
public interface CustomerApi {

    @Operation(summary = "Listar clientes com paginação ou por CPF/CNPJ - Roles autorizadas: (ADMIN, ATTENDANT)")
    @ApiResponse(responseCode = "200", description = "Clientes listados com sucesso")
    @GetMapping
    ResponseEntity<Page<CustomerResponseDTO>> getAllCustomers(Pageable pageable);

    @Operation(summary = "Obter cliente por ID - Roles autorizadas: (ADMIN, ATTENDANT)")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @GetMapping("/{id}")
    ResponseEntity<CustomerResponseDTO> getCustomerById(@PathVariable UUID id);

    @Operation(summary = "Listar clientes com paginação ou por CPF/CNPJ - Roles autorizadas: (ADMIN, ATTENDANT)")
    @ApiResponse(responseCode = "200", description = "Cliente encontrado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @GetMapping(params = "cpfCnpj")
    ResponseEntity<CustomerResponseDTO> getCustomerByCpfCnpj(@RequestParam String cpfCnpj);

    @Operation(summary = "Registrar um novo cliente - Roles autorizadas: (ADMIN, ATTENDANT)")
    @ApiResponse(responseCode = "201", description = "Cliente registrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados de cliente inválidos")
    @PostMapping
    ResponseEntity<CustomerResponseDTO> registerCustomer(CustomerRequestDTO request);

    @Operation(summary = "Ativar cliente - Roles autorizadas: (ADMIN, ATTENDANT)")
    @ApiResponse(responseCode = "200", description = "Cliente ativado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @ApiResponse(responseCode = "409", description = "Cliente já está ativo")
    @PatchMapping("/{id}/activate")
    ResponseEntity<CustomerResponseDTO> activateCustomer(@PathVariable UUID id);

    @Operation(summary = "Desativar cliente - Roles autorizadas: (ADMIN, ATTENDANT)")
    @ApiResponse(responseCode = "200", description = "Cliente desativado com sucesso")
    @ApiResponse(responseCode = "404", description = "Cliente não encontrado")
    @ApiResponse(responseCode = "409", description = "Cliente já está desativado")
    @PatchMapping("/{id}/deactivate")
    ResponseEntity<CustomerResponseDTO> deactivateCustomer(@PathVariable UUID id);
}
