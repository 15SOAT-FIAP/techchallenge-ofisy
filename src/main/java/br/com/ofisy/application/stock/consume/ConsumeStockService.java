package br.com.ofisy.application.stock.consume;

import br.com.ofisy.application.notification.publish.NotificationEventPublisher;
import br.com.ofisy.application.stock.exceptions.InsufficientStockException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.application.stockmovement.register.RegisterStockMovementUseCase;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import br.com.ofisy.domain.stockmovement.MovementType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConsumeStockService implements ConsumeStockUseCase {

    private final StockRepository stockRepository;
    private final RegisterStockMovementUseCase registerStockMovementUseCase;
    private final NotificationEventPublisher notificationEventPublisher;

    public ConsumeStockService(StockRepository stockRepository,
                               RegisterStockMovementUseCase registerStockMovementUseCase,
                               NotificationEventPublisher notificationEventPublisher) {
        this.stockRepository = stockRepository;
        this.registerStockMovementUseCase = registerStockMovementUseCase;
        this.notificationEventPublisher = notificationEventPublisher;
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

        registerStockMovementUseCase.execute(new RegisterStockMovementUseCase.RegisterStockMovementCommand(
                cmd.stockId(),
                MovementType.OUT,
                cmd.quantity(),
                previousQuantity,
                newQuantity
        ));

        Stock savedStock = stockRepository.save(stock);

        if (savedStock.isLowStock()) {
            notificationEventPublisher.publishLowStock(
                    new NotificationEventPublisher.LowStockEvent(
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
