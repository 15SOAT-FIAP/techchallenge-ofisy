package br.com.ofisy.application.stock;

import br.com.ofisy.application.stock.consume.ConsumeStockUseCase;
import br.com.ofisy.application.stock.create.CreateStockUseCase;
import br.com.ofisy.application.stock.identifybyid.IdentifyByIdStockUseCase;
import br.com.ofisy.application.stock.release.ReleaseStockUseCase;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import br.com.ofisy.integration.IntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StockServiceIT extends IntegrationTestBase {

    @Autowired
    private CreateStockUseCase createStockUseCase;
    @Autowired
    private ConsumeStockUseCase consumeStockUseCase;
    @Autowired
    private ReleaseStockUseCase releaseStockUseCase;
    @Autowired
    private IdentifyByIdStockUseCase identifyByIdStockUseCase;

    @Autowired
    private UserRepository userDomainRepository;
    @Autowired
    private NotificationRepository notificationDomainRepository;

    private UUID stockId;

    @BeforeEach
    void setUp() {
        userDomainRepository.save(
                User.create("stockman.svc.it@ofisy.com", passwordEncoder.encode("Test@123"), "Stockman Svc IT", Role.STOCKMAN)
        );

        var created = createStockUseCase.execute(new CreateStockUseCase.CreateStockCommand(
                "Peça Stock Svc IT", "Peça para testes de stock service", 10, new BigDecimal("100.00"), "Testes", 2
        ));
        stockId = created.getId();
    }

    @Nested
    @DisplayName("consumeStock")
    class ConsumeStock {

        @Test
        @DisplayName("Deve consumir quantidade e persistir no banco")
        void shouldConsumeStockAndPersist() {
            consumeStockUseCase.execute(new ConsumeStockUseCase.ConsumeStockCommand(stockId, 3));

            var stock = identifyByIdStockUseCase.execute(stockId);
            assertThat(stock.getQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("Deve criar notificação de estoque baixo após consumo abaixo do mínimo")
        void shouldCreateLowStockNotificationWhenStockIsBelowMinimum() {
            consumeStockUseCase.execute(new ConsumeStockUseCase.ConsumeStockCommand(stockId, 9));

            var notifications = notificationDomainRepository.findAll().stream()
                    .filter(n -> n.getType() == NotificationType.LOW_STOCK)
                    .filter(n -> stockId.equals(n.getStockId()))
                    .toList();

            assertThat(notifications).hasSize(1);
            assertThat(notifications.getFirst().getMessage().getContent()).contains("Peça Stock Svc IT");
            assertThat(notifications.getFirst().isRead()).isFalse();
        }

        @Test
        @DisplayName("Não deve criar notificação quando estoque ainda está acima do mínimo")
        void shouldNotCreateNotificationWhenStockIsAboveMinimum() {
            consumeStockUseCase.execute(new ConsumeStockUseCase.ConsumeStockCommand(stockId, 1));

            var notifications = notificationDomainRepository.findAll().stream()
                    .filter(n -> n.getType() == NotificationType.LOW_STOCK)
                    .filter(n -> stockId.equals(n.getStockId()))
                    .toList();

            assertThat(notifications).isEmpty();
        }
    }

    @Nested
    @DisplayName("releaseStock")
    class ReleaseStock {

        @Test
        @DisplayName("Deve devolver quantidade ao estoque e persistir no banco")
        void shouldReleaseStockAndPersist() {
            consumeStockUseCase.execute(new ConsumeStockUseCase.ConsumeStockCommand(stockId, 5));
            releaseStockUseCase.execute(new ReleaseStockUseCase.ReleaseStockCommand(stockId, 3));

            var stock = identifyByIdStockUseCase.execute(stockId);
            assertThat(stock.getQuantity()).isEqualTo(8); // 10 - 5 + 3
        }

        @Test
        @DisplayName("Deve permitir devolver estoque acima da quantidade original")
        void shouldAllowReleasingAboveOriginalQuantity() {
            releaseStockUseCase.execute(new ReleaseStockUseCase.ReleaseStockCommand(stockId, 20));

            var stock = identifyByIdStockUseCase.execute(stockId);
            assertThat(stock.getQuantity()).isEqualTo(30); // 10 + 20
        }
    }
}