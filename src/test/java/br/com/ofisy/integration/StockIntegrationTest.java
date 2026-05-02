package br.com.ofisy.integration;

import br.com.ofisy.application.stock.StockService;
import br.com.ofisy.application.stock.dto.CreateStockRequestDTO;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
class StockIntegrationTest extends IntegrationTestBase {

    public static final String PRICE_100 = "100.00";
    @Autowired
    private StockService stockService;

    @Autowired
    private NotificationRepository notificationRepository;

    private UUID stockId;

    @BeforeEach
    void setUp() {
        var created = stockService.create(new CreateStockRequestDTO(
                "Peça de Teste Stock", "Peça exclusiva para testes de integração",
                10, new BigDecimal(PRICE_100), "Testes", 2
        ));
        stockId = created.id();
    }

    @Nested
    @DisplayName("consumeStock")
    class ConsumeStock {

        @Test
        @DisplayName("Deve consumir quantidade e persistir no banco")
        void shouldConsumeStockAndPersist() {
            stockService.consumeStock(stockId, 3);

            var stock = stockService.findById(stockId);
            assertThat(stock.quantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("Deve criar notificação de estoque baixo após consumo abaixo do mínimo")
        void shouldCreateLowStockNotificationWhenStockIsBelowMinimum() {
            stockService.consumeStock(stockId, 9);

            var notifications = notificationRepository.findAll().stream()
                    .filter(n -> n.getType() == NotificationType.LOW_STOCK)
                    .filter(n -> stockId.equals(n.getStockId()))
                    .toList();

            assertThat(notifications).hasSize(1);
            assertThat(notifications.getFirst().getMessage()).contains("Peça de Teste Stock");
            assertThat(notifications.getFirst().getRead()).isFalse();
        }

        @Test
        @DisplayName("Não deve criar notificação quando estoque ainda está acima do mínimo")
        void shouldNotCreateNotificationWhenStockIsAboveMinimum() {
            stockService.consumeStock(stockId, 1);

            var notifications = notificationRepository.findAll().stream()
                    .filter(n -> n.getType() == NotificationType.LOW_STOCK)
                    .filter(n -> stockId.equals(n.getStockId()))
                    .toList();

            assertThat(notifications).isEmpty();
        }
    }
}
