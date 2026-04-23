package br.com.ofisy.application.notification;

import br.com.ofisy.application.stock.StockService;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockMonitorJobTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private StockService stockService;

    @InjectMocks
    private StockMonitorJob stockMonitorJob;

    private Stock buildStock(String productName, int quantity, int minThreshold) {
        return Stock.create(productName, "desc", quantity, BigDecimal.TEN, "cat", minThreshold);
    }

    // -------------------------------------------------------------------------
    // monitor() — lista vazia
    // -------------------------------------------------------------------------

    @Nested
    class EmptyList {

        @Test
        void shouldDoNothingWhenNoStocksBelowThreshold() {
            // Requisito 4.6: nenhum produto elegível → nenhuma notificação criada
            when(stockRepository.findAllBelowThreshold()).thenReturn(List.of());

            stockMonitorJob.monitor();

            verify(notificationService, never()).sendStockAlert(any());
            verify(stockService, never()).addStock(any(), any());
        }
    }

    // -------------------------------------------------------------------------
    // monitor() — um produto elegível
    // -------------------------------------------------------------------------

    @Nested
    class SingleEligibleProduct {

        @Test
        void shouldCreateNotificationAndAddStockForEligibleProduct() {
            // Requisito 4.1: verifica produto com quantity <= minThreshold
            // Requisito 4.4: incrementa quantity em minThreshold unidades
            Stock stock = buildStock("Caneta Azul", 2, 5);
            when(stockRepository.findAllBelowThreshold()).thenReturn(List.of(stock));

            stockMonitorJob.monitor();

            verify(notificationService).sendStockAlert(stock);
            verify(stockService).addStock(stock.getId(), stock.getMinThreshold());
        }

        @Test
        void shouldCallAddStockWithCorrectStockIdAndMinThreshold() {
            Stock stock = buildStock("Papel A4", 1, 10);
            UUID expectedId = stock.getId();
            when(stockRepository.findAllBelowThreshold()).thenReturn(List.of(stock));

            stockMonitorJob.monitor();

            verify(stockService).addStock(eq(expectedId), eq(10));
        }
    }

    // -------------------------------------------------------------------------
    // monitor() — múltiplos produtos
    // -------------------------------------------------------------------------

    @Nested
    class MultipleProducts {

        @Test
        void shouldProcessAllEligibleProducts() {
            // Requisito 4.1: todos os produtos elegíveis são processados
            Stock stock1 = buildStock("Produto A", 1, 5);
            Stock stock2 = buildStock("Produto B", 3, 10);
            Stock stock3 = buildStock("Produto C", 0, 2);
            when(stockRepository.findAllBelowThreshold()).thenReturn(List.of(stock1, stock2, stock3));

            stockMonitorJob.monitor();

            verify(notificationService).sendStockAlert(stock1);
            verify(notificationService).sendStockAlert(stock2);
            verify(notificationService).sendStockAlert(stock3);
            verify(stockService).addStock(stock1.getId(), stock1.getMinThreshold());
            verify(stockService).addStock(stock2.getId(), stock2.getMinThreshold());
            verify(stockService).addStock(stock3.getId(), stock3.getMinThreshold());
        }
    }

    // -------------------------------------------------------------------------
    // monitor() — isolamento de falha
    // -------------------------------------------------------------------------

    @Nested
    class FailureIsolation {

        @Test
        void shouldContinueProcessingRemainingProductsWhenOneThrowsException() {
            // Requisito 4.7: falha em um produto não impede os demais
            Stock stock1 = buildStock("Produto Falho", 1, 5);
            Stock stock2 = buildStock("Produto OK", 2, 8);
            when(stockRepository.findAllBelowThreshold()).thenReturn(List.of(stock1, stock2));
            doThrow(new RuntimeException("Erro simulado")).when(notificationService).sendStockAlert(stock1);

            stockMonitorJob.monitor();

            // sendStockAlert foi chamado para ambos
            verify(notificationService).sendStockAlert(stock1);
            verify(notificationService).sendStockAlert(stock2);
            // addStock só foi chamado uma vez (para stock2, pois stock1 falhou antes)
            verify(stockService, times(1)).addStock(any(), any());
            verify(stockService).addStock(stock2.getId(), stock2.getMinThreshold());
        }

        @Test
        void shouldContinueProcessingWhenAddStockThrowsException() {
            // Requisito 4.7: falha no addStock também é isolada
            Stock stock1 = buildStock("Produto Falho", 1, 5);
            Stock stock2 = buildStock("Produto OK", 2, 8);
            when(stockRepository.findAllBelowThreshold()).thenReturn(List.of(stock1, stock2));
            doThrow(new RuntimeException("Erro no addStock")).when(stockService).addStock(stock1.getId(), stock1.getMinThreshold());

            stockMonitorJob.monitor();

            verify(notificationService).sendStockAlert(stock1);
            verify(stockService).addStock(stock1.getId(), stock1.getMinThreshold());
            verify(notificationService).sendStockAlert(stock2);
            verify(stockService).addStock(stock2.getId(), stock2.getMinThreshold());
        }
    }
}
