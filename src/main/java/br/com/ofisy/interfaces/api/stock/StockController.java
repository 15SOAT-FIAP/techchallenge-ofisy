package br.com.ofisy.interfaces.api.stock;

import br.com.ofisy.application.stock.StockService;
import br.com.ofisy.application.stock.dto.CreateStockRequestDTO;
import br.com.ofisy.application.stock.dto.StockResponseDTO;
import br.com.ofisy.application.stock.dto.UpdateStockRequestDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController implements StockApi {

    private final StockService stockService;

    @GetMapping
    public ResponseEntity<Page<StockResponseDTO>> getAllStocks(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(stockService.findAll(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockResponseDTO> getStockById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(stockService.findById(id));
    }

    @GetMapping("/productName/{productName}")
    public ResponseEntity<StockResponseDTO> getByProductName(
            @PathVariable String productName) {

        return ResponseEntity.ok(stockService.findByProductName(productName));
    }

    @PostMapping
    public ResponseEntity<StockResponseDTO> create(
            @Valid @RequestBody CreateStockRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.create(requestDTO));
    }

    @PostMapping("/{id}/add")
    public ResponseEntity<StockResponseDTO> addStock(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stockService.addStock(id, quantity));
    }

    @PostMapping("/{id}/consume")
    public ResponseEntity<StockResponseDTO> consumeStock(
            @PathVariable UUID id,
            @RequestParam Integer quantity) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(stockService.consumeStock(id, quantity));
    }

    @PutMapping("/{id}")
    public ResponseEntity<StockResponseDTO> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStockRequestDTO updateStockDTO) {

        return ResponseEntity.ok(stockService.update(id, updateStockDTO));
    }
}
