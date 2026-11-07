package br.com.ofisy.application.serviceorder;

import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.quote.create.CreateQuoteUseCase;
import br.com.ofisy.application.quote.exceptions.QuoteAlreadyExistsException;
import br.com.ofisy.application.quote.exceptions.QuoteItemAlreadyExistsException;
import br.com.ofisy.application.quote.findbyserviceorderid.FindQuoteByServiceOrderIdUseCase;
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
import br.com.ofisy.domain.quote.Quote;
import br.com.ofisy.domain.quote.QuoteStatus;
import br.com.ofisy.domain.quote.QuoteStockItem;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteDataException;
import br.com.ofisy.domain.quote.exceptions.InvalidQuoteStatusException;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrder;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
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
import static org.assertj.core.api.Assertions.tuple;

class ServiceOrderServiceIT extends IntegrationTestBase {

    @Autowired private CreateServiceOrderUseCase createServiceOrderUseCase;
    @Autowired private CancelServiceOrderUseCase cancelServiceOrderUseCase;
    @Autowired private StartDiagnosticUseCase startDiagnosticUseCase;
    @Autowired private DeliverToCustomerUseCase deliverToCustomerUseCase;
    @Autowired private GetServiceOrderStatusUseCase getServiceOrderStatusUseCase;
    @Autowired private ListReceivedServiceOrdersUseCase listReceivedServiceOrdersUseCase;
    @Autowired private ApproveServiceOrderQuoteUseCase approveServiceOrderQuoteUseCase;
    @Autowired private ReproveServiceOrderQuoteUseCase reproveServiceOrderQuoteUseCase;
    @Autowired private GenerateServiceOrderQuoteUseCase generateServiceOrderQuoteUseCase;
    @Autowired private FindQuoteByServiceOrderIdUseCase findQuoteByServiceOrderIdUseCase;

    @Autowired private CustomerRepository customerDomainRepository;
    @Autowired private VehicleRepository vehicleDomainRepository;
    @Autowired private UserRepository userDomainRepository;
    @Autowired private ServiceCatalogRepository serviceCatalogDomainRepository;
    @Autowired private NotificationRepository notificationDomainRepository;

    private UUID customerId;
    private UUID vehicleId;
    private final UUID stockId = UUID.randomUUID();
    private final UUID stockId2 = UUID.randomUUID();
    private UUID serviceCatalogId;
    private String userEmail;

    @BeforeEach
    void setUp() {
        User user = userDomainRepository.save(
                User.create("mecanico.svc.it@ofisy.com", passwordEncoder.encode("Test@123"), "Mecânico SVC IT", Role.MECHANIC));
        userEmail = user.getEmail().emailAddress();

        Customer customer = customerDomainRepository.save(
                Customer.create(new CpfCnpj("52998224725"), "João Svc IT", "joao.svc.it@ofisy.com", "11992222222"));
        customerId = customer.getId();

        Vehicle vehicle = vehicleDomainRepository.save(
                Vehicle.create(customerId, new LicensePlate("SVC1T01"), "Civic", "Honda", "Preto", 2022, null));
        vehicleId = vehicle.getId();

        ServiceCatalog serviceCatalog = serviceCatalogDomainRepository.save(
                ServiceCatalog.create("Serviço Svc IT", "Serviço para testes de service", new BigDecimal("200.00")));
        serviceCatalogId = serviceCatalog.getId();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar ordem de serviço sem itens com status RECEIVED")
        void shouldCreateServiceOrderSuccessfully() {
            ServiceOrder response = createServiceOrderUseCase.execute(validCommand());

            assertThat(response.getVehicleId()).isEqualTo(vehicleId);
            assertThat(response.getCustomerId()).isEqualTo(customerId);
            assertThat(response.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
            assertThat(response.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não pertence ao cliente")
        void shouldThrowExceptionWhenVehicleNotOwnedByCustomer() {
            Customer other = customerDomainRepository.save(
                    Customer.create(new CpfCnpj("11144477735"), "Maria Svc IT", "maria.svc.it@ofisy.com", "11977777777"));

            assertThatThrownBy(() -> createServiceOrderUseCase.execute(
                    new CreateServiceOrderUseCase.CreateServiceOrderCommand(
                            vehicleId, other.getId(), "Relatório", userEmail)))
                    .isInstanceOf(VehicleNotOwnedByCustomerException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado")
        void shouldThrowExceptionWhenCustomerNotFound() {
            assertThatThrownBy(() -> createServiceOrderUseCase.execute(
                    new CreateServiceOrderUseCase.CreateServiceOrderCommand(
                            vehicleId, UUID.randomUUID(), "Relatório", userEmail)))
                    .isInstanceOf(CustomerNotFoundException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void shouldThrowExceptionWhenVehicleNotFound() {
            assertThatThrownBy(() -> createServiceOrderUseCase.execute(
                    new CreateServiceOrderUseCase.CreateServiceOrderCommand(
                            UUID.randomUUID(), customerId, "Relatório", userEmail)))
                    .isInstanceOf(VehicleNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("startDiagnostic")
    class StartDiagnostic {

        @Test
        @DisplayName("Deve iniciar diagnóstico com sucesso")
        void shouldStartDiagnosticSuccessfully() {
            ServiceOrder created = createServiceOrderUseCase.execute(validCommand());
            ServiceOrder response = startDiagnosticUseCase.execute(created.getId());

            assertThat(response.getStatus()).isEqualTo(ServiceOrderStatus.IN_DIAGNOSTIC);
            assertThat(response.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao iniciar diagnóstico de OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            assertThatThrownBy(() -> startDiagnosticUseCase.execute(UUID.randomUUID()))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção ao iniciar diagnóstico com transição inválida")
        void shouldThrowExceptionWhenStatusTransitionIsInvalid() {
            ServiceOrder created = createServiceOrderUseCase.execute(validCommand());
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
            ServiceOrder created = createServiceOrderUseCase.execute(validCommand());
            ServiceOrder response = cancelServiceOrderUseCase.execute(created.getId());

            assertThat(response.getStatus()).isEqualTo(ServiceOrderStatus.CANCELLED);
        }

        @Test
        @DisplayName("Deve lançar exceção ao cancelar OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            assertThatThrownBy(() -> cancelServiceOrderUseCase.execute(UUID.randomUUID()))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("getStatus")
    class GetStatus {

        @Test
        @DisplayName("Deve retornar status da ordem de serviço")
        void shouldReturnServiceOrderStatus() {
            ServiceOrder created = createServiceOrderUseCase.execute(validCommand());
            ServiceOrder response = getServiceOrderStatusUseCase.execute(created.getId());

            assertThat(response.getId()).isEqualTo(created.getId());
            assertThat(response.getStatus()).isEqualTo(ServiceOrderStatus.RECEIVED);
        }

        @Test
        @DisplayName("Deve lançar exceção quando OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            assertThatThrownBy(() -> getServiceOrderStatusUseCase.execute(UUID.randomUUID()))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("listReceived")
    class ListReceived {

        @Test
        @DisplayName("Deve retornar ordens de serviço recebidas")
        void shouldReturnReceivedServiceOrders() {
            createServiceOrderUseCase.execute(validCommand());

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
            ServiceOrder created = createServiceOrderUseCase.execute(validCommand());
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

            generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                            serviceOrderId, stockOnlyCommands(2), List.of()));

            assertThat(getServiceOrderStatusUseCase.execute(serviceOrderId).getStatus())
                    .isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
        }

        @Test
        @DisplayName("Deve calcular total corretamente com peças e serviços")
        void shouldCalculateTotalCorrectly() {
            UUID serviceOrderId = createAndStartDiagnostic();

            Quote quote = generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                            serviceOrderId,
                            List.of(stockItem(stockId, 2)),
                            serviceItemCommands()));

            assertThat(quote.getStockItems()).hasSize(1);
            assertThat(quote.getServiceItems()).hasSize(1);
            assertThat(quote.getTotalPrice()).isEqualByComparingTo(new BigDecimal("400.00"));
        }

        @Test
        @DisplayName("Deve criar notificação ao gerar orçamento")
        void shouldCreateNotificationWhenGeneratingQuote() {
            UUID serviceOrderId = createAndStartDiagnostic();
            Quote quote = generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                            serviceOrderId, stockOnlyCommands(1), List.of()));

            assertThat(notificationDomainRepository.findAll().stream()
                    .filter(n -> n.getType() == NotificationType.QUOTE_GENERATED)
                    .filter(n -> quote.getId().equals(n.getQuoteId()))
                    .toList()).hasSize(1);
        }

        @Test
        @DisplayName("Deve lançar exceção quando ambas as listas estão vazias")
        void shouldThrowExceptionWhenBothListsAreEmpty() {
            UUID serviceOrderId = createAndStartDiagnostic();

            assertThatThrownBy(() -> generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                            serviceOrderId, List.of(), List.of())))
                    .isInstanceOf(InvalidQuoteDataException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção ao gerar orçamento duplicado")
        void shouldThrowExceptionWhenQuoteAlreadyExists() {
            UUID serviceOrderId = createAndStartDiagnostic();
            var cmd = new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                    serviceOrderId, stockOnlyCommands(1), List.of());
            generateServiceOrderQuoteUseCase.execute(cmd);

            assertThatThrownBy(() -> generateServiceOrderQuoteUseCase.execute(cmd))
                    .isInstanceOf(QuoteAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção ao gerar orçamento com item de estoque duplicado")
        void shouldThrowExceptionWhenDuplicateStockItem() {
            UUID serviceOrderId = createAndStartDiagnostic();
            var stockItems = List.of(
                    stockItem(stockId, 1),
                    stockItem(stockId, 2));

            assertThatThrownBy(() -> generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                            serviceOrderId, stockItems, List.of())))
                    .isInstanceOf(QuoteItemAlreadyExistsException.class);
        }

        @Test
        @DisplayName("Deve guardar nome e preço enviados no item de estoque")
        void shouldKeepProductNameAndUnitPriceFromCommand() {
            UUID serviceOrderId = createAndStartDiagnostic();

            Quote quote = generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                            serviceOrderId, stockOnlyCommands(2), List.of()));

            assertThat(quote.getStockItems())
                    .extracting(QuoteStockItem::getStockId, QuoteStockItem::getProductName,
                            item -> item.getUnitPrice().stripTrailingZeros())
                    .containsExactlyInAnyOrder(
                            tuple(stockId, "Peça Svc IT 1", new BigDecimal("100.00").stripTrailingZeros()),
                            tuple(stockId2, "Peça Svc IT 2", new BigDecimal("200.00").stripTrailingZeros()));
            assertThat(quote.getTotalPrice()).isEqualByComparingTo(new BigDecimal("600.00"));
        }

        @Test
        @DisplayName("Deve lançar exceção ao gerar orçamento de OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            assertThatThrownBy(() -> generateServiceOrderQuoteUseCase.execute(
                    new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                            UUID.randomUUID(), stockOnlyCommands(1), List.of())))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("approveQuote")
    class ApproveQuote {

        @Test
        @DisplayName("Deve aprovar orçamento pendente")
        void shouldApproveQuote() {
            Quote quote = createAndGenerateQuote();

            Quote response = approveServiceOrderQuoteUseCase.execute(quote.getId());

            assertThat(response.getStatus()).isEqualTo(QuoteStatus.APPROVED);
            assertThat(response.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve lançar exceção ao aprovar orçamento não pendente")
        void shouldThrowExceptionWhenApprovingNonPendingQuote() {
            Quote quote = createAndGenerateQuote();
            approveServiceOrderQuoteUseCase.execute(quote.getId());
            UUID quoteId = quote.getId();

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
            Quote quote = createAndGenerateQuote();

            Quote response = reproveServiceOrderQuoteUseCase.execute(
                    new ReproveServiceOrderQuoteUseCase.ReproveServiceOrderQuoteCommand(
                            quote.getId(), "Outra oficina passou um orçamento mais em conta"));

            assertThat(response.getStatus()).isEqualTo(QuoteStatus.REPROVED);
            assertThat(response.getQuoteRefusalReason())
                    .isEqualTo("Outra oficina passou um orçamento mais em conta");
        }

        @Test
        @DisplayName("Deve lançar exceção ao reprovar orçamento não pendente")
        void shouldThrowExceptionWhenReprovingNonPendingQuote() {
            Quote quote = createAndGenerateQuote();
            approveServiceOrderQuoteUseCase.execute(quote.getId());
            UUID quoteId = quote.getId();

            assertThatThrownBy(() -> reproveServiceOrderQuoteUseCase.execute(
                    new ReproveServiceOrderQuoteUseCase.ReproveServiceOrderQuoteCommand(quoteId, "Motivo")))
                    .isInstanceOf(InvalidQuoteStatusException.class);
        }
    }

    private CreateServiceOrderUseCase.CreateServiceOrderCommand validCommand() {
        return new CreateServiceOrderUseCase.CreateServiceOrderCommand(
                vehicleId, customerId, "Relatório de teste da OS", userEmail);
    }

    private UUID createAndStartDiagnostic() {
        ServiceOrder created = createServiceOrderUseCase.execute(validCommand());
        startDiagnosticUseCase.execute(created.getId());
        return created.getId();
    }

    private Quote createAndGenerateQuote() {
        UUID serviceOrderId = createAndStartDiagnostic();
        return generateServiceOrderQuoteUseCase.execute(
                new GenerateServiceOrderQuoteUseCase.GenerateQuoteCommand(
                        serviceOrderId, stockOnlyCommands(1), List.of()));
    }

    private List<CreateQuoteUseCase.StockItemCommand> stockOnlyCommands(int quantity) {
        return List.of(stockItem(stockId, quantity), stockItem(stockId2, quantity));
    }

    private CreateQuoteUseCase.StockItemCommand stockItem(UUID id, int quantity) {
        return id.equals(stockId)
                ? new CreateQuoteUseCase.StockItemCommand(id, "Peça Svc IT 1", new BigDecimal("100.00"), quantity)
                : new CreateQuoteUseCase.StockItemCommand(id, "Peça Svc IT 2", new BigDecimal("200.00"), quantity);
    }

    private List<CreateQuoteUseCase.ServiceItemCommand> serviceItemCommands() {
        return List.of(new CreateQuoteUseCase.ServiceItemCommand(serviceCatalogId));
    }
}