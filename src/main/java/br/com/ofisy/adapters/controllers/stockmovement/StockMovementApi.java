package br.com.ofisy.adapters.controllers.stockmovement;

import br.com.ofisy.adapters.controllers.stockmovement.dto.StockMovementResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.UUID;

@Tag(name = "API de Movimentação de estoque")
public interface StockMovementApi {

    @Operation(summary = "Listar todas as movimentações de estoque com paginação - Roles autorizadas: (ADMIN, STOCKMAN)")
    @ApiResponse(responseCode = "200", description = "Movimentações de estoque listadas com sucesso - sem stockId")
    @GetMapping
    ResponseEntity<Page<StockMovementResponseDTO>> getAllStockMovements(Pageable pageable);

    @Operation(summary = "Listar movimentações de estoque específico - Roles autorizadas: (ADMIN, STOCKMAN)")
    @ApiResponse(responseCode = "200", description = "Movimentações de estoque listadas com sucesso - stockId")
    @ApiResponse(responseCode = "404", description = "Movimentações de estoque não encontradas")
    @GetMapping(params = "stockId")
    ResponseEntity<Page<StockMovementResponseDTO>> getByStockId(UUID stockId, Pageable pageable);
}
