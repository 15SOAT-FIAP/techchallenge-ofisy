package br.com.ofisy.application.stock.add;

import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.application.stockmovement.StockMovementService;
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
class AddStockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMovementService stockMovementService;

    @InjectMocks
    private AddStockService addStockService;

    @Test
    @DisplayName("Deve adicionar itens no estoque com sucesso")
    void shouldAddStockSuccessfully() {
        Stock stock = Stock.create("Filtro de Óleo", "Filtro 1.6", 40, new BigDecimal("100.00"), "Filtros", 10);
        UUID id = UUID.randomUUID();
        ReflectionTestUtils.setField(stock, "id", id);

        when(stockRepository.findById(id)).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenAnswer(inv -> inv.getArgument(0));

        Stock result = addStockService.execute(new AddStockUseCase.AddStockCommand(id, 10));

        assertThat(result.getQuantity()).isEqualTo(50);
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    @DisplayName("Deve lançar exception quando estoque não é localizado")
    void shouldThrowExceptionWhenStockNotFound() {
        UUID id = UUID.randomUUID();
        when(stockRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class,
                () -> addStockService.execute(new AddStockUseCase.AddStockCommand(id, 10)));
        verify(stockRepository, never()).save(any(Stock.class));
    }
}
