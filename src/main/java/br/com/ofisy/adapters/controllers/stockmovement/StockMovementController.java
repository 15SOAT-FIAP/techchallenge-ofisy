package br.com.ofisy.adapters.controllers.stockmovement;

import br.com.ofisy.adapters.controllers.stockmovement.dto.StockMovementResponseDTO;
import br.com.ofisy.adapters.presenters.stockmovement.StockMovementPresenter;
import br.com.ofisy.application.stockmovement.list.ListStockMovementUseCase;
import br.com.ofisy.application.stockmovement.listbystock.ListByStockStockMovementUseCase;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stock-movements")
@RequiredArgsConstructor
public class StockMovementController implements StockMovementApi {

    private final ListStockMovementUseCase listStockMovementUseCase;
    private final ListByStockStockMovementUseCase listByStockStockMovementUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCKMAN')")
    public ResponseEntity<Page<StockMovementResponseDTO>> getAllStockMovements(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(listStockMovementUseCase.execute(pageable).map(StockMovementPresenter::present));
    }

    @GetMapping(params = "stockId")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCKMAN')")
    public ResponseEntity<Page<StockMovementResponseDTO>> getByStockId(
            @RequestParam(required = false) UUID stockId,
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(listByStockStockMovementUseCase.execute(stockId, pageable).map(StockMovementPresenter::present));
    }
}
