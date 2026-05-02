package br.com.ofisy.integration;

import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.quote.QuoteService;
import br.com.ofisy.application.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.ReproveQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.ServiceItemRequestDTO;
import br.com.ofisy.application.quote.dto.StockItemRequestDTO;
import br.com.ofisy.application.quote.exceptions.QuoteAlreadyExistsException;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.quote.exceptions.QuoteNotFoundException;
import br.com.ofisy.application.serviceorder.ServiceOrderService;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteDataException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
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
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Tag("integration")
class ServiceOrderIntegrationTest extends IntegrationTestBase {

    public static final String PRICE_300 = "300.00";
    public static final String PRICE_100 = "100.00";
    public static final String PRICE_200 = "200.00";
    public static final String PRICE_400 = "400.0";

    @Autowired
    private ServiceOrderService serviceOrderService;

    @Autowired
    private QuoteService quoteService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ServiceCatalogRepository serviceCatalogRepository;

    @Autowired
    private ServiceOrderExecutionRepository serviceOrderExecutionRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    private UUID customerId;
    private UUID vehicleId;
    private UUID stockId;
    private UUID stockId2;
    private UUID serviceCatalogId;
    private String userEmail;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(
                User.create("mecanico.teste@ofisy.com", "$2a$12$hash", "Mecânico Teste", Role.MECHANIC)
        );
        userEmail = user.getEmail().emailAddress();

        Customer customer = customerRepository.save(
                Customer.create(new CpfCnpj("52998224725"), "João Silva Teste", "joao.teste@ofisy.com", "11999999999")
        );
        customerId = customer.getId();

        Vehicle vehicle = vehicleRepository.save(
                Vehicle.create(customerId, new LicensePlate("TST1T23"), "Civic", "Honda", "Preto", 2022, null)
        );
        vehicleId = vehicle.getId();

        Stock stock = stockRepository.save(
                Stock.create("Peça OS Teste", "Peça para testes de OS", 10, new BigDecimal(PRICE_100), "Testes", 2)
        );
        stockId = stock.getId();

        Stock stock2 = stockRepository.save(
                Stock.create("Peça OS Teste 2", "Peça 2 para testes de OS", 10, new BigDecimal(PRICE_200), "Testes", 2)
        );
        stockId2 = stock2.getId();

        ServiceCatalog serviceCatalog = serviceCatalogRepository.save(
                ServiceCatalog.create("Serviço OS Teste", "Serviço para testes de OS", new BigDecimal(PRICE_200))
        );
        serviceCatalogId = serviceCatalog.getId();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar ordem de serviço com sucesso")
        void shouldCreateServiceOrderSuccessfully() {
            var response = serviceOrderService.create(defaultRequest(), userEmail);

            assertThat(response).isNotNull();
            assertThat(response.vehicleId()).isEqualTo(vehicleId);
            assertThat(response.customerId()).isEqualTo(customerId);
            assertThat(response.status()).isEqualTo(ServiceOrderStatus.RECEIVED.name());
            assertThat(response.report()).isEqualTo("Relatório de teste da OS");
            assertThat(response.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não pertence ao cliente")
        void shouldThrowExceptionWhenVehicleNotOwnedByCustomer() {
            Customer otherCustomer = customerRepository.save(
                    Customer.create(new CpfCnpj("11144477735"), "Maria Teste", "maria.teste@ofisy.com", "11988888888")
            );
            var request = new ServiceOrderRequestDTO(vehicleId, otherCustomer.getId(), "Relatório de teste da OS");

            assertThatThrownBy(() -> serviceOrderService.create(request, userEmail))
                    .isInstanceOf(VehicleNotOwnedByCustomerException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado")
        void shouldThrowExceptionWhenCustomerNotFound() {
            var request = new ServiceOrderRequestDTO(vehicleId, UUID.randomUUID(), "Relatório de teste da OS");

            assertThatThrownBy(() -> serviceOrderService.create(request, userEmail))
                    .isInstanceOf(CustomerNotFoundException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void shouldThrowExceptionWhenVehicleNotFound() {
            var request = new ServiceOrderRequestDTO(UUID.randomUUID(), customerId, "Relatório de teste da OS");

            assertThatThrownBy(() -> serviceOrderService.create(request, userEmail))
                    .isInstanceOf(VehicleNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("startDiagnostic")
    class StartDiagnostic {

        @Test
        @DisplayName("Deve iniciar diagnóstico com sucesso")
        void shouldStartDiagnosticSuccessfully() {
            var created = serviceOrderService.create(defaultRequest(), userEmail);
            var response = serviceOrderService.startDiagnostic(created.id());

            assertThat(response.status()).isEqualTo(ServiceOrderStatus.IN_DIAGNOSTIC.name());
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao iniciar diagnóstico de OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            UUID quoteId = UUID.randomUUID();
            assertThatThrownBy(() -> serviceOrderService.startDiagnostic(quoteId))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção ao iniciar diagnóstico de OS com status inválido")
        void shouldThrowExceptionWhenStatusTransitionIsInvalid() {
            var created = serviceOrderService.create(defaultRequest(), userEmail);
            serviceOrderService.startDiagnostic(created.id());
            UUID id = created.id();

            assertThatThrownBy(() -> serviceOrderService.startDiagnostic(id))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);
        }
    }

    @Nested
    @DisplayName("close")
    class Close {

        @Test
        @DisplayName("Deve cancelar ordem de serviço com sucesso")
        void shouldCloseServiceOrderSuccessfully() {
            var created = serviceOrderService.create(defaultRequest(), userEmail);
            var response = serviceOrderService.close(created.id());

            assertThat(response.status()).isEqualTo(ServiceOrderStatus.CANCELLED.name());
        }

        @Test
        @DisplayName("Deve lançar exceção ao cancelar OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            UUID quoteId = UUID.randomUUID();
            assertThatThrownBy(() -> serviceOrderService.close(quoteId))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getStatus")
    class GetStatus {

        @Test
        @DisplayName("Deve retornar status da ordem de serviço")
        void shouldReturnServiceOrderStatus() {
            var created = serviceOrderService.create(defaultRequest(), userEmail);

            var response = serviceOrderService.getStatus(created.id());

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(created.id());
            assertThat(response.status()).isEqualTo(ServiceOrderStatus.RECEIVED);
        }

        @Test
        @DisplayName("Deve lançar exceção quando OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            UUID quoteId = UUID.randomUUID();
            assertThatThrownBy(() -> serviceOrderService.getStatus(quoteId))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("listReceived")
    class ListReceived {

        @Test
        @DisplayName("Deve retornar ordens de serviço recebidas")
        void shouldReturnReceivedServiceOrders() {
            serviceOrderService.create(defaultRequest(), userEmail);

            var response = serviceOrderService.listReceived(PageRequest.of(0, 10));

            assertThat(response.getContent()).isNotEmpty();
            assertThat(response.getContent()).allMatch(so -> so.status().equals(ServiceOrderStatus.RECEIVED.name()));
        }
    }

    @Nested
    @DisplayName("deliverToCustomer")
    class DeliverToCustomer {

        @Test
        @DisplayName("Deve lançar exceção ao entregar OS com status inválido")
        void shouldThrowExceptionWhenDeliveringWithInvalidStatus() {
            var created = serviceOrderService.create(defaultRequest(), userEmail);
            UUID id = created.id();

            assertThatThrownBy(() -> serviceOrderService.deliverToCustomer(id))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);
        }
    }

    @Nested
    @DisplayName("generateQuote")
    class GenerateQuote {

        @Test
        @DisplayName("Deve gerar orçamento completo e mudar status para AWAITING_APPROVAL")
        void shouldGenerateQuoteWithStockItemsAndChangeStatus() {
            UUID osId = createAndStartDiagnostic();
            UUID serviceOrderExecutionId = generateServiceOrderExecution();

            var request = fullQuoteRequest(osId, serviceOrderExecutionId, 2);

            var quote = serviceOrderService.generateQuote(osId, request);

            assertThat(quote).isNotNull();
            assertThat(quote.serviceOrderId()).isEqualTo(osId);
            assertThat(quote.status()).isEqualTo(QuoteStatus.PENDING);
            assertThat(quote.stockItems()).hasSize(1);

            var status = serviceOrderService.getStatus(osId);
            assertThat(status.status()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
        }

        @Test
        @DisplayName("Deve gerar orçamento com itens de estoque e serviço e validar o total completo da OS")
        void shouldGenerateQuoteWithStockAndServiceItems() {
            UUID osId = createAndStartDiagnostic();
            UUID serviceOrderExecutionId = generateServiceOrderExecution();

            var quote = serviceOrderService.generateQuote(osId, fullQuoteRequest(osId, serviceOrderExecutionId, 2));

            assertThat(quote.stockItems()).hasSize(1);
            assertThat(quote.serviceItems()).hasSize(1);
            assertThat(quote.totalPrice()).isEqualByComparingTo(new BigDecimal(PRICE_400));
        }

        @Test
        @DisplayName("Deve criar notificação ao gerar orçamento")
        void shouldCreateNotificationWhenGeneratingQuote() {
            UUID osId = createAndStartDiagnostic();
            var request = stockOnlyQuoteRequest(osId, 1);

            var quote = serviceOrderService.generateQuote(osId, request);

            var notifications = notificationRepository.findAll().stream()
                    .filter(n -> n.getType() == NotificationType.QUOTE_GENERATED)
                    .filter(n -> quote.id().equals(n.getQuoteId()))
                    .toList();

            assertThat(notifications).hasSize(1);
            assertThat(notifications.getFirst().getRead()).isFalse();
        }

        @Test
        @DisplayName("Deve lançar exceção quando ambas as listas estão vazias")
        void shouldThrowExceptionWhenBothListsAreEmpty() {
            UUID osId = createAndStartDiagnostic();
            var request = new CreateQuoteRequestDTO(osId, List.of(), List.of());

            assertThatThrownBy(() -> serviceOrderService.generateQuote(osId, request))
                    .isInstanceOf(InvalidQuoteDataException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção ao gerar orçamento duplicado para a OS")
        void shouldThrowExceptionWhenQuoteAlreadyExists() {
            UUID osId = createAndStartDiagnostic();

            CreateQuoteRequestDTO quoteRequestDTO = stockOnlyQuoteRequest(osId, 1);
            serviceOrderService.generateQuote(osId, quoteRequestDTO);

            assertThatThrownBy(() -> serviceOrderService.generateQuote(osId, quoteRequestDTO))
                    .isInstanceOf(QuoteAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando item de estoque duplicado")
        void shouldThrowExceptionWhenDuplicateStockItem() {
            UUID osId = createAndStartDiagnostic();
            var request = new CreateQuoteRequestDTO(
                    osId,
                    List.of(
                            new StockItemRequestDTO(stockId, 1),
                            new StockItemRequestDTO(stockId, 2)
                    ),
                    List.of()
            );

            assertThatThrownBy(() -> serviceOrderService.generateQuote(osId, request))
                    .isInstanceOf(QuoteItemAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Deve consumir estoque ao gerar orçamento")
        void shouldConsumeStockWhenGeneratingQuote() {
            UUID osId = createAndStartDiagnostic();
            var stock1QuantityBefore = stockRepository.findById(stockId).orElseThrow().getQuantity();
            var stock2QuantityBefore = stockRepository.findById(stockId2).orElseThrow().getQuantity();

            serviceOrderService.generateQuote(osId, stockOnlyQuoteRequest(osId, 2));

            var stock1QuantityAfter = stockRepository.findById(stockId).orElseThrow().getQuantity();
            var stock2QuantityAfter = stockRepository.findById(stockId2).orElseThrow().getQuantity();
            assertThat(stock1QuantityAfter).isEqualTo(stock1QuantityBefore - 2);
            assertThat(stock2QuantityAfter).isEqualTo(stock2QuantityBefore - 2);
        }

        @Test
        @DisplayName("Deve lançar exceção ao gerar orçamento de OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            UUID randomId = UUID.randomUUID();
            var request = stockOnlyQuoteRequest(randomId, 1);

            assertThatThrownBy(() -> serviceOrderService.generateQuote(randomId, request))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("approveQuote")
    class ApproveQuote {

        @Test
        @DisplayName("Deve aprovar orçamento pendente")
        void shouldApproveQuote() {
            UUID osId = createAndStartDiagnostic();
            var quote = serviceOrderService.generateQuote(osId, stockOnlyQuoteRequest(osId, 1));

            var response = quoteService.approve(quote.id());

            assertThat(response.status()).isEqualTo(QuoteStatus.APPROVED);
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao aprovar orçamento não pendente")
        void shouldThrowExceptionWhenApprovingNonPendingQuote() {
            UUID osId = createAndStartDiagnostic();
            var quote = serviceOrderService.generateQuote(osId, stockOnlyQuoteRequest(osId, 1));
            quoteService.approve(quote.id());
            UUID quoteId = quote.id();

            assertThatThrownBy(() -> quoteService.approve(quoteId))
                    .isInstanceOf(InvalidQuoteStatusException.class);
        }
    }

    @Nested
    @DisplayName("reproveQuote")
    class ReproveQuote {

        @Test
        @DisplayName("Deve reprovar orçamento pendente com motivo")
        void shouldReproveQuoteWithReason() {
            UUID osId = createAndStartDiagnostic();
            UUID serviceOrderExecutionId = generateServiceOrderExecution();

            var quote = serviceOrderService.generateQuote(osId, fullQuoteRequest(osId, serviceOrderExecutionId, 1));

            var response = quoteService.reprove(quote.id(), new ReproveQuoteRequestDTO("Outra oficina passou um orçamento mais em conta"));

            assertThat(response.status()).isEqualTo(QuoteStatus.REPROVED);
            assertThat(response.quoteRefusalReason()).isEqualTo("Outra oficina passou um orçamento mais em conta");
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao reprovar orçamento não pendente")
        void shouldThrowExceptionWhenReprovingNonPendingQuote() {
            UUID osId = createAndStartDiagnostic();
            UUID serviceOrderExecutionId = generateServiceOrderExecution();

            var quote = serviceOrderService.generateQuote(osId, fullQuoteRequest(osId, serviceOrderExecutionId, 1));
            quoteService.approve(quote.id());

            UUID quoteId = quote.id();
            ReproveQuoteRequestDTO reproveQuoteRequestDTO = new ReproveQuoteRequestDTO("Achei o valor do orçamento muito alto para meu bolso");
            assertThatThrownBy(() -> quoteService.reprove(quoteId, reproveQuoteRequestDTO))
                    .isInstanceOf(InvalidQuoteStatusException.class);
        }
    }

    @Nested
    @DisplayName("findQuote")
    class FindQuote {

        @Test
        @DisplayName("Deve retornar orçamento por ID")
        void shouldReturnQuoteById() {
            UUID osId = createAndStartDiagnostic();
            var quote = serviceOrderService.generateQuote(osId, stockOnlyQuoteRequest(osId, 1));

            var response = quoteService.findById(quote.id());

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(quote.id());
            assertThat(response.serviceOrderId()).isEqualTo(osId);
        }

        @Test
        @DisplayName("Deve lançar exceção quando orçamento não encontrado")
        void shouldThrowExceptionWhenQuoteNotFound() {
            UUID quoteId = UUID.randomUUID();
            assertThatThrownBy(() -> quoteService.findById(quoteId))
                    .isInstanceOf(QuoteNotFoundException.class);
        }

        @Test
        @DisplayName("Deve retornar orçamento por OS")
        void shouldReturnQuoteByServiceOrderId() {
            UUID osId = createAndStartDiagnostic();
            serviceOrderService.generateQuote(osId, stockOnlyQuoteRequest(osId, 1));

            var response = quoteService.findByServiceOrderId(osId);

            assertThat(response).hasSize(1);
            assertThat(response.getFirst().serviceOrderId()).isEqualTo(osId);
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há orçamentos para a OS")
        void shouldReturnEmptyListWhenNoQuotes() {
            var response = quoteService.findByServiceOrderId(UUID.randomUUID());

            assertThat(response).isEmpty();
        }
    }

    private ServiceOrderRequestDTO defaultRequest() {
        return new ServiceOrderRequestDTO(vehicleId, customerId, "Relatório de teste da OS");
    }

    private UUID createAndStartDiagnostic() {
        var created = serviceOrderService.create(defaultRequest(), userEmail);
        serviceOrderService.startDiagnostic(created.id());
        return created.id();
    }

    private CreateQuoteRequestDTO stockOnlyQuoteRequest(UUID serviceOrderId, int quantity) {
        return new CreateQuoteRequestDTO(
                serviceOrderId,
                List.of(new StockItemRequestDTO(stockId, quantity), new StockItemRequestDTO(stockId2, quantity)),
                List.of()
        );
    }

    private CreateQuoteRequestDTO fullQuoteRequest(UUID serviceOrderId, UUID serviceOrderExecutionId, int quantity) {
        return new CreateQuoteRequestDTO(
                serviceOrderId,
                List.of(new StockItemRequestDTO(stockId, quantity)),
                List.of(new ServiceItemRequestDTO(serviceOrderExecutionId))
        );
    }

    private UUID generateServiceOrderExecution() {
        ServiceOrderExecution execution = serviceOrderExecutionRepository.save(
                ServiceOrderExecution.create(serviceCatalogId, UUID.randomUUID())
        );

        return execution.getId();
    }
}
