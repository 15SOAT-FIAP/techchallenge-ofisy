package br.com.ofisy.integration.quote;

import br.com.ofisy.application.quote.QuoteService;
import br.com.ofisy.application.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.ReproveQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.ServiceItemRequestDTO;
import br.com.ofisy.application.quote.dto.StockItemRequestDTO;
import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import br.com.ofisy.domain.quote.QuoteRepository;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteDataException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import br.com.ofisy.integration.IntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
class QuoteIntegrationTest extends IntegrationTestBase {

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private QuoteRepository quoteRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ServiceCatalogRepository serviceCatalogRepository;

    @Autowired
    private ServiceOrderRepository serviceOrderRepository;

    @Autowired
    private ServiceOrderExecutionRepository serviceOrderExecutionRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID serviceOrderId;
    private UUID stockId;
    private UUID serviceOrderExecutionId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(
                User.create("mecanico@ofisy.com", "$2a$12$hash", "Mecânico Teste", Role.MECHANIC)
        );

        Customer customer = customerRepository.save(
                Customer.create(new CpfCnpj("52998224725"), "João Silva", "joao@teste.com", "11999999999")
        );

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(customer.getId(), new LicensePlate("TST1T23"), "Civic", "Honda", "Preto", 2022, null)
        );

        ServiceOrder serviceOrder = serviceOrderRepository.save(
                ServiceOrder.receive(vehicle.getId(), customer.getId(), "Veículo com problema no freio", user.getId())
        );
        serviceOrderId = serviceOrder.getId();

        Stock stock = stockRepository.save(
                Stock.create("Peça de Teste", "Peça exclusiva para testes de integração", 10, new BigDecimal("120.00"), "Testes", 2)
        );
        stockId = stock.getId();

        ServiceCatalog serviceCatalog = serviceCatalogRepository.save(
                ServiceCatalog.create("Serviço de Teste", "Serviço exclusivo para testes de integração", new BigDecimal("350.00"))
        );

        ServiceOrderExecution execution = serviceOrderExecutionRepository.save(
                ServiceOrderExecution.create(serviceCatalog.getId(), serviceOrderId)
        );
        serviceOrderExecutionId = execution.getId();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar orçamento com itens de estoque com sucesso")
        void shouldCreateQuoteWithStockItemsSuccessfully() {
            var request = new CreateQuoteRequestDTO(
                    serviceOrderId,
                    List.of(new StockItemRequestDTO(stockId, 2)),
                    List.of()
            );

            var response = quoteService.create(request);

            assertThat(response).isNotNull();
            assertThat(response.serviceOrderId()).isEqualTo(serviceOrderId);
            assertThat(response.status()).isEqualTo(QuoteStatus.PENDING);
            assertThat(response.totalPrice()).isEqualByComparingTo(new BigDecimal("240.00"));
            assertThat(response.stockItems()).hasSize(1);
            assertThat(response.serviceItems()).isEmpty();
        }

        @Test
        @DisplayName("Deve criar orçamento com itens de estoque e serviço com sucesso")
        void shouldCreateQuoteWithStockAndServiceItemsSuccessfully() {
            var request = new CreateQuoteRequestDTO(
                    serviceOrderId,
                    List.of(new StockItemRequestDTO(stockId, 1)),
                    List.of(new ServiceItemRequestDTO(serviceOrderExecutionId))
            );

            var response = quoteService.create(request);

            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(QuoteStatus.PENDING);
            assertThat(response.totalPrice()).isEqualByComparingTo(new BigDecimal("470.00"));
            assertThat(response.stockItems()).hasSize(1);
            assertThat(response.serviceItems()).hasSize(1);
        }

        @Test
        @DisplayName("Deve lançar exceção quando ambas as listas estão vazias")
        void shouldThrowExceptionWhenBothListsAreEmpty() {
            var request = new CreateQuoteRequestDTO(
                    serviceOrderId,
                    List.of(),
                    List.of()
            );

            assertThatThrownBy(() -> quoteService.create(request))
                    .isInstanceOf(InvalidQuoteDataException.class);
        }

        @Test
        @DisplayName("Deve consumir estoque ao criar orçamento")
        void shouldConsumeStockWhenCreatingQuote() {
            var quantityBefore = stockRepository.findById(stockId).orElseThrow().getQuantity();

            var request = new CreateQuoteRequestDTO(
                    serviceOrderId,
                    List.of(new StockItemRequestDTO(stockId, 2)),
                    List.of()
            );

            quoteService.create(request);

            var quantityAfter = stockRepository.findById(stockId).orElseThrow().getQuantity();
            assertThat(quantityAfter).isEqualTo(quantityBefore - 2);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar orçamento quando encontrado")
        void shouldReturnQuoteWhenFound() {
            var request = new CreateQuoteRequestDTO(
                    serviceOrderId,
                    List.of(new StockItemRequestDTO(stockId, 1)),
                    List.of()
            );
            var created = quoteService.create(request);

            var response = quoteService.findById(created.id());

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(created.id());
        }

        @Test
        @DisplayName("Deve lançar exceção quando orçamento não encontrado")
        void shouldThrowExceptionWhenQuoteNotFound() {
            assertThatThrownBy(() -> quoteService.findById(UUID.randomUUID()))
                    .isInstanceOf(QuoteNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findByServiceOrderId")
    class FindByServiceOrderId {

        @Test
        @DisplayName("Deve retornar orçamentos da ordem de serviço")
        void shouldReturnQuotesByServiceOrderId() {
            var request = new CreateQuoteRequestDTO(
                    serviceOrderId,
                    List.of(new StockItemRequestDTO(stockId, 1)),
                    List.of()
            );
            quoteService.create(request);

            var response = quoteService.findByServiceOrderId(serviceOrderId);

            assertThat(response).hasSize(1);
            assertThat(response.getFirst().serviceOrderId()).isEqualTo(serviceOrderId);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há orçamentos")
        void shouldReturnEmptyListWhenNoQuotes() {
            var response = quoteService.findByServiceOrderId(UUID.randomUUID());

            assertThat(response).isEmpty();
        }
    }

    @Nested
    @DisplayName("approve")
    class Approve {

        @Test
        @DisplayName("Deve aprovar orçamento pendente")
        void shouldApproveQuote() {
            var request = new CreateQuoteRequestDTO(
                    serviceOrderId,
                    List.of(new StockItemRequestDTO(stockId, 1)),
                    List.of()
            );
            var created = quoteService.create(request);

            var response = quoteService.approve(created.id());

            assertThat(response.status()).isEqualTo(QuoteStatus.APPROVED);
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao aprovar orçamento não pendente")
        void shouldThrowExceptionWhenApprovingNonPendingQuote() {
            var request = new CreateQuoteRequestDTO(
                    serviceOrderId,
                    List.of(new StockItemRequestDTO(stockId, 1)),
                    List.of()
            );
            var created = quoteService.create(request);
            quoteService.approve(created.id());

            assertThatThrownBy(() -> quoteService.approve(created.id()))
                    .isInstanceOf(InvalidQuoteStatusException.class);
        }
    }

    @Nested
    @DisplayName("reprove")
    class Reprove {

        @Test
        @DisplayName("Deve reprovar orçamento pendente com motivo")
        void shouldReproveQuoteWithReason() {
            var request = new CreateQuoteRequestDTO(
                    serviceOrderId,
                    List.of(new StockItemRequestDTO(stockId, 1)),
                    List.of()
            );
            var created = quoteService.create(request);

            var response = quoteService.reprove(created.id(), new ReproveQuoteRequestDTO("Valor muito alto"));

            assertThat(response.status()).isEqualTo(QuoteStatus.REPROVED);
            assertThat(response.quoteRefusalReason()).isEqualTo("Valor muito alto");
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao reprovar orçamento não pendente")
        void shouldThrowExceptionWhenReprovingNonPendingQuote() {
            var request = new CreateQuoteRequestDTO(
                    serviceOrderId,
                    List.of(new StockItemRequestDTO(stockId, 1)),
                    List.of()
            );
            var created = quoteService.create(request);
            quoteService.approve(created.id());

            assertThatThrownBy(() -> quoteService.reprove(created.id(), new ReproveQuoteRequestDTO("Motivo")))
                    .isInstanceOf(InvalidQuoteStatusException.class);
        }
    }
}
