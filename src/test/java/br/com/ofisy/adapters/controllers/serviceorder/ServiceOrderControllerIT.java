package br.com.ofisy.adapters.controllers.serviceorder;

import br.com.ofisy.adapters.controllers.serviceorder.dto.CreateServiceOrderResponseDTO;
import br.com.ofisy.adapters.controllers.serviceorder.dto.ServiceOrderResponseDTO;
import br.com.ofisy.adapters.controllers.serviceorder.dto.ServiceOrderStatusResponseDTO;
import br.com.ofisy.domain.customer.CpfCnpj;
import br.com.ofisy.domain.customer.Customer;
import br.com.ofisy.domain.customer.CustomerRepository;
import br.com.ofisy.domain.notification.NotificationRepository;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
import br.com.ofisy.domain.serviceorder.ServiceOrderStatus;
import br.com.ofisy.domain.stock.Stock;
import br.com.ofisy.domain.stock.StockRepository;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.domain.vehicle.VehicleRepository;
import br.com.ofisy.integration.IntegrationTestBase;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceOrderControllerIT extends IntegrationTestBase {

    @Autowired private CustomerRepository customerDomainRepository;
    @Autowired private VehicleRepository vehicleDomainRepository;
    @Autowired private UserRepository userDomainRepository;
    @Autowired private StockRepository stockDomainRepository;
    @Autowired private ServiceCatalogRepository serviceCatalogDomainRepository;
    @Autowired private NotificationRepository notificationDomainRepository;

    private UUID customerId;
    private UUID vehicleId;
    private UUID stockId;
    private UUID stockId2;
    private UUID serviceCatalogId;
    private String token;

    @BeforeEach
    void setUp() {
        String rawPassword = "Test@123";
        User user = userDomainRepository.save(
                User.create("attendant.ctrl.it@ofisy.com", passwordEncoder.encode(rawPassword), "Atendente IT", Role.ATTENDANT));
        token = obtainToken(user.getEmail().emailAddress(), rawPassword);

        Customer customer = customerDomainRepository.save(
                Customer.create(new CpfCnpj("71428793860"), "João Ctrl IT", "joao.ctrl.it@ofisy.com", "11991111111"));
        customerId = customer.getId();

        Vehicle vehicle = vehicleDomainRepository.save(
                Vehicle.create(customerId, new LicensePlate("CTR1T01"), "Civic", "Honda", "Preto", 2022, null));
        vehicleId = vehicle.getId();

        Stock stock1 = stockDomainRepository.save(
                Stock.create("Peça Ctrl IT 1", "Peça 1 para testes de controller", 10, new BigDecimal("100.00"), "Testes", 2));
        stockId = stock1.getId();

        Stock stock2 = stockDomainRepository.save(
                Stock.create("Peça Ctrl IT 2", "Peça 2 para testes de controller", 10, new BigDecimal("200.00"), "Testes", 2));
        stockId2 = stock2.getId();

        ServiceCatalog serviceCatalog = serviceCatalogDomainRepository.save(
                ServiceCatalog.create("Serviço Ctrl IT", "Serviço para testes de controller", new BigDecimal("200.00")));
        serviceCatalogId = serviceCatalog.getId();
    }

    @Nested
    @DisplayName("POST /api/v1/service-orders")
    class Create {

        @Test
        @DisplayName("Deve criar ordem de serviço sem itens com status RECEIVED")
        void shouldCreateServiceOrderSuccessfully() {
            var serviceOrderId = createServiceOrder();

            var status = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/api/v1/service-orders/{id}/status", serviceOrderId)
                    .then()
                    .statusCode(200)
                    .extract().as(ServiceOrderStatusResponseDTO.class);

            assertThat(status.status()).isEqualTo(ServiceOrderStatus.RECEIVED);
        }

        @Test
        @DisplayName("Deve retornar 422 quando veículo não pertence ao cliente")
        void shouldReturn422WhenVehicleNotOwnedByCustomer() {
            Customer other = customerDomainRepository.save(
                    Customer.create(new CpfCnpj("11144477735"), "Maria IT", "maria.it@ofisy.com", "11988888888"));

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
    @DisplayName("POST /api/v1/service-orders/create-complete")
    class CreateComplete {

        @Test
        @DisplayName("Deve criar OS com itens e retornar serviceOrderId")
        void shouldCreateCompleteServiceOrderAndReturnId() {
            var response = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "vehicleId", vehicleId,
                            "customerId", customerId,
                            "report", "Relatório completo",
                            "stockItems", List.of(Map.of("stockId", stockId, "quantity", 1)),
                            "serviceItems", List.of(Map.of("serviceCatalogId", serviceCatalogId))
                    ))
                    .when()
                    .post("/api/v1/service-orders/create-complete")
                    .then()
                    .statusCode(201)
                    .extract().as(CreateServiceOrderResponseDTO.class);

            assertThat(response.serviceOrderId()).isNotNull();
        }

        @Test
        @DisplayName("Deve criar OS com itens e ir para AWAITING_APPROVAL")
        void shouldCreateCompleteAndGoToAwaitingApproval() {
            var response = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "vehicleId", vehicleId,
                            "customerId", customerId,
                            "report", "Relatório",
                            "stockItems", List.of(Map.of("stockId", stockId, "quantity", 1)),
                            "serviceItems", List.of()
                    ))
                    .when()
                    .post("/api/v1/service-orders/create-complete")
                    .then()
                    .statusCode(201)
                    .extract().as(CreateServiceOrderResponseDTO.class);

            var status = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/api/v1/service-orders/{id}/status", response.serviceOrderId())
                    .then()
                    .statusCode(200)
                    .extract().as(ServiceOrderStatusResponseDTO.class);

            assertThat(status.status()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
        }

        @Test
        @DisplayName("Deve consumir estoque ao criar OS completa")
        void shouldConsumeStockWhenCreatingComplete() {
            int before = stockRepository.findById(stockId).orElseThrow().getQuantity();

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "vehicleId", vehicleId,
                            "customerId", customerId,
                            "report", "Relatório",
                            "stockItems", List.of(Map.of("stockId", stockId, "quantity", 2)),
                            "serviceItems", List.of()
                    ))
                    .when()
                    .post("/api/v1/service-orders/create-complete")
                    .then()
                    .statusCode(201);

            assertThat(stockRepository.findById(stockId).orElseThrow().getQuantity()).isEqualTo(before - 2);
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
                    .extract().jsonPath().getList("content");

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

    @Nested
    @DisplayName("POST /api/v1/service-orders/{id}/quote")
    class GenerateQuote {

        @Test
        @DisplayName("Deve gerar orçamento e mudar status para AWAITING_APPROVAL")
        void shouldGenerateQuoteAndChangeStatus() {
            var serviceOrderId = createAndStartDiagnostic();

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(fullQuoteBody(2))
                    .when()
                    .post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                    .then()
                    .statusCode(201);

            var status = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/api/v1/service-orders/{id}/status", serviceOrderId)
                    .then()
                    .statusCode(200)
                    .extract().as(ServiceOrderStatusResponseDTO.class);

            assertThat(status.status()).isEqualTo(ServiceOrderStatus.AWAITING_APPROVAL);
        }

        @Test
        @DisplayName("Deve gerar orçamento com total calculado corretamente")
        void shouldGenerateQuoteWithCorrectTotal() {
            var serviceOrderId = createAndStartDiagnostic();

            var quote = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(fullQuoteBody(2))
                    .when()
                    .post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                    .then()
                    .statusCode(201)
                    .extract().jsonPath();

            assertThat(quote.getList("stockItems")).hasSize(1);
            assertThat(quote.getList("serviceItems")).hasSize(1);
            // stockId: 100 * 2 = 200, service: 200 → total = 400
            assertThat(new BigDecimal(quote.getString("totalPrice")))
                    .isEqualByComparingTo(new BigDecimal("400.00"));
        }

        @Test
        @DisplayName("Deve criar notificação ao gerar orçamento")
        void shouldCreateNotificationWhenGeneratingQuote() {
            var serviceOrderId = createAndStartDiagnostic();

            var quoteId = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(stockOnlyQuoteBody(1))
                    .when()
                    .post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                    .then()
                    .statusCode(201)
                    .extract().jsonPath().getUUID("id");

            var notifications = notificationDomainRepository.findAll().stream()
                    .filter(n -> n.getQuoteId() != null && n.getQuoteId().equals(quoteId))
                    .toList();

            assertThat(notifications).hasSize(1);
            assertThat(notifications.getFirst().isRead()).isFalse();
        }

        @Test
        @DisplayName("Deve retornar 400 quando ambas as listas estão vazias")
        void shouldReturn400WhenBothListsAreEmpty() {
            var serviceOrderId = createAndStartDiagnostic();

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of("stockItems", List.of(), "serviceItems", List.of()))
                    .when()
                    .post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                    .then()
                    .statusCode(400);
        }

        @Test
        @DisplayName("Deve retornar 409 ao gerar orçamento duplicado")
        void shouldReturn409WhenQuoteAlreadyExists() {
            var serviceOrderId = createAndStartDiagnostic();
            var body = stockOnlyQuoteBody(1);

            RestAssured.given().header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON).body(body)
                    .when().post("/api/v1/service-orders/{id}/quote", serviceOrderId);

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(body)
                    .when()
                    .post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                    .then()
                    .statusCode(409);
        }

        @Test
        @DisplayName("Deve consumir estoque ao gerar orçamento")
        void shouldConsumeStockWhenGeneratingQuote() {
            var serviceOrderId = createAndStartDiagnostic();
            int before1 = stockRepository.findById(stockId).orElseThrow().getQuantity();
            int before2 = stockRepository.findById(stockId2).orElseThrow().getQuantity();

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(stockOnlyQuoteBody(2))
                    .when()
                    .post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                    .then()
                    .statusCode(201);

            assertThat(stockRepository.findById(stockId).orElseThrow().getQuantity()).isEqualTo(before1 - 2);
            assertThat(stockRepository.findById(stockId2).orElseThrow().getQuantity()).isEqualTo(before2 - 2);
        }

        @Test
        @DisplayName("Deve retornar 404 ao gerar orçamento de OS não encontrada")
        void shouldReturn404WhenServiceOrderNotFound() {
            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(stockOnlyQuoteBody(1))
                    .when()
                    .post("/api/v1/service-orders/{id}/quote", UUID.randomUUID())
                    .then()
                    .statusCode(404);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/service-orders/{id}/quote")
    class UpdateQuote {

        @Test
        @DisplayName("Deve atualizar orçamento pendente com sucesso")
        void shouldUpdatePendingQuote() {
            var serviceOrderId = createAndStartDiagnostic();
            generateStockOnlyQuote(serviceOrderId, 1);

            var quoteId = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .get("/api/v1/service-orders/{id}/status", serviceOrderId)
                    .then()
                    .statusCode(200)
                    .extract().jsonPath().getUUID("quoteId");

            var response = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of(
                            "stockItems", List.of(
                                    Map.of("stockId", stockId, "quantity", 3),
                                    Map.of("stockId", stockId2, "quantity", 1)
                            ),
                            "serviceItems", List.of()
                    ))
                    .when()
                    .patch("/api/v1/service-orders/{id}/quote", quoteId)
                    .then()
                    .statusCode(200)
                    .extract().jsonPath();

            assertThat(response.getList("stockItems")).hasSize(2);
        }

        @Test
        @DisplayName("Deve retornar 409 ao editar orçamento não pendente")
        void shouldReturn409WhenQuoteIsNotPending() {
            var serviceOrderId = createAndStartDiagnostic();
            var quoteId = generateStockOnlyQuote(serviceOrderId, 1);

            RestAssured.given().header("Authorization", "Bearer " + token)
                    .when().patch("/api/v1/service-orders/quote/{id}/approve", quoteId);

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of("stockItems", List.of(Map.of("stockId", stockId, "quantity", 1)), "serviceItems", List.of()))
                    .when()
                    .patch("/api/v1/service-orders/{id}/quote", quoteId)
                    .then()
                    .statusCode(409);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/service-orders/quote/{id}/approve")
    class ApproveQuote {

        @Test
        @DisplayName("Deve aprovar orçamento pendente")
        void shouldApproveQuote() {
            var serviceOrderId = createAndStartDiagnostic();
            var quoteId = generateStockOnlyQuote(serviceOrderId, 1);

            var response = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .patch("/api/v1/service-orders/quote/{id}/approve", quoteId)
                    .then()
                    .statusCode(200)
                    .extract().jsonPath();

            assertThat(response.getString("status")).isEqualTo("APPROVED");
        }

        @Test
        @DisplayName("Deve retornar 409 ao aprovar orçamento não pendente")
        void shouldReturn409WhenApprovingNonPendingQuote() {
            var serviceOrderId = createAndStartDiagnostic();
            var quoteId = generateStockOnlyQuote(serviceOrderId, 1);

            RestAssured.given().header("Authorization", "Bearer " + token)
                    .when().patch("/api/v1/service-orders/quote/{id}/approve", quoteId);

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .when()
                    .patch("/api/v1/service-orders/quote/{id}/approve", quoteId)
                    .then()
                    .statusCode(409);
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/service-orders/quote/{id}/reprove")
    class ReproveQuote {

        @Test
        @DisplayName("Deve reprovar orçamento pendente com motivo")
        void shouldReproveQuoteWithReason() {
            var serviceOrderId = createAndStartDiagnostic();
            var quoteId = generateStockOnlyQuote(serviceOrderId, 1);

            var response = RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of("reason", "Outra oficina passou um orçamento mais em conta"))
                    .when()
                    .patch("/api/v1/service-orders/quote/{id}/reprove", quoteId)
                    .then()
                    .statusCode(200)
                    .extract().jsonPath();

            assertThat(response.getString("status")).isEqualTo("REPROVED");
            assertThat(response.getString("quoteRefusalReason")).isEqualTo("Outra oficina passou um orçamento mais em conta");
        }

        @Test
        @DisplayName("Deve retornar 409 ao reprovar orçamento não pendente")
        void shouldReturn409WhenReprovingNonPendingQuote() {
            var serviceOrderId = createAndStartDiagnostic();
            var quoteId = generateStockOnlyQuote(serviceOrderId, 1);

            RestAssured.given().header("Authorization", "Bearer " + token)
                    .when().patch("/api/v1/service-orders/quote/{id}/approve", quoteId);

            RestAssured.given()
                    .header("Authorization", "Bearer " + token)
                    .contentType(ContentType.JSON)
                    .body(Map.of("reason", "Valor alto demais"))
                    .when()
                    .patch("/api/v1/service-orders/quote/{id}/reprove", quoteId)
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
                .extract().as(ServiceOrderResponseDTO.class).id();
    }

    private UUID createAndStartDiagnostic() {
        var serviceOrderId = createServiceOrder();
        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .patch("/api/v1/service-orders/{id}/start-diagnostic", serviceOrderId)
                .then()
                .statusCode(200);
        return serviceOrderId;
    }

    private UUID generateStockOnlyQuote(UUID serviceOrderId, int quantity) {
        return RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body(stockOnlyQuoteBody(quantity))
                .when()
                .post("/api/v1/service-orders/{id}/quote", serviceOrderId)
                .then()
                .statusCode(201)
                .extract().jsonPath().getUUID("id");
    }

    private Map<String, Object> stockOnlyQuoteBody(int quantity) {
        return Map.of(
                "stockItems", List.of(
                        Map.of("stockId", stockId, "quantity", quantity),
                        Map.of("stockId", stockId2, "quantity", quantity)
                ),
                "serviceItems", List.of()
        );
    }

    private Map<String, Object> fullQuoteBody(int quantity) {
        return Map.of(
                "stockItems", List.of(Map.of("stockId", stockId, "quantity", quantity)),
                "serviceItems", List.of(Map.of("serviceCatalogId", serviceCatalogId))
        );
    }
}