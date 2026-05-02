package br.com.ofisy.integration;

import br.com.ofisy.application.stock.StockService;
import br.com.ofisy.application.stock.dto.CreateStockRequestDTO;
import br.com.ofisy.application.stock.dto.UpdateStockRequestDTO;
import br.com.ofisy.application.stock.exceptions.InsufficientStockException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
class StockIntegrationTest extends IntegrationTestBase {

    @Autowired
    private StockService stockService;

    @Autowired
    private NotificationRepository notificationRepository;

    private UUID stockId;

    @BeforeEach
    void setUp() {
        var created = stockService.create(new CreateStockRequestDTO(
                "Peça de Teste Stock",
                "Peça exclusiva para testes de integração",
                10,
                new BigDecimal("100.00"),
                "Testes",
                2
        ));
        stockId = created.id();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar item de estoque com sucesso")
        void shouldCreateStockSuccessfully() {
            var request = new CreateStockRequestDTO(
                    "Nova Peça Teste",
                    "Descrição da peça",
                    5,
                    new BigDecimal("50.00"),
                    "Motor",
                    1
            );

            var response = stockService.create(request);

            assertThat(response).isNotNull();
            assertThat(response.productName()).isEqualTo("Nova Peça Teste");
            assertThat(response.quantity()).isEqualTo(5);
            assertThat(response.unitPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(response.isLowStock()).isFalse();
            assertThat(response.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve criar item com estoque baixo quando quantidade menor que mínimo")
        void shouldCreateStockWithLowStockFlag() {
            var request = new CreateStockRequestDTO(
                    "Peça Crítica Teste",
                    "Peça com estoque baixo",
                    1,
                    new BigDecimal("200.00"),
                    "Motor",
                    5
            );

            var response = stockService.create(request);

            assertThat(response.isLowStock()).isTrue();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar item de estoque quando encontrado")
        void shouldReturnStockWhenFound() {
            var response = stockService.findById(stockId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(stockId);
            assertThat(response.productName()).isEqualTo("Peça de Teste Stock");
        }

        @Test
        @DisplayName("Deve lançar exceção quando item não encontrado")
        void shouldThrowExceptionWhenStockNotFound() {
            assertThatThrownBy(() -> stockService.findById(UUID.randomUUID()))
                    .isInstanceOf(StockNotFoundException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando ID é nulo")
        void shouldThrowExceptionWhenIdIsNull() {
            assertThatThrownBy(() -> stockService.findById(null))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("findByProductName")
    class FindByProductName {

        @Test
        @DisplayName("Deve retornar item de estoque pelo nome do produto")
        void shouldReturnStockByProductName() {
            var response = stockService.findByProductName("Peça de Teste Stock");

            assertThat(response).isNotNull();
            assertThat(response.productName()).isEqualTo("Peça de Teste Stock");
        }

        @Test
        @DisplayName("Deve lançar exceção quando produto não encontrado pelo nome")
        void shouldThrowExceptionWhenProductNameNotFound() {
            assertThatThrownBy(() -> stockService.findByProductName("Produto Inexistente"))
                    .isInstanceOf(StockNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retornar página com itens de estoque")
        void shouldReturnPageWithStockItems() {
            var response = stockService.findAll(PageRequest.of(0, 10));

            assertThat(response.getContent()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("Deve atualizar item de estoque com sucesso")
        void shouldUpdateStockSuccessfully() {
            var request = new UpdateStockRequestDTO(
                    "Peça Atualizada Teste",
                    "Nova descrição",
                    new BigDecimal("150.00"),
                    "Freios",
                    3
            );

            var response = stockService.update(stockId, request);

            assertThat(response.productName()).isEqualTo("Peça Atualizada Teste");
            assertThat(response.unitPrice()).isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(response.category()).isEqualTo("Freios");
            assertThat(response.minThreshold()).isEqualTo(3);
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar item não encontrado")
        void shouldThrowExceptionWhenUpdatingNonExistentStock() {
            var request = new UpdateStockRequestDTO(null, null, null, null, null);

            assertThatThrownBy(() -> stockService.update(UUID.randomUUID(), request))
                    .isInstanceOf(StockNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("addStock")
    class AddStock {

        @Test
        @DisplayName("Deve adicionar quantidade ao estoque com sucesso")
        void shouldAddStockSuccessfully() {
            var before = stockService.findById(stockId).quantity();

            var response = stockService.addStock(stockId, 5);

            assertThat(response.quantity()).isEqualTo(before + 5);
        }

        @Test
        @DisplayName("Deve lançar exceção ao adicionar estoque em item não encontrado")
        void shouldThrowExceptionWhenAddingToNonExistentStock() {
            assertThatThrownBy(() -> stockService.addStock(UUID.randomUUID(), 5))
                    .isInstanceOf(StockNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("consumeStock")
    class ConsumeStock {

        @Test
        @DisplayName("Deve consumir quantidade do estoque com sucesso")
        void shouldConsumeStockSuccessfully() {
            var before = stockService.findById(stockId).quantity();

            var response = stockService.consumeStock(stockId, 3);

            assertThat(response.quantity()).isEqualTo(before - 3);
        }

        @Test
        @DisplayName("Deve lançar exceção quando estoque insuficiente")
        void shouldThrowExceptionWhenInsufficientStock() {
            assertThatThrownBy(() -> stockService.consumeStock(stockId, 999))
                    .isInstanceOf(InsufficientStockException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção ao consumir estoque de item não encontrado")
        void shouldThrowExceptionWhenConsumingNonExistentStock() {
            assertThatThrownBy(() -> stockService.consumeStock(UUID.randomUUID(), 1))
                    .isInstanceOf(StockNotFoundException.class);
        }

        @Test
        @DisplayName("Deve sinalizar estoque baixo após consumo")
        void shouldFlagLowStockAfterConsumption() {
            var response = stockService.consumeStock(stockId, 9);

            assertThat(response.isLowStock()).isTrue();
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
