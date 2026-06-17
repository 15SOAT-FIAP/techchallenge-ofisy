package br.com.ofisy.application.stock.consume;

import br.com.ofisy.application.notification.createlowstock.CreateLowStockNotificationUseCase;
import br.com.ofisy.application.stock.exceptions.InsufficientStockException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.application.stockmovement.StockMovementService;
import br.com.ofisy.application.stockmovement.dto.StockMovementRequestDTO;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import br.com.ofisy.domain.stockmovement.MovementType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConsumeStockService implements ConsumeStockUseCase {

    private final StockRepository stockRepository;
    private final StockMovementService stockMovementService;
    private final CreateLowStockNotificationUseCase createLowStockNotificationUseCase;

    public ConsumeStockService(StockRepository stockRepository,
                               StockMovementService stockMovementService,
                               CreateLowStockNotificationUseCase createLowStockNotificationUseCase) {
        this.stockRepository = stockRepository;
        this.stockMovementService = stockMovementService;
        this.createLowStockNotificationUseCase = createLowStockNotificationUseCase;
    }

    @Override
    public Stock execute(ConsumeStockCommand cmd) {
        Stock stock = stockRepository.findById(cmd.stockId())
                .orElseThrow(() -> new StockNotFoundException(cmd.stockId()));

        if (stock.getQuantity() == null || stock.getQuantity() < cmd.quantity()) {
            throw new InsufficientStockException(cmd.stockId());
        }

        Integer previousQuantity = stock.getQuantity();
        stock.consumeQuantity(cmd.quantity());
        Integer newQuantity = stock.getQuantity();

        stockMovementService.registerMovement(new StockMovementRequestDTO(
                cmd.stockId(),
                MovementType.OUT,
                cmd.quantity(),
                previousQuantity,
                newQuantity
        ));

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

        return savedStock;
    }
}
