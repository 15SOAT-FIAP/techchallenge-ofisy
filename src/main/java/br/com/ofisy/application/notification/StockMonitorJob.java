package br.com.ofisy.application.notification;

import br.com.ofisy.application.stock.StockService;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockMonitorJob {

    private final StockRepository stockRepository;
    private final NotificationService notificationService;
    private final StockService stockService;

    @Scheduled(fixedDelay = 60_000)
    public void monitor() {
        List<Stock> stocks = stockRepository.findAllBelowThreshold();
        for (Stock stock : stocks) {
            try {
                notificationService.sendStockAlert(stock);
                stockService.addStock(stock.getId(), stock.getMinThreshold());
            } catch (Exception e) {
                log.error("Erro ao processar stock {}: {}", stock.getId(), e.getMessage());
            }
        }
    }
}
