package br.com.ofisy.application.notification;

import br.com.ofisy.application.stock.StockService;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import net.jqwik.api.*;
import net.jqwik.api.Combinators;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

// Feature: notification-stock-monitor, Propriedade 6: jobCreatesNotificationAndReplenishesStock
class StockMonitorJobPropertyTest {

    private final StockRepository stockRepository = Mockito.mock(StockRepository.class);
    private final NotificationService notificationService = Mockito.mock(NotificationService.class);
    private final StockService stockService = Mockito.mock(StockService.class);
    private final StockMonitorJob stockMonitorJob =
            new StockMonitorJob(stockRepository, notificationService, stockService);

    /**
     * Propriedade 6: Cron job cria notificação e reabastece estoque
     * Valida: Requisitos 4.1, 4.2, 4.4
     *
     * Para qualquer lista de produtos com quantity <= minThreshold,
     * após monitor() ser executado:
     * - notificationService.sendStockAlert() deve ser chamado uma vez por produto
     * - stockService.addStock() deve ser chamado com o id e minThreshold de cada produto
     */
    @Property(tries = 100)
    void jobCreatesNotificationAndReplenishesStock(
            @ForAll("lowStockLists") List<Stock> stocks
    ) {
        // Arrange — reset all mocks to clear accumulated invocations between tries
        Mockito.reset(stockRepository, notificationService, stockService);
        when(stockRepository.findAllBelowThreshold()).thenReturn(stocks);

        // Act
        stockMonitorJob.monitor();

        // Assert — sendStockAlert called once per stock (each stock object is distinct)
        for (Stock stock : stocks) {
            verify(notificationService).sendStockAlert(stock);
        }

        // Assert — addStock called exactly once per stock with correct id and minThreshold
        // Capture all addStock calls and verify they match the expected stocks
        @SuppressWarnings("unchecked")
        ArgumentCaptor<UUID> idCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<Integer> thresholdCaptor = ArgumentCaptor.forClass(Integer.class);
        verify(stockService, times(stocks.size())).addStock(idCaptor.capture(), thresholdCaptor.capture());

        List<UUID> capturedIds = idCaptor.getAllValues();
        List<Integer> capturedThresholds = thresholdCaptor.getAllValues();

        assertThat(capturedIds).hasSize(stocks.size());
        assertThat(capturedThresholds).hasSize(stocks.size());

        for (int i = 0; i < stocks.size(); i++) {
            assertThat(capturedIds.get(i)).isEqualTo(stocks.get(i).getId());
            assertThat(capturedThresholds.get(i)).isEqualTo(stocks.get(i).getMinThreshold());
        }

        verifyNoMoreInteractions(notificationService, stockService);
    }

    @Provide
    Arbitrary<List<Stock>> lowStockLists() {
        // Generate product names: unique enough via index, but jqwik doesn't guarantee uniqueness
        // Use simple alphanumeric names to avoid Stock unique constraint issues in-memory
        Arbitrary<String> productNames = Arbitraries.strings()
                .withCharRange('a', 'z')
                .ofMinLength(3)
                .ofMaxLength(20);

        Arbitrary<Integer> minThresholds = Arbitraries.integers().between(1, 50);

        // quantity <= minThreshold to satisfy isLowStock() = true
        Arbitrary<Stock> lowStockArbitrary = Combinators.combine(productNames, minThresholds)
                .as((name, minThreshold) -> {
                    // quantity is between 0 and minThreshold (inclusive) so isLowStock() = true
                    int quantity = (int) (Math.random() * (minThreshold + 1));
                    return Stock.create(name, "desc", quantity, BigDecimal.TEN, "cat", minThreshold);
                });

        return lowStockArbitrary.list().ofMinSize(1).ofMaxSize(5);
    }
}
