package br.com.ofisy.adapters.controllers.serviceorder;

import br.com.ofisy.adapters.controllers.serviceorder.dto.ServiceOrderResponseDTO;
import br.com.ofisy.adapters.controllers.serviceorder.dto.ServiceOrderStatusResponseDTO;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import br.com.ofisy.integration.IntegrationTestBase;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderControllerIT extends IntegrationTestBase {

    @Autowired
    private CustomerRepository customerDomainRepository;
    @Autowired
    private VehicleRepository vehicleDomainRepository;
    @Autowired
    private UserRepository userDomainRepository;

    private UUID customerId;
    private UUID vehicleId;
    private String token;

    @BeforeEach
    void setUp() {
        String rawPassword = "Test@123";
        User user = userDomainRepository.save(
                User.create("attendant.ctrl.it@ofisy.com", passwordEncoder.encode(rawPassword), "Atendente IT", Role.ATTENDANT)
        );
        token = obtainToken(user.getEmail().emailAddress(), rawPassword);

        Customer customer = customerDomainRepository.save(
                Customer.create(new CpfCnpj("71428793860"), "João Ctrl IT", "joao.ctrl.it@ofisy.com", "11991111111")
        );
        customerId = customer.getId();

        Vehicle vehicle = vehicleDomainRepository.save(
                Vehicle.create(customerId, new LicensePlate("CTR1T01"), "Civic", "Honda", "Preto", 2022, null)
        );
        vehicleId = vehicle.getId();
    }

    @Nested
    @DisplayName("POST /api/v1/service-orders")
    class Create {

        @Test
        @DisplayName("Deve criar ordem de serviço sem itens com status RECEIVED")
        void shouldCreateServiceOrderSuccessfully() {
            var response = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of("vehicleId", vehicleId, "customerId", customerId, "report", "Relatório de teste da OS"))
                    .when()
                    .post("/api/v1/service-orders")
                    .then()
                    .statusCode(201)
                    .extract().as(ServiceOrderResponseDTO.class);

            assertThat(response.vehicleId()).isEqualTo(vehicleId);
            assertThat(response.customerId()).isEqualTo(customerId);
            assertThat(response.status()).isEqualTo(ServiceOrderStatus.RECEIVED.name());
            assertThat(response.createdAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar 422 quando veículo não pertence ao cliente")
        void shouldReturn422WhenVehicleNotOwnedByCustomer() {
            Customer other = customerDomainRepository.save(
                    Customer.create(new CpfCnpj("11144477735"), "Maria IT", "maria.it@ofisy.com", "11988888888")
            );

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of("vehicleId", vehicleId, "customerId", other.getId(), "report", "Relatório"))
                    .when()
                    .post("/api/v1/service-orders")
                    .then()
                    .statusCode(422);
        }

        @Test
        @DisplayName("Deve retornar 404 quando cliente não encontrado")
        void shouldReturn404WhenCustomerNotFound() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of("vehicleId", vehicleId, "customerId", UUID.randomUUID(), "report", "Relatório"))
                    .when()
                    .post("/api/v1/service-orders")
                    .then()
                    .statusCode(404);
        }

        @Test
        @DisplayName("Deve retornar 404 quando veículo não encontrado")
        void shouldReturn404WhenVehicleNotFound() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of("vehicleId", UUID.randomUUID(), "customerId", customerId, "report", "Relatório"))
                    .when()
                    .post("/api/v1/service-orders")
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/service-orders/{id}/start-diagnostic")
    class StartDiagnostic {

        @Test
        @DisplayName("Deve iniciar diagnóstico com sucesso")
        void shouldStartDiagnosticSuccessfully() {
            var serviceOrderId = createServiceOrder();

            var response = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .patch("/api/v1/service-orders/{id}/start-diagnostic", serviceOrderId)
                    .then()
                    .statusCode(200)
                    .extract().as(ServiceOrderResponseDTO.class);

            assertThat(response.status()).isEqualTo(ServiceOrderStatus.IN_DIAGNOSTIC.name());
            assertThat(response.updatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar 404 ao iniciar diagnóstico de OS não encontrada")
        void shouldReturn404WhenServiceOrderNotFound() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .patch("/api/v1/service-orders/{id}/start-diagnostic", UUID.randomUUID())
                    .then()
                    .statusCode(404);
        }

        @Test
        @DisplayName("Deve retornar 409 ao iniciar diagnóstico com transição inválida")
        void shouldReturn409WhenStatusTransitionIsInvalid() {
            var serviceOrderId = createServiceOrder();
            RestAssured.given().header("Authorization", "Bearer " + token)
                    .when().patch("/api/v1/service-orders/{id}/start-diagnostic", serviceOrderId);

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .patch("/api/v1/service-orders/{id}/start-diagnostic", serviceOrderId)
                    .then()
                    .statusCode(409);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/service-orders/{id}/cancel")
    class Cancel {

        @Test
        @DisplayName("Deve cancelar ordem de serviço com sucesso")
        void shouldCancelServiceOrderSuccessfully() {
            var serviceOrderId = createServiceOrder();

            var response = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .patch("/api/v1/service-orders/{id}/cancel", serviceOrderId)
                    .then()
                    .statusCode(200)
                    .extract().as(ServiceOrderResponseDTO.class);

            assertThat(response.status()).isEqualTo(ServiceOrderStatus.CANCELLED.name());
        }

        @Test
        @DisplayName("Deve retornar 404 ao cancelar OS não encontrada")
        void shouldReturn404WhenServiceOrderNotFound() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .patch("/api/v1/service-orders/{id}/cancel", UUID.randomUUID())
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/service-orders/{id}/status")
    class GetStatus {

        @Test
        @DisplayName("Deve retornar status da ordem de serviço")
        void shouldReturnServiceOrderStatus() {
            var serviceOrderId = createServiceOrder();

            var response = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/api/v1/service-orders/{id}/status", serviceOrderId)
                    .then()
                    .statusCode(200)
                    .extract().as(ServiceOrderStatusResponseDTO.class);

            assertThat(response.id()).isEqualTo(serviceOrderId);
            assertThat(response.status()).isEqualTo(ServiceOrderStatus.RECEIVED);
        }

        @Test
        @DisplayName("Deve retornar 404 quando OS não encontrada")
        void shouldReturn404WhenServiceOrderNotFound() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/api/v1/service-orders/{id}/status", UUID.randomUUID())
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/service-orders/received")
    class ListReceived {

        @Test
        @DisplayName("Deve retornar ordens de serviço recebidas")
        void shouldReturnReceivedServiceOrders() {
            createServiceOrder();

            var content = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/api/v1/service-orders/received")
                    .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getList("content");

            assertThat(content).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/service-orders/{id}/deliver")
    class DeliverToCustomer {

        @Test
        @DisplayName("Deve retornar 409 ao entregar OS com status inválido")
        void shouldReturn409WhenDeliveringWithInvalidStatus() {
            var serviceOrderId = createServiceOrder();

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .patch("/api/v1/service-orders/{id}/deliver", serviceOrderId)
                    .then()
                    .statusCode(409);
        }
    }

    private UUID createServiceOrder() {
        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(Map.of("vehicleId", vehicleId, "customerId", customerId, "report", "Relatório de teste da OS"))
                .when()
                .post("/api/v1/service-orders")
                .then()
                .statusCode(201)
                .extract().jsonPath().getUUID("id");
    }
}
