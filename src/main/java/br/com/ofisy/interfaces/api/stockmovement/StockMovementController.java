package br.com.ofisy.interfaces.api.stockmovement;

import br.com.ofisy.application.stockmovement.StockMovementService;
import br.com.ofisy.application.stockmovement.dto.StockMovementResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
public class StockMovementController implements StockMovementApi {

    private final StockMovementService stockMovementService;

    @GetMapping
    public ResponseEntity<Page<StockMovementResponseDTO>> getAllStockMovements(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(stockMovementService.findAll(pageable));
    }

    @GetMapping(params = "stockId")
    public ResponseEntity<Page<StockMovementResponseDTO>> getByStockId(
            @RequestParam(required = false) UUID stockId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(stockMovementService.findByStockId(stockId, pageable));
    }
}
