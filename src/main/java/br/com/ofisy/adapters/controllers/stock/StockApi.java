package br.com.ofisy.adapters.controllers.stock;

import br.com.ofisy.adapters.controllers.stock.dto.CreateStockRequestDTO;
import br.com.ofisy.adapters.controllers.stock.dto.StockResponseDTO;
import br.com.ofisy.adapters.controllers.stock.dto.UpdateStockRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "API de Estoque")
public interface StockApi {

    @Operation(summary = "Listar estoques com paginação - Roles autorizadas: (ADMIN, STOCKMAN, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Estoques listados com sucesso")
    @GetMapping
    ResponseEntity<Page<StockResponseDTO>> getAllStocks(Pageable pageable);

    @Operation(summary = "Obter estoque por ID - Roles autorizadas: (ADMIN, STOCKMAN, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Estoques listado com sucesso")
    @ApiResponse(responseCode = "404", description = "Estoque não encontrado")
    @GetMapping("/{id}")
    ResponseEntity<StockResponseDTO> getStockById(UUID id);

    @Operation(summary = "Obter estoque por nome do produto - Roles autorizadas: (ADMIN, STOCKMAN, MECHANIC)")
    @ApiResponse(responseCode = "200", description = "Estoque listado com sucesso")
    @ApiResponse(responseCode = "404", description = "Estoque não encontrado")
    @GetMapping("/productName/{productName}")
    ResponseEntity<StockResponseDTO> getByProductName(String productName);

    @Operation(summary = "Criar um novo estoque - Roles autorizadas: (ADMIN, STOCKMAN)")
    @ApiResponse(responseCode = "201", description = "Estoque cadastrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados para criação de estoque inválidos")
    @PostMapping
    ResponseEntity<StockResponseDTO> create(CreateStockRequestDTO request);

    @Operation(summary = "Adicionar estoque - Roles autorizadas: (ADMIN, STOCKMAN)")
    @ApiResponse(responseCode = "200", description = "Estoque adicionado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados para adicionar estoque inválidos")
    @PostMapping("/{id}/add")
    ResponseEntity<StockResponseDTO> addStock(UUID id, Integer quantity);

    @Operation(summary = "Consumir estoque - Roles autorizadas: (ADMIN, STOCKMAN)")
    @ApiResponse(responseCode = "200", description = "Estoque consumido com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados para consumir estoque inválidos")
    @PostMapping("/{id}/consume")
    ResponseEntity<StockResponseDTO> consumeStock(UUID id, Integer quantity);

    @Operation(summary = "Atualizar um estoque existente - Roles autorizadas: (ADMIN, STOCKMAN)")
    @ApiResponse(responseCode = "200", description = "Estoque atualizado com sucesso")
    @ApiResponse(responseCode = "400", description = "Dados para atualizar estoque inválidos")
    @PutMapping("/{id}")
    ResponseEntity<StockResponseDTO> update(UUID id, UpdateStockRequestDTO request);
}
