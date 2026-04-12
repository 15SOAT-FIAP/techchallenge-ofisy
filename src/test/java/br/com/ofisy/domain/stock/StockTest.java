package br.com.ofisy.domain.stock;

import br.com.ofisy.application.stock.StockService;
import br.com.ofisy.application.stock.exceptions.InsufficientStockException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class StockTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private StockService stockService;

    private Stock stock;

    private Stock lowStock;

    @BeforeEach
    void setUp() {
        stock = createStock();
        lowStock = createLowStock();
    }

    @Test
    void shoulAddStockSuccessfully() {
        when(stockRepository.findById(stock.getId())).thenReturn(Optional.of(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(stock);

        Stock result = stockService.addStock(stock.getId(), 10);

        assertEquals(50, result.getQuantity());
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    void shouldThrowExceptionWhenStockNotFound() {
        when(stockRepository.findById(stock.getId())).thenReturn(Optional.empty());

        assertThrows(StockNotFoundException.class, () -> stockService.addStock(stock.getId(), 10));
        verify(stockRepository, never()).save(any(Stock.class));
    }

    @Test
    void shouldThrowExceptionWhenInsufficientStock() {
        when(stockRepository.findById(lowStock.getId())).thenReturn(Optional.of(lowStock));

        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> stockService.consumeStock(lowStock.getId(), 100)
        );

        verify(stockRepository, never()).save(any(Stock.class));
    }

    private Stock createStock() {
        return Stock.create(
                "Filtro de Óleo",
                "Filtro de óleo para motor 1.6",
                40,
                new BigDecimal("100.00"),
                "Filtros",
                10
        );
    }

    private Stock createLowStock() {
        return Stock.create(
                "Filtro de combustível",
                "Filtro de combustível",
                50,
                new BigDecimal("90.00"),
                "Filtros",
                10
        );
    }
}
