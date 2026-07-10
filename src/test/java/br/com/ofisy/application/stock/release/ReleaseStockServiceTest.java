package br.com.ofisy.application.stock.release;

import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.application.stockmovement.register.RegisterStockMovementUseCase;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import br.com.ofisy.domain.stock.exceptions.InvalidOperationException;
import br.com.ofisy.domain.stockmovement.MovementType;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReleaseStockServiceTest {

    private static final UUID STOCK_ID = UUID.randomUUID();
    public static final String PRICE_100 = "100.00";

    @Mock
    private StockRepository stockRepository;
    @Mock
    private RegisterStockMovementUseCase registerStockMovementUseCase;

    @InjectMocks
    private ReleaseStockService service;

    @Nested
    class Execute {

        @Test
        void shouldReleaseQuantityAndPersist() {
            var stock = validStock(5);
            var cmd = new ReleaseStockUseCase.ReleaseStockCommand(STOCK_ID, 3);

            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

            var result = service.execute(cmd);

            assertThat(result.getQuantity()).isEqualTo(8);
            verify(stockRepository).save(stock);
        }

        @Test
        void shouldRegisterInboundMovement() {
            var stock = validStock(5);
            var cmd = new ReleaseStockUseCase.ReleaseStockCommand(STOCK_ID, 3);

            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(stock));
            when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

            service.execute(cmd);

            verify(registerStockMovementUseCase).execute(
                    new RegisterStockMovementUseCase.RegisterStockMovementCommand(
                            STOCK_ID, MovementType.IN, 3, 5, 8));
        }

        @Test
        void shouldThrowWhenStockNotFound() {
            var cmd = new ReleaseStockUseCase.ReleaseStockCommand(STOCK_ID, 1);
            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(StockNotFoundException.class);
        }

        @Test
        void shouldThrowWhenQuantityIsInvalid() {
            var stock = validStock(5);
            var cmd = new ReleaseStockUseCase.ReleaseStockCommand(STOCK_ID, 0);

            when(stockRepository.findById(STOCK_ID)).thenReturn(Optional.of(stock));

            assertThatThrownBy(() -> service.execute(cmd))
                    .isInstanceOf(InvalidOperationException.class);
        }
    }

    private Stock validStock(int quantity) {
        return Stock.reconstruct(STOCK_ID, "Filtro de óleo", "Filtro",
                quantity, new BigDecimal(PRICE_100), "Filtros", 2,
                LocalDateTime.now(), LocalDateTime.now());
    }
}