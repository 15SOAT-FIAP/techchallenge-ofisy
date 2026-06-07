package br.com.ofisy.application.stock;

import br.com.ofisy.application.notification.createlowstock.CreateLowStockNotificationUseCase;
import br.com.ofisy.application.stock.dto.CreateStockRequestDTO;
import br.com.ofisy.application.stock.dto.StockResponseDTO;
import br.com.ofisy.application.stock.dto.UpdateStockRequestDTO;
import br.com.ofisy.application.stock.exceptions.InsufficientStockException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.application.stockmovement.StockMovementService;
import br.com.ofisy.application.stockmovement.dto.StockMovementRequestDTO;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import br.com.ofisy.domain.stockmovement.MovementType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMovementService stockMovementService;

    private final StockRepository stockRepository;

    private final CreateLowStockNotificationUseCase createLowStockNotificationUseCase;

    @Transactional
    public StockResponseDTO create(CreateStockRequestDTO createStockRequestDTO) {
        Stock stock = StockMapper.toDomain(createStockRequestDTO);

        Stock savedStock = stockRepository.save(stock);

        return StockMapper.toDTO(savedStock);
    }

    @Transactional
    public StockResponseDTO update(UUID stockId, UpdateStockRequestDTO request) {
        Stock existingStock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException(stockId));

        existingStock.update(
                request.productName(),
                request.description(),
                request.unitPrice(),
                request.category(),
                request.minThreshold()
        );

        return StockMapper.toDTO(existingStock);
    }

    @Transactional
    public StockResponseDTO addStock(UUID stockId, Integer quantity) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException(stockId));

        Integer previousQuantity = stock.getQuantity();
        Integer newQuantity = stock.getQuantity() + quantity;

        stock.addQuantity(quantity);

        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO(
                stockId,
                MovementType.IN,
                quantity,
                previousQuantity,
                newQuantity
        );

        stockMovementService.registerMovement(requestDTO);

        Stock savedStock = stockRepository.save(stock);

        return StockMapper.toDTO(savedStock);
    }

    @Transactional
    public StockResponseDTO consumeStock(UUID stockId, Integer quantity) {
        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new StockNotFoundException(stockId));

        if (stock.getQuantity() == null || stock.getQuantity() < quantity) {
            throw new InsufficientStockException(stockId);
        }

        Integer previousQuantity = stock.getQuantity();
        Integer newQuantity = stock.getQuantity() - quantity;

        stock.consumeQuantity(quantity);

        StockMovementRequestDTO requestDTO = new StockMovementRequestDTO(
                stockId,
                MovementType.OUT,
                quantity,
                previousQuantity,
                newQuantity
        );

        stockMovementService.registerMovement(requestDTO);

        Stock savedStock = stockRepository.save(stock);

        if (savedStock.isLowStock()) {
            createLowStockNotificationUseCase.execute(
                    new CreateLowStockNotificationUseCase.CreateLowStockCommand(
                            savedStock.getId(),
                            savedStock.getProductName(),
                            savedStock.getQuantity(),
                            savedStock.getMinThreshold()
                    )
            );
        }

        return StockMapper.toDTO(savedStock);
    }

    @Transactional(readOnly = true)
    public Page<StockResponseDTO> findAll(Pageable pageable) {
        Page<Stock> stockList = stockRepository.findAll(pageable);

        return stockList.map(StockMapper::toDTO);
    }

    @Transactional(readOnly = true)
    public StockResponseDTO findById(UUID id) {
        if (id == null) {
            throw new IllegalArgumentException("ID não pode ser nulo");
        }
        var stock = stockRepository.findById(id)
                .orElseThrow(() -> new StockNotFoundException(id));

        return StockMapper.toDTO(stock);
    }

    @Transactional(readOnly = true)
    public StockResponseDTO findByProductName(String productName) {
        var stock = stockRepository.findByProductName(productName)
                .orElseThrow(() -> new StockNotFoundException(productName));

        return StockMapper.toDTO(stock);
    }
}
