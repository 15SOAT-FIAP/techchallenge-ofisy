package br.com.ofisy.application.serviceorder;

import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.quote.QuoteService;
import br.com.ofisy.application.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.ReproveQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.ServiceItemRequestDTO;
import br.com.ofisy.application.quote.dto.StockItemRequestDTO;
import br.com.ofisy.application.quote.exceptions.QuoteAlreadyExistsException;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
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
import br.com.ofisy.infrastructure.persistence.notification.JpaNotificationRepository;
import br.com.ofisy.infrastructure.persistence.quote.JpaQuoteRepository;
import br.com.ofisy.infrastructure.persistence.servicecatalog.JpaServiceCatalogRepository;
import br.com.ofisy.infrastructure.persistence.serviceorder.JpaServiceOrderRepository;
import br.com.ofisy.infrastructure.persistence.serviceorderexecution.JpaServiceOrderExecutionRepository;
import br.com.ofisy.infrastructure.persistence.stock.JpaStockRepository;
import br.com.ofisy.infrastructure.persistence.stockmovement.JpaStockMovementRepository;
import br.com.ofisy.infrastructure.persistence.user.JpaUserRepository;
import br.com.ofisy.infrastructure.persistence.vehicle.JpaVehicleRepository;
import br.com.ofisy.infrastructure.persistence.customer.JpaCustomerRepository;
import br.com.ofisy.integration.IntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderServiceIT extends IntegrationTestBase {

    @Autowired 
    private ServiceOrderService serviceOrderService;
    @Autowired 
    private QuoteService quoteService;

    @Autowired 
    private JpaServiceOrderRepository serviceOrderRepository;
    @Autowired 
    private JpaQuoteRepository quoteRepository;
    @Autowired 
    private JpaNotificationRepository notificationRepository;
    @Autowired 
    private JpaServiceOrderExecutionRepository serviceOrderExecutionRepository;
    @Autowired 
    private JpaUserRepository userRepository;
    @Autowired 
    private JpaCustomerRepository customerRepository;
    @Autowired 
    private JpaVehicleRepository vehicleRepository;
    @Autowired
    private JpaStockRepository stockRepository;
    @Autowired
    private JpaStockMovementRepository stockMovementRepository;
    @Autowired
    private JpaServiceCatalogRepository serviceCatalogRepository;

    @Autowired 
    private CustomerRepository customerDomainRepository;
    @Autowired 
    private VehicleRepository vehicleDomainRepository;
    @Autowired 
    private UserRepository userDomainRepository;
    @Autowired 
    private StockRepository stockDomainRepository;
    @Autowired 
    private ServiceCatalogRepository serviceCatalogDomainRepository;
    @Autowired 
    private ServiceOrderExecutionRepository serviceOrderExecutionDomainRepository;
    @Autowired 
    private NotificationRepository notificationDomainRepository;

    private UUID customerId;
    private UUID vehicleId;
    private UUID stockId;
    private UUID stockId2;
    private UUID serviceCatalogId;
    private String userEmail;

    @BeforeEach
    void setUp() {
        User user = userDomainRepository.save(
                User.create("mecanico.svc.it@ofisy.com", passwordEncoder.encode("Test@123"), "Mecânico SVC IT", Role.MECHANIC)
        );
        userEmail = user.getEmail().emailAddress();

        Customer customer = customerDomainRepository.save(
                Customer.create(new CpfCnpj("52998224725"), "João Svc IT", "joao.svc.it@ofisy.com", "11992222222")
        );
        customerId = customer.getId();

        Vehicle vehicle = vehicleDomainRepository.save(
                Vehicle.create(customerId, new LicensePlate("SVC1T01"), "Civic", "Honda", "Preto", 2022, null)
        );
        vehicleId = vehicle.getId();

        Stock stock1 = stockDomainRepository.save(
                Stock.create("Peça Svc IT 1", "Peça 1 para testes de service", 10, new BigDecimal("100.00"), "Testes", 2)
        );
        stockId = stock1.getId();

        Stock stock2 = stockDomainRepository.save(
                Stock.create("Peça Svc IT 2", "Peça 2 para testes de service", 10, new BigDecimal("200.00"), "Testes", 2)
        );
        stockId2 = stock2.getId();

        ServiceCatalog serviceCatalog = serviceCatalogDomainRepository.save(
                ServiceCatalog.create("Serviço Svc IT", "Serviço para testes de service", new BigDecimal("200.00"))
        );
        serviceCatalogId = serviceCatalog.getId();
    }

    @AfterEach
    void tearDown() {
        notificationRepository.deleteAll();
        quoteRepository.deleteAll();
        serviceOrderExecutionRepository.deleteAll();
        serviceOrderRepository.deleteAll();
        stockMovementRepository.deleteAll();
        stockRepository.deleteAll();
        serviceCatalogRepository.deleteAll();
        vehicleRepository.deleteAll();
        customerRepository.deleteAll();
        userRepository.deleteAll();
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
            assertThat(response.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não pertence ao cliente")
        void shouldThrowExceptionWhenVehicleNotOwnedByCustomer() {
            Customer other = customerDomainRepository.save(
                    Customer.create(new CpfCnpj("11144477735"), "Maria Svc IT", "maria.svc.it@ofisy.com", "11977777777")
            );
            var request = new ServiceOrderRequestDTO(vehicleId, other.getId(), "Relatório");

            assertThatThrownBy(() -> serviceOrderService.create(request, userEmail))
                    .isInstanceOf(VehicleNotOwnedByCustomerException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado")
        void shouldThrowExceptionWhenCustomerNotFound() {
            var request = new ServiceOrderRequestDTO(vehicleId, UUID.randomUUID(), "Relatório");

            assertThatThrownBy(() -> serviceOrderService.create(request, userEmail))
                    .isInstanceOf(CustomerNotFoundException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void shouldThrowExceptionWhenVehicleNotFound() {
            var request = new ServiceOrderRequestDTO(UUID.randomUUID(), customerId, "Relatório");

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
            assertThatThrownBy(() -> serviceOrderService.startDiagnostic(UUID.randomUUID()))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção ao iniciar diagnóstico com transição inválida")
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
        void shouldCancelServiceOrderSuccessfully() {
            var created = serviceOrderService.create(defaultRequest(), userEmail);
            var response = serviceOrderService.close(created.id());

            assertThat(response.status()).isEqualTo(ServiceOrderStatus.CANCELLED.name());
        }

        @Test
        @DisplayName("Deve lançar exceção ao cancelar OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            assertThatThrownBy(() -> serviceOrderService.close(UUID.randomUUID()))
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
            assertThatThrownBy(() -> serviceOrderService.getStatus(UUID.randomUUID()))
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
        @DisplayName("Deve gerar orçamento e mudar status para AWAITING_APPROVAL")
        void shouldGenerateQuoteAndChangeStatus() {
            var serviceOrderId = createAndStartDiagnostic();
            var execId = createServiceOrderExecution(serviceOrderId);

            var quote = serviceOrderService.generateQuote(serviceOrderId, fullQuoteRequest(serviceOrderId, execId, 2));

            assertThat(quote.serviceOrderId()).isEqualTo(serviceOrderId);
            assertThat(quote.status()).isEqualTo(QuoteStatus.PENDING);
            assertThat(serviceOrderService.getStatus(serviceOrderId).status()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
        }

        @Test
        @DisplayName("Deve calcular total corretamente com peças e serviços")
        void shouldCalculateTotalCorrectly() {
            var serviceOrderId = createAndStartDiagnostic();
            var execId = createServiceOrderExecution(serviceOrderId);

            var quote = serviceOrderService.generateQuote(serviceOrderId, fullQuoteRequest(serviceOrderId, execId, 2));

            assertThat(quote.stockItems()).hasSize(1);
            assertThat(quote.serviceItems()).hasSize(1);
            assertThat(quote.totalPrice()).isEqualByComparingTo(new BigDecimal("400.00"));
        }

        @Test
        @DisplayName("Deve criar notificação ao gerar orçamento")
        void shouldCreateNotificationWhenGeneratingQuote() {
            var serviceOrderId = createAndStartDiagnostic();
            var quote = serviceOrderService.generateQuote(serviceOrderId, stockOnlyQuoteRequest(serviceOrderId, 1));

            var notifications = notificationDomainRepository.findAll().stream()
                    .filter(n -> n.getType() == NotificationType.QUOTE_GENERATED)
                    .filter(n -> quote.id().equals(n.getQuoteId()))
                    .toList();

            assertThat(notifications).hasSize(1);
            assertThat(notifications.getFirst().getRead()).isFalse();
        }

        @Test
        @DisplayName("Deve lançar exceção quando ambas as listas estão vazias")
        void shouldThrowExceptionWhenBothListsAreEmpty() {
            var serviceOrderId = createAndStartDiagnostic();

            assertThatThrownBy(() -> serviceOrderService.generateQuote(serviceOrderId, new CreateQuoteRequestDTO(serviceOrderId, List.of(), List.of())))
                    .isInstanceOf(InvalidQuoteDataException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção ao gerar orçamento duplicado")
        void shouldThrowExceptionWhenQuoteAlreadyExists() {
            var serviceOrderId = createAndStartDiagnostic();
            var req = stockOnlyQuoteRequest(serviceOrderId, 1);
            serviceOrderService.generateQuote(serviceOrderId, req);

            assertThatThrownBy(() -> serviceOrderService.generateQuote(serviceOrderId, req))
                    .isInstanceOf(QuoteAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção ao gerar orçamento com item de estoque duplicado")
        void shouldThrowExceptionWhenDuplicateStockItem() {
            var serviceOrderId = createAndStartDiagnostic();
            var req = new CreateQuoteRequestDTO(serviceOrderId,
                    List.of(new StockItemRequestDTO(stockId, 1), new StockItemRequestDTO(stockId, 2)),
                    List.of());

            assertThatThrownBy(() -> serviceOrderService.generateQuote(serviceOrderId, req))
                    .isInstanceOf(QuoteItemAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Deve consumir estoque ao gerar orçamento")
        void shouldConsumeStockWhenGeneratingQuote() {
            var serviceOrderId = createAndStartDiagnostic();
            int before1 = stockRepository.findById(stockId).orElseThrow().getQuantity();
            int before2 = stockRepository.findById(stockId2).orElseThrow().getQuantity();

            serviceOrderService.generateQuote(serviceOrderId, stockOnlyQuoteRequest(serviceOrderId, 2));

            assertThat(stockRepository.findById(stockId).orElseThrow().getQuantity()).isEqualTo(before1 - 2);
            assertThat(stockRepository.findById(stockId2).orElseThrow().getQuantity()).isEqualTo(before2 - 2);
        }

        @Test
        @DisplayName("Deve lançar exceção ao gerar orçamento de OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            var randomId = UUID.randomUUID();

            assertThatThrownBy(() -> serviceOrderService.generateQuote(randomId, stockOnlyQuoteRequest(randomId, 1)))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("approveQuote")
    class ApproveQuote {

        @Test
        @DisplayName("Deve aprovar orçamento pendente")
        void shouldApproveQuote() {
            var serviceOrderId = createAndStartDiagnostic();
            var quote = serviceOrderService.generateQuote(serviceOrderId, stockOnlyQuoteRequest(serviceOrderId, 1));

            var response = serviceOrderService.approveQuote(quote.id());

            assertThat(response.status()).isEqualTo(QuoteStatus.APPROVED);
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao aprovar orçamento não pendente")
        void shouldThrowExceptionWhenApprovingNonPendingQuote() {
            var serviceOrderId = createAndStartDiagnostic();
            var quote = serviceOrderService.generateQuote(serviceOrderId, stockOnlyQuoteRequest(serviceOrderId, 1));
            serviceOrderService.approveQuote(quote.id());
            UUID quoteId = quote.id();

            assertThatThrownBy(() -> serviceOrderService.approveQuote(quoteId))
                    .isInstanceOf(InvalidQuoteStatusException.class);
        }
    }

    @Nested
    @DisplayName("reproveQuote")
    class ReproveQuote {

        @Test
        @DisplayName("Deve reprovar orçamento pendente com motivo")
        void shouldReproveQuoteWithReason() {
            var serviceOrderId = createAndStartDiagnostic();
            var execId = createServiceOrderExecution(serviceOrderId);
            var quote = serviceOrderService.generateQuote(serviceOrderId, fullQuoteRequest(serviceOrderId, execId, 1));

            var response = serviceOrderService.reproveQuote(quote.id(), new ReproveQuoteRequestDTO("Outra oficina passou um orçamento mais em conta"));

            assertThat(response.status()).isEqualTo(QuoteStatus.REPROVED);
            assertThat(response.quoteRefusalReason()).isEqualTo("Outra oficina passou um orçamento mais em conta");
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao reprovar orçamento não pendente")
        void shouldThrowExceptionWhenReprovingNonPendingQuote() {
            var serviceOrderId = createAndStartDiagnostic();
            var quote = serviceOrderService.generateQuote(serviceOrderId, stockOnlyQuoteRequest(serviceOrderId, 1));
            serviceOrderService.approveQuote(quote.id());
            UUID quoteId = quote.id();

            assertThatThrownBy(() -> serviceOrderService.reproveQuote(quoteId, new ReproveQuoteRequestDTO("Motivo")))
                    .isInstanceOf(InvalidQuoteStatusException.class);
        }
    }

    // --- helpers ---

    private ServiceOrderRequestDTO defaultRequest() {
        return new ServiceOrderRequestDTO(vehicleId, customerId, "Relatório de teste da OS");
    }

    private UUID createAndStartDiagnostic() {
        var created = serviceOrderService.create(defaultRequest(), userEmail);
        serviceOrderService.startDiagnostic(created.id());
        return created.id();
    }

    private UUID createServiceOrderExecution(UUID serviceOrderId) {
        return serviceOrderExecutionDomainRepository.save(
                ServiceOrderExecution.create(serviceCatalogId, serviceOrderId)
        ).getId();
    }

    private CreateQuoteRequestDTO stockOnlyQuoteRequest(UUID serviceOrderId, int quantity) {
        return new CreateQuoteRequestDTO(serviceOrderId,
                List.of(new StockItemRequestDTO(stockId, quantity), new StockItemRequestDTO(stockId2, quantity)),
                List.of());
    }

    private CreateQuoteRequestDTO fullQuoteRequest(UUID serviceOrderId, UUID execId, int quantity) {
        return new CreateQuoteRequestDTO(serviceOrderId,
                List.of(new StockItemRequestDTO(stockId, quantity)),
                List.of(new ServiceItemRequestDTO(execId)));
    }
}