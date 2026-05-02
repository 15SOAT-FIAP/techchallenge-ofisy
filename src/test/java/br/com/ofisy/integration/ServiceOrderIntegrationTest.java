package br.com.ofisy.integration;

import br.com.ofisy.application.quote.dto.CreateQuoteRequestDTO;
import br.com.ofisy.application.quote.dto.ServiceItemRequestDTO;
import br.com.ofisy.application.quote.dto.StockItemRequestDTO;
import br.com.ofisy.application.serviceorder.ServiceOrderService;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.notification.NotificationType;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
class ServiceOrderIntegrationTest extends IntegrationTestBase {

    @Autowired private ServiceOrderService serviceOrderService;
    @Autowired private CustomerRepository customerRepository;
    @Autowired private VehicleRepository vehicleRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private StockRepository stockRepository;
    @Autowired private ServiceCatalogRepository serviceCatalogRepository;
    @Autowired private ServiceOrderExecutionRepository serviceOrderExecutionRepository;
    @Autowired private NotificationRepository notificationRepository;

    private UUID customerId;
    private UUID vehicleId;
    private UUID stockId;
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
                Stock.create("Peça OS Teste", "Peça para testes de OS", 10, new BigDecimal("100.00"), "Testes", 2)
        );
        stockId = stock.getId();
    }

    private ServiceOrderRequestDTO defaultRequest() {
        return new ServiceOrderRequestDTO(vehicleId, customerId, "Relatório de teste da OS");
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
            var request = new ServiceOrderRequestDTO(vehicleId, otherCustomer.getId(), "Relatório");

            assertThatThrownBy(() -> serviceOrderService.create(request, userEmail))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando cliente não encontrado")
        void shouldThrowExceptionWhenCustomerNotFound() {
            var request = new ServiceOrderRequestDTO(vehicleId, UUID.randomUUID(), "Relatório");

            assertThatThrownBy(() -> serviceOrderService.create(request, userEmail))
                    .isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando veículo não encontrado")
        void shouldThrowExceptionWhenVehicleNotFound() {
            var request = new ServiceOrderRequestDTO(UUID.randomUUID(), customerId, "Relatório");

            assertThatThrownBy(() -> serviceOrderService.create(request, userEmail))
                    .isInstanceOf(RuntimeException.class);
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
    @DisplayName("generateQuote")
    class GenerateQuote {

        @Test
        @DisplayName("Deve gerar orçamento e mudar status da OS para AWAITING_APPROVAL")
        void shouldGenerateQuoteAndChangeStatusToAwaitingApproval() {
            var created = serviceOrderService.create(defaultRequest(), userEmail);
            serviceOrderService.startDiagnostic(created.id());

            var quoteRequest = new CreateQuoteRequestDTO(
                    created.id(),
                    List.of(new StockItemRequestDTO(stockId, 1)),
                    List.of()
            );

            var quote = serviceOrderService.generateQuote(created.id(), quoteRequest);

            assertThat(quote).isNotNull();
            assertThat(quote.serviceOrderId()).isEqualTo(created.id());

            var status = serviceOrderService.getStatus(created.id());
            assertThat(status.status()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
        }

        @Test
        @DisplayName("Deve criar notificação ao gerar orçamento")
        void shouldCreateNotificationWhenGeneratingQuote() {
            var created = serviceOrderService.create(defaultRequest(), userEmail);
            serviceOrderService.startDiagnostic(created.id());

            var quoteRequest = new CreateQuoteRequestDTO(
                    created.id(),
                    List.of(new StockItemRequestDTO(stockId, 1)),
                    List.of()
            );

            var quote = serviceOrderService.generateQuote(created.id(), quoteRequest);

            var notifications = notificationRepository.findAll().stream()
                    .filter(n -> n.getType() == NotificationType.QUOTE_GENERATED)
                    .filter(n -> quote.id().equals(n.getQuoteId()))
                    .toList();

            assertThat(notifications).hasSize(1);
            assertThat(notifications.getFirst().getRead()).isFalse();
        }

        @Test
        @DisplayName("Deve gerar orçamento com itens de serviço e notificar")
        void shouldGenerateQuoteWithServiceItemsAndNotify() {
            var created = serviceOrderService.create(defaultRequest(), userEmail);
            serviceOrderService.startDiagnostic(created.id());

            ServiceOrderExecution execution = serviceOrderExecutionRepository.save(
                    ServiceOrderExecution.create(
                            serviceCatalogRepository.findAll(PageRequest.of(0, 1)).getContent().getFirst().getId(),
                            created.id()
                    )
            );

            var quoteRequest = new CreateQuoteRequestDTO(
                    created.id(),
                    List.of(new StockItemRequestDTO(stockId, 1)),
                    List.of(new ServiceItemRequestDTO(execution.getId()))
            );

            var quote = serviceOrderService.generateQuote(created.id(), quoteRequest);

            assertThat(quote.serviceItems()).hasSize(1);
            assertThat(quote.stockItems()).hasSize(1);

            var notifications = notificationRepository.findAll().stream()
                    .filter(n -> n.getType() == NotificationType.QUOTE_GENERATED)
                    .toList();
            assertThat(notifications).hasSize(1);
        }

        @Test
        @DisplayName("Deve lançar exceção ao gerar orçamento de OS não encontrada")
        void shouldThrowExceptionWhenServiceOrderNotFound() {
            var quoteRequest = new CreateQuoteRequestDTO(
                    UUID.randomUUID(),
                    List.of(new StockItemRequestDTO(stockId, 1)),
                    List.of()
            );
            UUID quoteId = UUID.randomUUID();
            assertThatThrownBy(() -> serviceOrderService.generateQuote(quoteId, quoteRequest))
                    .isInstanceOf(ServiceOrderNotFoundException.class);
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
}
