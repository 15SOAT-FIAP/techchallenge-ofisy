package br.com.ofisy.application.serviceorder;

import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.quote.QuoteService;
import br.com.ofisy.application.quote.dto.*;
import br.com.ofisy.application.quote.exceptions.QuoteAlreadyExistsException;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.serviceorder.approvequote.ApproveServiceOrderQuoteUseCase;
import br.com.ofisy.application.serviceorder.cancel.CancelServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.create.CreateServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.delivertocustomer.DeliverToCustomerUseCase;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.serviceorder.generatequote.GenerateServiceOrderQuoteUseCase;
import br.com.ofisy.application.serviceorder.getstatus.GetServiceOrderStatusUseCase;
import br.com.ofisy.application.serviceorder.listreceived.ListReceivedServiceOrdersUseCase;
import br.com.ofisy.application.serviceorder.reprovequote.ReproveServiceOrderQuoteUseCase;
import br.com.ofisy.application.serviceorder.startdiagnostic.StartDiagnosticUseCase;
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
import br.com.ofisy.domain.serviceorder.ServiceOrder;
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
import br.com.ofisy.integration.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderServiceIT extends IntegrationTestBase {

    @Autowired
    private CreateServiceOrderUseCase createServiceOrderUseCase;
    @Autowired
    private CancelServiceOrderUseCase cancelServiceOrderUseCase;
    @Autowired
    private StartDiagnosticUseCase startDiagnosticUseCase;
    @Autowired
    private DeliverToCustomerUseCase deliverToCustomerUseCase;
    @Autowired
    private GetServiceOrderStatusUseCase getServiceOrderStatusUseCase;
    @Autowired
    private ListReceivedServiceOrdersUseCase listReceivedServiceOrdersUseCase;
    @Autowired
    private GenerateServiceOrderQuoteUseCase generateServiceOrderQuoteUseCase;
    @Autowired
    private ApproveServiceOrderQuoteUseCase approveServiceOrderQuoteUseCase;
    @Autowired
    private ReproveServiceOrderQuoteUseCase reproveServiceOrderQuoteUseCase;
    @Autowired
    private QuoteService quoteService;

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

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar ordem de serviço com sucesso")
        void shouldCreateServiceOrderSuccessfully() {
            ServiceOrder response = createServiceOrderUseCase.execute(validCommand("Relatório de teste da OS"));

            assertThat(response).isNotNull();
            assertThat(response.getVehicleId()).isEqualTo(vehicleId);
            assertThat(response.getCustomerId()).isEqualTo(customerId);
            assertThat(response.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
            assertThat(response.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não pertence ao cliente")
        void shouldThrowExceptionWhenVehicleNotOwnedByCustomer() {
            Customer other = customerDomainRepository.save(
                    Customer.create(new CpfCnpj("11144477735"), "Maria Svc IT", "maria.svc.it@ofisy.com", "11977777777")
            );

            assertThatThrownBy(() -> createServiceOrderUseCase.execute(
                    new CreateServiceOrderUseCase.CreateServiceOrderCommand(vehicleId, other.getId(), "Relatório", userEmail)))
                    .isInstanceOf(VehicleNotOwnedByCustomerException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado")
        void shouldThrowExceptionWhenCustomerNotFound() {
            assertThatThrownBy(() -> createServiceOrderUseCase.execute(
                    new CreateServiceOrderUseCase.CreateServiceOrderCommand(vehicleId, UUID.randomUUID(), "Relatório", userEmail)))
                    .isInstanceOf(CustomerNotFoundException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void shouldThrowExceptionWhenVehicleNotFound() {
            assertThatThrownBy(() -> createServiceOrderUseCase.execute(
                    new CreateServiceOrderUseCase.CreateServiceOrderCommand(UUID.randomUUID(), customerId, "Relatório", userEmail)))
                    .isInstanceOf(VehicleNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("startDiagnostic")
    class StartDiagnostic {

        @Test
        @DisplayName("Deve iniciar diagnóstico com sucesso")
        void shouldStartDiagnosticSuccessfully() {
            ServiceOrder created = createServiceOrderUseCase.execute(validCommand("Relatório de teste da OS"));
            ServiceOrder response = startDiagnosticUseCase.execute(created.getId());

            assertThat(response.getStatus()).isEqualTo(ServiceOrderStatus.IN_DIAGNOSTIC);
            assertThat(response.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao iniciar diagnóstico de OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            UUID randomId = UUID.randomUUID();
            assertThatThrownBy(() -> startDiagnosticUseCase.execute(randomId))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção ao iniciar diagnóstico com transição inválida")
        void shouldThrowExceptionWhenStatusTransitionIsInvalid() {
            ServiceOrder created = createServiceOrderUseCase.execute(validCommand("Relatório de teste da OS"));
            startDiagnosticUseCase.execute(created.getId());
            UUID id = created.getId();

            assertThatThrownBy(() -> startDiagnosticUseCase.execute(id))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);
        }
    }

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("Deve cancelar ordem de serviço com sucesso")
        void shouldCancelServiceOrderSuccessfully() {
            ServiceOrder created = createServiceOrderUseCase.execute(validCommand("Relatório de teste da OS"));
            ServiceOrder response = cancelServiceOrderUseCase.execute(created.getId());

            assertThat(response.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("Deve lançar exceção ao cancelar OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            UUID randomId = UUID.randomUUID();
            assertThatThrownBy(() -> cancelServiceOrderUseCase.execute(randomId))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getStatus")
    class GetStatus {

        @Test
        @DisplayName("Deve retornar status da ordem de serviço")
        void shouldReturnServiceOrderStatus() {
            ServiceOrder created = createServiceOrderUseCase.execute(validCommand("Relatório de teste da OS"));
            ServiceOrder response = getServiceOrderStatusUseCase.execute(created.getId());

            assertThat(response).isNotNull();
            assertThat(response.getId()).isEqualTo(created.getId());
            assertThat(response.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        }

        @Test
        @DisplayName("Deve lançar exceção quando OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            UUID randomId = UUID.randomUUID();
            assertThatThrownBy(() -> getServiceOrderStatusUseCase.execute(randomId))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("listReceived")
    class ListReceived {

        @Test
        @DisplayName("Deve retornar ordens de serviço recebidas")
        void shouldReturnReceivedServiceOrders() {
            createServiceOrderUseCase.execute(validCommand("Relatório de teste da OS"));

            assertThat(listReceivedServiceOrdersUseCase.execute(PageRequest.of(0, 10)).getContent())
                    .isNotEmpty()
                    .allMatch(so -> so.getStatus() == ServiceOrderStatus.RECEIVED);
        }
    }

    @Nested
    @DisplayName("deliverToCustomer")
    class DeliverToCustomer {

        @Test
        @DisplayName("Deve lançar exceção ao entregar OS com status inválido")
        void shouldThrowExceptionWhenDeliveringWithInvalidStatus() {
            ServiceOrder created = createServiceOrderUseCase.execute(validCommand("Relatório de teste da OS"));
            UUID id = created.getId();

            assertThatThrownBy(() -> deliverToCustomerUseCase.execute(id))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);
        }
    }

    @Nested
    @DisplayName("generateQuote")
    class GenerateQuote {

        @Test
        @DisplayName("Deve gerar orçamento e mudar status para AWAITING_APPROVAL")
        void shouldGenerateQuoteAndChangeStatus() {
            UUID serviceOrderId = createAndStartDiagnostic();
            UUID execId = createServiceOrderExecution(serviceOrderId);

            QuoteResponseDTO quote = generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(serviceOrderId, fullQuoteRequest(execId, 2)));

            assertThat(quote.serviceOrderId()).isEqualTo(serviceOrderId);
            assertThat(quote.status()).isEqualTo(QuoteStatus.PENDING);
            assertThat(getServiceOrderStatusUseCase.execute(serviceOrderId).getStatus())
                    .isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
        }

        @Test
        @DisplayName("Deve calcular total corretamente com peças e serviços")
        void shouldCalculateTotalCorrectly() {
            UUID serviceOrderId = createAndStartDiagnostic();
            UUID execId = createServiceOrderExecution(serviceOrderId);

            QuoteResponseDTO quote = generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(serviceOrderId, fullQuoteRequest(execId, 2)));

            assertThat(quote.stockItems()).hasSize(1);
            assertThat(quote.serviceItems()).hasSize(1);
            assertThat(quote.totalPrice()).isEqualByComparingTo(new BigDecimal("400.00"));
        }

        @Test
        @DisplayName("Deve criar notificação ao gerar orçamento")
        void shouldCreateNotificationWhenGeneratingQuote() {
            UUID serviceOrderId = createAndStartDiagnostic();
            QuoteResponseDTO quote = generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(serviceOrderId, stockOnlyQuoteRequest(1)));

            assertThat(notificationDomainRepository.findAll().stream()
                    .filter(n -> n.getType() == NotificationType.QUOTE_GENERATED)
                    .filter(n -> quote.id().equals(n.getQuoteId()))
                    .toList()).hasSize(1);
        }

        @Test
        @DisplayName("Deve lançar exceção quando ambas as listas estão vazias")
        void shouldThrowExceptionWhenBothListsAreEmpty() {
            UUID serviceOrderId = createAndStartDiagnostic();
            CreateQuoteRequestDTO quoteRequest = new CreateQuoteRequestDTO(List.of(), List.of());

            assertThatThrownBy(() -> generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(serviceOrderId, quoteRequest)))
                    .isInstanceOf(InvalidQuoteDataException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção ao gerar orçamento duplicado")
        void shouldThrowExceptionWhenQuoteAlreadyExists() {
            UUID serviceOrderId = createAndStartDiagnostic();
            CreateQuoteRequestDTO req = stockOnlyQuoteRequest(1);
            generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(serviceOrderId, req));

            assertThatThrownBy(() -> generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(serviceOrderId, req)))
                    .isInstanceOf(QuoteAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção ao gerar orçamento com item de estoque duplicado")
        void shouldThrowExceptionWhenDuplicateStockItem() {
            UUID serviceOrderId = createAndStartDiagnostic();
            CreateQuoteRequestDTO req = new CreateQuoteRequestDTO(
                    List.of(new StockItemRequestDTO(stockId, 1), new StockItemRequestDTO(stockId, 2)),
                    List.of());

            assertThatThrownBy(() -> generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(serviceOrderId, req)))
                    .isInstanceOf(QuoteItemAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Deve consumir estoque ao gerar orçamento")
        void shouldConsumeStockWhenGeneratingQuote() {
            UUID serviceOrderId = createAndStartDiagnostic();
            int before1 = stockRepository.findById(stockId).orElseThrow().getQuantity();
            int before2 = stockRepository.findById(stockId2).orElseThrow().getQuantity();

            generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(serviceOrderId, stockOnlyQuoteRequest(2)));

            assertThat(stockRepository.findById(stockId).orElseThrow().getQuantity()).isEqualTo(before1 - 2);
            assertThat(stockRepository.findById(stockId2).orElseThrow().getQuantity()).isEqualTo(before2 - 2);
        }

        @Test
        @DisplayName("Deve lançar exceção ao gerar orçamento de OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            UUID randomId = UUID.randomUUID();
            CreateQuoteRequestDTO stockOnlyRequest = stockOnlyQuoteRequest(1);

            assertThatThrownBy(() -> generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(randomId, stockOnlyRequest)))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("approveQuote")
    class ApproveQuote {

        @Test
        @DisplayName("Deve aprovar orçamento pendente")
        void shouldApproveQuote() {
            UUID serviceOrderId = createAndStartDiagnostic();
            QuoteResponseDTO quote = generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(serviceOrderId, stockOnlyQuoteRequest(1)));

            QuoteResponseDTO response = approveServiceOrderQuoteUseCase.execute(quote.id());

            assertThat(response.status()).isEqualTo(QuoteStatus.APPROVED);
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao aprovar orçamento não pendente")
        void shouldThrowExceptionWhenApprovingNonPendingQuote() {
            UUID serviceOrderId = createAndStartDiagnostic();
            QuoteResponseDTO quote = generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(serviceOrderId, stockOnlyQuoteRequest(1)));
            approveServiceOrderQuoteUseCase.execute(quote.id());
            UUID quoteId = quote.id();

            assertThatThrownBy(() -> approveServiceOrderQuoteUseCase.execute(quoteId))
                    .isInstanceOf(InvalidQuoteStatusException.class);
        }
    }

    @Nested
    @DisplayName("reproveQuote")
    class ReproveQuote {

        @Test
        @DisplayName("Deve reprovar orçamento pendente com motivo")
        void shouldReproveQuoteWithReason() {
            UUID serviceOrderId = createAndStartDiagnostic();
            UUID execId = createServiceOrderExecution(serviceOrderId);
            QuoteResponseDTO quote = generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(serviceOrderId, fullQuoteRequest(execId, 1)));

            QuoteResponseDTO response = reproveServiceOrderQuoteUseCase.execute(
                    new ReproveServiceOrderQuoteUseCase.ReproveQuoteCommand(
                            quote.id(), new ReproveQuoteRequestDTO("Outra oficina passou um orçamento mais em conta")));

            assertThat(response.status()).isEqualTo(QuoteStatus.REPROVED);
            assertThat(response.quoteRefusalReason()).isEqualTo("Outra oficina passou um orçamento mais em conta");
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao reprovar orçamento não pendente")
        void shouldThrowExceptionWhenReprovingNonPendingQuote() {
            UUID serviceOrderId = createAndStartDiagnostic();
            QuoteResponseDTO quote = generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(serviceOrderId, stockOnlyQuoteRequest(1)));
            approveServiceOrderQuoteUseCase.execute(quote.id());
            UUID quoteId = quote.id();
            ReproveQuoteRequestDTO reproveRequest = new ReproveQuoteRequestDTO("Motivo");

            assertThatThrownBy(() -> reproveServiceOrderQuoteUseCase.execute(
                    new ReproveServiceOrderQuoteUseCase.ReproveQuoteCommand(quoteId, reproveRequest)))
                    .isInstanceOf(InvalidQuoteStatusException.class);
        }
    }

    // --- helpers ---

    private CreateServiceOrderUseCase.CreateServiceOrderCommand validCommand(String report) {
        return new CreateServiceOrderUseCase.CreateServiceOrderCommand(vehicleId, customerId, report, userEmail);
    }

    private UUID createAndStartDiagnostic() {
        ServiceOrder created = createServiceOrderUseCase.execute(validCommand("Relatório de teste da OS"));
        startDiagnosticUseCase.execute(created.getId());
        return created.getId();
    }

    private UUID createServiceOrderExecution(UUID serviceOrderId) {
        return serviceOrderExecutionDomainRepository.save(
                ServiceOrderExecution.create(serviceCatalogId, serviceOrderId)
        ).getId();
    }

    private CreateQuoteRequestDTO stockOnlyQuoteRequest(int quantity) {
        return new CreateQuoteRequestDTO(
                List.of(new StockItemRequestDTO(stockId, quantity), new StockItemRequestDTO(stockId2, quantity)),
                List.of());
    }

    private CreateQuoteRequestDTO fullQuoteRequest(UUID execId, int quantity) {
        return new CreateQuoteRequestDTO(
                List.of(new StockItemRequestDTO(stockId, quantity)),
                List.of(new ServiceItemRequestDTO(execId)));
    }
}