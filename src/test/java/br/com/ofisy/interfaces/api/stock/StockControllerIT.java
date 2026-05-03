package br.com.ofisy.interfaces.api.stock;

import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import br.com.ofisy.infrastructure.persistence.notification.JpaNotificationRepository;
import br.com.ofisy.infrastructure.persistence.stock.JpaStockRepository;
import br.com.ofisy.infrastructure.persistence.stockmovement.JpaStockMovementRepository;
import br.com.ofisy.infrastructure.persistence.user.JpaUserRepository;
import br.com.ofisy.integration.IntegrationTestBase;
import io.restassured.RestAssured;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class StockControllerIT extends IntegrationTestBase {

    @Autowired
    private JpaStockRepository stockRepository;
    @Autowired
    private JpaStockMovementRepository stockMovementRepository;
    @Autowired
    private JpaNotificationRepository notificationRepository;
    @Autowired
    private JpaUserRepository userRepository;

    @Autowired 
    private StockRepository stockDomainRepository;
    @Autowired 
    private UserRepository userDomainRepository;
    @Autowired 
    private NotificationRepository notificationDomainRepository;

    private UUID stockId;
    private String token;

    @BeforeEach
    void setUp() {
        String rawPassword = "Test@123";
        User user = userDomainRepository.save(
                User.create("stockman.ctrl.it@ofisy.com", passwordEncoder.encode(rawPassword), "Stockman IT", Role.STOCKMAN)
        );
        token = obtainToken(user.getEmail().emailAddress(), rawPassword);

        Stock stock = stockDomainRepository.save(
                Stock.create("Peça Stock Ctrl IT", "Peça para testes de stock controller", 10, new BigDecimal("100.00"), "Testes", 2)
        );
        stockId = stock.getId();
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        stockMovementRepository.deleteAll();
        stockRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("POST /api/v1/stocks/{id}/consume")
    class ConsumeStock {

        @Test
        @DisplayName("Deve consumir quantidade e persistir no banco")
        void shouldConsumeStockAndPersist() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .post("/api/v1/stocks/{id}/consume?quantity=3", stockId)
                    .then()
                    .statusCode(200);

            var stock = stockRepository.findById(stockId).orElseThrow();
            assertThat(stock.getQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("Deve criar notificação de estoque baixo após consumo abaixo do mínimo")
        void shouldCreateLowStockNotificationWhenStockIsBelowMinimum() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .post("/api/v1/stocks/{id}/consume?quantity=9", stockId)
                    .then()
                    .statusCode(200);

            var notifications = notificationDomainRepository.findAll().stream()
                    .filter(n -> n.getType() == NotificationType.LOW_STOCK)
                    .filter(n -> stockId.equals(n.getStockId()))
                    .toList();

            assertThat(notifications).hasSize(1);
            assertThat(notifications.getFirst().getMessage()).contains("Peça Stock Ctrl IT");
            assertThat(notifications.getFirst().getRead()).isFalse();
        }

        @Test
        @DisplayName("Não deve criar notificação quando estoque ainda está acima do mínimo")
        void shouldNotCreateNotificationWhenStockIsAboveMinimum() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .post("/api/v1/stocks/{id}/consume?quantity=1", stockId)
                    .then()
                    .statusCode(200);

            var notifications = notificationDomainRepository.findAll().stream()
                    .filter(n -> n.getType() == NotificationType.LOW_STOCK)
                    .filter(n -> stockId.equals(n.getStockId()))
                    .toList();

            assertThat(notifications).isEmpty();
        }
    }
}