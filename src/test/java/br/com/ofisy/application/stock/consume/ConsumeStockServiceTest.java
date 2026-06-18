package br.com.ofisy.application.stock.consume;

import br.com.ofisy.application.notification.createlowstock.CreateLowStockNotificationUseCase;
import br.com.ofisy.application.stock.exceptions.InsufficientStockException;
import br.com.ofisy.application.stockmovement.register.RegisterStockMovementUseCase;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumeStockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private RegisterStockMovementUseCase registerStockMovementUseCase;

    @Mock
    private CreateLowStockNotificationUseCase createLowStockNotificationUseCase;

    @InjectMocks
    private ConsumeStockService consumeStockService;

    @Test
    @DisplayName("Deve consumir itens no estoque com sucesso")
    void shouldConsumeStockSuccessfully() {
        Stock stock = Stock.create("Filtro de Óleo", "Filtro 1.6", 40, new BigDecimal("100.00"), "Filtros", 10);
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(stock, "id", id);

        when(stockRepository.findById(id)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        Stock result = consumeStockService.execute(new ConsumeStockUseCase.ConsumeStockCommand(id, 10));

        assertThat(result.getQuantity()).isEqualTo(30);
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    @DisplayName("Deve lançar exception quando estoque é insuficiente")
    void shouldThrowExceptionWhenInsufficientStock() {
        Stock stock = Stock.create("Filtro de combustível", "Filtro", 5, new BigDecimal("90.00"), "Filtros", 10);
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(stock, "id", id);

        when(stockRepository.findById(id)).thenReturn(Optional.of(stock));

        assertThrows(InsufficientStockException.class,
                () -> consumeStockService.execute(new ConsumeStockUseCase.ConsumeStockCommand(id, 100)));
        verify(stockRepository, never()).save(any(Stock.class));
    }
}
