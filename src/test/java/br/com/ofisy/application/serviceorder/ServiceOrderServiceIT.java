package br.com.ofisy.application.serviceorder;

import br.com.ofisy.application.customer.exceptions.CustomerNotFoundException;
import br.com.ofisy.application.serviceorder.cancel.CancelServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.create.CreateServiceOrderUseCase;
import br.com.ofisy.application.serviceorder.delivertocustomer.DeliverToCustomerUseCase;
import br.com.ofisy.application.serviceorder.exceptions.ServiceOrderNotFoundException;
import br.com.ofisy.application.serviceorder.exceptions.VehicleNotOwnedByCustomerException;
import br.com.ofisy.application.serviceorder.getstatus.GetServiceOrderStatusUseCase;
import br.com.ofisy.application.serviceorder.listreceived.ListReceivedServiceOrdersUseCase;
import br.com.ofisy.application.serviceorder.startdiagnostic.StartDiagnosticUseCase;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ServiceOrderServiceIT extends IntegrationTestBase {

    @Autowired private CreateServiceOrderUseCase createServiceOrderUseCase;
    @Autowired private CancelServiceOrderUseCase cancelServiceOrderUseCase;
    @Autowired private StartDiagnosticUseCase startDiagnosticUseCase;
    @Autowired private DeliverToCustomerUseCase deliverToCustomerUseCase;
    @Autowired private GetServiceOrderStatusUseCase getServiceOrderStatusUseCase;
    @Autowired private ListReceivedServiceOrdersUseCase listReceivedServiceOrdersUseCase;

    @Autowired private CustomerRepository customerDomainRepository;
    @Autowired private VehicleRepository vehicleDomainRepository;
    @Autowired private UserRepository userDomainRepository;

    private UUID customerId;
    private UUID vehicleId;
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

    private CreateServiceOrderUseCase.CreateServiceOrderCommand validCommand() {
        return new CreateServiceOrderUseCase.CreateServiceOrderCommand(
                vehicleId, customerId, "Relatório de teste da OS", userEmail);
    }
}
