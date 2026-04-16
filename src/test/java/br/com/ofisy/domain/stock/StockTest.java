package br.com.ofisy.domain.stock;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class StockTest {

    private Stock stock;
    private Stock lowStock;

    @BeforeEach
    void setUp() {
        stock = createStock();
        lowStock = createLowStock();
    }

    @Test
    @DisplayName("Deve adicionar itens no estoque com sucesso")
    void shouldAddStockSuccessfully() {
        stock.addQuantity(10);

        assertEquals(50, stock.getQuantity());
    }

    @Test
    @DisplayName("Deve consumir itens do estoque com sucesso")
    void shouldConsumeStockSuccessfully() {
        stock.consumeQuantity(10);

        assertEquals(30, stock.getQuantity());
    }

    @Test
    @DisplayName("Deve identificar que estoque está abaixo do limite mínimo")
    void shouldIdentifyStockBelowMinThreshold() {
        lowStock.consumeQuantity(100);

        assertEquals(true, lowStock.isLowStock());
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