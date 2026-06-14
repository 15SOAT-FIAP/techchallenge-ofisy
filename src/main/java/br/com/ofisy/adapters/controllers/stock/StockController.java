package br.com.ofisy.adapters.controllers.stock;

import br.com.ofisy.adapters.controllers.stock.dto.CreateStockRequestDTO;
import br.com.ofisy.adapters.controllers.stock.dto.StockResponseDTO;
import br.com.ofisy.adapters.controllers.stock.dto.UpdateStockRequestDTO;
import br.com.ofisy.adapters.presenters.stock.StockPresenter;
import br.com.ofisy.application.stock.add.AddStockUseCase;
import br.com.ofisy.application.stock.consume.ConsumeStockUseCase;
import br.com.ofisy.application.stock.create.CreateStockUseCase;
import br.com.ofisy.application.stock.identifybyid.IdentifyByIdStockUseCase;
import br.com.ofisy.application.stock.identifybyproduct.IdentifyByProductStockUseCase;
import br.com.ofisy.application.stock.list.ListStockUseCase;
import br.com.ofisy.application.stock.update.UpdateStockUseCase;
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
@RequestMapping("/api/v1/stocks")
@RequiredArgsConstructor
public class StockController implements StockApi {

    private final CreateStockUseCase createStockUseCase;
    private final UpdateStockUseCase updateStockUseCase;
    private final AddStockUseCase addStockUseCase;
    private final ConsumeStockUseCase consumeStockUseCase;
    private final ListStockUseCase listStockUseCase;
    private final IdentifyByIdStockUseCase identifyByIdStockUseCase;
    private final IdentifyByProductStockUseCase identifyByProductStockUseCase;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCKMAN', 'MECHANIC')")
    public ResponseEntity<Page<StockResponseDTO>> getAllStocks(
            @ParameterObject @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(listStockUseCase.execute(pageable).map(StockPresenter::present));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCKMAN', 'MECHANIC')")
    public ResponseEntity<StockResponseDTO> getStockById(@PathVariable UUID id) {

        return ResponseEntity.ok(StockPresenter.present(identifyByIdStockUseCase.execute(id)));
    }

    @GetMapping("/productName/{productName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCKMAN', 'MECHANIC')")
    public ResponseEntity<StockResponseDTO> getByProductName(@PathVariable String productName) {

        return ResponseEntity.ok(StockPresenter.present(identifyByProductStockUseCase.execute(productName)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCKMAN')")
    public ResponseEntity<StockResponseDTO> create(@Valid @RequestBody CreateStockRequestDTO requestDTO) {

        CreateStockUseCase.CreateStockCommand cmd = new CreateStockUseCase.CreateStockCommand(
                requestDTO.productName(),
                requestDTO.description(),
                requestDTO.quantity(),
                requestDTO.unitPrice(),
                requestDTO.category(),
                requestDTO.minThreshold()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(StockPresenter.present(createStockUseCase.execute(cmd)));
    }

    @PostMapping("/{id}/add")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCKMAN')")
    public ResponseEntity<StockResponseDTO> addStock(@PathVariable UUID id, @RequestParam Integer quantity) {

        AddStockUseCase.AddStockCommand cmd = new AddStockUseCase.AddStockCommand(id, quantity);
        return ResponseEntity.ok(StockPresenter.present(addStockUseCase.execute(cmd)));
    }

    @PostMapping("/{id}/consume")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCKMAN')")
    public ResponseEntity<StockResponseDTO> consumeStock(@PathVariable UUID id, @RequestParam Integer quantity) {

        ConsumeStockUseCase.ConsumeStockCommand cmd = new ConsumeStockUseCase.ConsumeStockCommand(id, quantity);
        return ResponseEntity.ok(StockPresenter.present(consumeStockUseCase.execute(cmd)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STOCKMAN')")
    public ResponseEntity<StockResponseDTO> update(@PathVariable UUID id,
                                                   @Valid @RequestBody UpdateStockRequestDTO requestDTO) {

        UpdateStockUseCase.UpdateStockCommand cmd = new UpdateStockUseCase.UpdateStockCommand(
                id,
                requestDTO.productName(),
                requestDTO.description(),
                requestDTO.unitPrice(),
                requestDTO.category(),
                requestDTO.minThreshold()
        );
        return ResponseEntity.ok(StockPresenter.present(updateStockUseCase.execute(cmd)));
    }
}
