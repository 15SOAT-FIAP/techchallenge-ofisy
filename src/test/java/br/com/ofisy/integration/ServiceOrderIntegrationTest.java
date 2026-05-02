package br.com.ofisy.integration;

import br.com.ofisy.application.serviceorder.ServiceOrderService;
import br.com.ofisy.application.serviceorder.dto.ServiceOrderRequestDTO;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.serviceorder.exceptions.InvalidServiceOrderTransitionException;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Tag("integration")
class ServiceOrderIntegrationTest extends IntegrationTestBase {

    @Autowired
    private ServiceOrderService serviceOrderService;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID customerId;
    private UUID vehicleId;
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
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("Deve criar ordem de serviço com sucesso")
        void shouldCreateServiceOrderSuccessfully() {
            var request = new ServiceOrderRequestDTO(vehicleId, customerId, "Veículo com problema no freio");

            var response = serviceOrderService.create(request, userEmail);

            assertThat(response).isNotNull();
            assertThat(response.vehicleId()).isEqualTo(vehicleId);
            assertThat(response.customerId()).isEqualTo(customerId);
            assertThat(response.status()).isEqualTo(ServiceOrderStatus.RECEIVED.name());
            assertThat(response.report()).isEqualTo("Veículo com problema no freio");
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
            var request = new ServiceOrderRequestDTO(vehicleId, customerId, "Relatório");
            var created = serviceOrderService.create(request, userEmail);

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
        @DisplayName("Deve lançar exceção ao iniciar diagnóstico de OS com status inválido")
        void shouldThrowExceptionWhenStatusTransitionIsInvalid() {
            var request = new ServiceOrderRequestDTO(vehicleId, customerId, "Relatório");
            var created = serviceOrderService.create(request, userEmail);
            serviceOrderService.startDiagnostic(created.id());

            assertThatThrownBy(() -> serviceOrderService.startDiagnostic(created.id()))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);
        }
    }

    @Nested
    @DisplayName("close")
    class Close {

        @Test
        @DisplayName("Deve cancelar ordem de serviço com sucesso")
        void shouldCloseServiceOrderSuccessfully() {
            var request = new ServiceOrderRequestDTO(vehicleId, customerId, "Relatório");
            var created = serviceOrderService.create(request, userEmail);

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
            var request = new ServiceOrderRequestDTO(vehicleId, customerId, "Relatório");
            var created = serviceOrderService.create(request, userEmail);

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
            var request = new ServiceOrderRequestDTO(vehicleId, customerId, "Relatório");
            serviceOrderService.create(request, userEmail);

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
            var request = new ServiceOrderRequestDTO(vehicleId, customerId, "Relatório");
            var created = serviceOrderService.create(request, userEmail);

            assertThatThrownBy(() -> serviceOrderService.deliverToCustomer(created.id()))
                    .isInstanceOf(InvalidServiceOrderTransitionException.class);
        }
    }
}
